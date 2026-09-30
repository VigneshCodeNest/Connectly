package com.connectly.service.impl;

import com.connectly.constant.StatusType;
import com.connectly.dto.request.CreateStatusRequest;
import com.connectly.dto.response.StatusResponse;
import com.connectly.dto.response.StatusViewResponse;
import com.connectly.dto.response.UserStatusFeedResponse;
import com.connectly.dto.response.UserSummaryResponse;
import com.connectly.entity.Status;
import com.connectly.entity.StatusView;
import com.connectly.entity.User;
import com.connectly.exception.BadRequestException;
import com.connectly.exception.ResourceNotFoundException;
import com.connectly.exception.UnauthorizedException;
import com.connectly.repository.ConnectionRequestRepository;
import com.connectly.repository.StatusRepository;
import com.connectly.repository.StatusViewRepository;
import com.connectly.repository.UserRepository;
import com.connectly.service.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatusServiceImpl implements StatusService {

    private static final Logger logger = LoggerFactory.getLogger(StatusServiceImpl.class);

    private final StatusRepository statusRepository;
    private final StatusViewRepository statusViewRepository;
    private final ConnectionRequestRepository connectionRepository;
    private final com.connectly.repository.BlockedUserRepository blockedUserRepository;
    private final UserRepository userRepository;

    public StatusServiceImpl(
            StatusRepository statusRepository,
            StatusViewRepository statusViewRepository,
            ConnectionRequestRepository connectionRepository,
            com.connectly.repository.BlockedUserRepository blockedUserRepository,
            UserRepository userRepository
    ) {
        this.statusRepository = statusRepository;
        this.statusViewRepository = statusViewRepository;
        this.connectionRepository = connectionRepository;
        this.blockedUserRepository = blockedUserRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public StatusResponse createStatus(CreateStatusRequest request, User currentUser) {
        if (request.getStatusType() == null) {
            throw new BadRequestException("Status type is required (TEXT or IMAGE)");
        }

        // Strict validation: Only TEXT and IMAGE statuses are supported. VIDEO, AUDIO, MUSIC are strictly disallowed.
        if (request.getStatusType() != StatusType.TEXT && request.getStatusType() != StatusType.IMAGE) {
            throw new BadRequestException("Unsupported status type: " + request.getStatusType() + ". Only TEXT and IMAGE statuses are supported");
        }

        if (request.getStatusType() == StatusType.TEXT) {
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                throw new BadRequestException("Text status content cannot be empty");
            }
        } else if (request.getStatusType() == StatusType.IMAGE) {
            if (request.getMediaUrl() == null || request.getMediaUrl().trim().isEmpty()) {
                throw new BadRequestException("Media URL is required for image status");
            }
        }

        Status status = new Status();
        status.setUser(currentUser);
        status.setStatusType(request.getStatusType());
        status.setContent(request.getContent() != null ? request.getContent().trim() : null);
        status.setMediaUrl(request.getMediaUrl() != null ? request.getMediaUrl().trim() : null);
        status.setBackgroundColor(request.getBackgroundColor() != null ? request.getBackgroundColor().trim() : null);

        // Automatic 24-hour expiration
        status.setExpiresAt(LocalDateTime.now().plusHours(24));

        Status savedStatus = statusRepository.save(status);
        logger.info("Created {} status (ID: {}) for user {}", savedStatus.getStatusType(), savedStatus.getId(), currentUser.getUsername());

        return StatusResponse.fromEntity(savedStatus, 0, false, Collections.emptyList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserStatusFeedResponse> getStatusFeed(User currentUser) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Get connected contacts, excluding blocked users
        List<Long> contactIds = connectionRepository.findConnectedUserIds(currentUser.getId()).stream()
                .filter(id -> !blockedUserRepository.existsByUserIdAndBlockedUserId(currentUser.getId(), id) &&
                              !blockedUserRepository.existsByUserIdAndBlockedUserId(id, currentUser.getId()))
                .collect(Collectors.toList());

        List<Long> allUserIds = new ArrayList<>(contactIds);
        allUserIds.add(currentUser.getId());

        // 2. Fetch all active (non-expired) statuses for user and contacts
        List<Status> activeStatuses = statusRepository.findByUserIdInAndExpiresAtAfterOrderByCreatedAtDesc(allUserIds, now);

        // 3. Group by user
        Map<Long, List<Status>> statusesByUser = new LinkedHashMap<>();
        for (Status s : activeStatuses) {
            statusesByUser.computeIfAbsent(s.getUser().getId(), k -> new ArrayList<>()).add(s);
        }

        List<UserStatusFeedResponse> feed = new ArrayList<>();

        for (Map.Entry<Long, List<Status>> entry : statusesByUser.entrySet()) {
            List<Status> userStatuses = entry.getValue();
            if (userStatuses.isEmpty()) continue;

            User author = userStatuses.get(0).getUser();
            boolean isSelf = author.getId().equals(currentUser.getId());

            List<StatusResponse> statusResponses = new ArrayList<>();
            boolean allViewed = true;

            for (Status s : userStatuses) {
                int viewCount = (int) statusViewRepository.countByStatusId(s.getId());
                boolean viewedByMe = isSelf || statusViewRepository.existsByStatusIdAndViewerId(s.getId(), currentUser.getId());

                if (!viewedByMe) {
                    allViewed = false;
                }

                List<StatusViewResponse> viewers = isSelf
                        ? statusViewRepository.findByStatusIdOrderByViewedAtDesc(s.getId()).stream()
                            .map(StatusViewResponse::fromEntity).collect(Collectors.toList())
                        : Collections.emptyList();

                statusResponses.add(StatusResponse.fromEntity(s, viewCount, viewedByMe, viewers));
            }

            LocalDateTime latestCreatedAt = userStatuses.get(0).getCreatedAt();
            feed.add(new UserStatusFeedResponse(
                    UserSummaryResponse.fromEntity(author),
                    statusResponses,
                    allViewed,
                    latestCreatedAt
            ));
        }

        return feed;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatusResponse> getMyStatuses(User currentUser) {
        LocalDateTime now = LocalDateTime.now();
        List<Status> myStatuses = statusRepository.findByUserIdAndExpiresAtAfterOrderByCreatedAtDesc(currentUser.getId(), now);
        List<StatusResponse> results = new ArrayList<>();

        for (Status s : myStatuses) {
            int viewCount = (int) statusViewRepository.countByStatusId(s.getId());
            List<StatusViewResponse> viewers = statusViewRepository.findByStatusIdOrderByViewedAtDesc(s.getId())
                    .stream()
                    .map(StatusViewResponse::fromEntity)
                    .collect(Collectors.toList());

            results.add(StatusResponse.fromEntity(s, viewCount, true, viewers));
        }

        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public StatusResponse getStatusById(Long statusId, User currentUser) {
        Status status = statusRepository.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException("Status not found with ID: " + statusId));

        if (status.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResourceNotFoundException("Status has expired and is no longer available");
        }

        boolean isSelf = status.getUser().getId().equals(currentUser.getId());
        int viewCount = (int) statusViewRepository.countByStatusId(status.getId());
        boolean viewedByMe = isSelf || statusViewRepository.existsByStatusIdAndViewerId(status.getId(), currentUser.getId());

        List<StatusViewResponse> viewers = isSelf
                ? statusViewRepository.findByStatusIdOrderByViewedAtDesc(status.getId()).stream()
                    .map(StatusViewResponse::fromEntity).collect(Collectors.toList())
                : Collections.emptyList();

        return StatusResponse.fromEntity(status, viewCount, viewedByMe, viewers);
    }

    @Override
    @Transactional
    public StatusResponse viewStatus(Long statusId, User currentUser) {
        Status status = statusRepository.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException("Status not found with ID: " + statusId));

        if (status.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResourceNotFoundException("Status has expired and is no longer available");
        }

        boolean isSelf = status.getUser().getId().equals(currentUser.getId());

        // Record view if viewer is not the author and hasn't viewed yet
        if (!isSelf && !statusViewRepository.existsByStatusIdAndViewerId(statusId, currentUser.getId())) {
            StatusView view = new StatusView(status, currentUser);
            statusViewRepository.save(view);
            logger.info("User {} viewed status ID {}", currentUser.getUsername(), statusId);
        }

        int viewCount = (int) statusViewRepository.countByStatusId(statusId);
        List<StatusViewResponse> viewers = isSelf
                ? statusViewRepository.findByStatusIdOrderByViewedAtDesc(statusId).stream()
                    .map(StatusViewResponse::fromEntity).collect(Collectors.toList())
                : Collections.emptyList();

        return StatusResponse.fromEntity(status, viewCount, true, viewers);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatusViewResponse> getStatusViewers(Long statusId, User currentUser) {
        Status status = statusRepository.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException("Status not found with ID: " + statusId));

        // Ownership enforcement: Only status owner can see viewer list
        if (!status.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Only the status owner can view who viewed their status");
        }

        return statusViewRepository.findByStatusIdOrderByViewedAtDesc(statusId)
                .stream()
                .map(StatusViewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteStatus(Long statusId, User currentUser) {
        Status status = statusRepository.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException("Status not found with ID: " + statusId));

        // Ownership enforcement: Only status owner can delete
        if (!status.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to delete another user's status");
        }

        statusViewRepository.deleteByStatusId(statusId);
        statusRepository.delete(status);
        logger.info("Status ID {} deleted by owner {}", statusId, currentUser.getUsername());
    }

    @Override
    @Transactional
    @Scheduled(fixedRate = 3600000) // Run every hour
    public int purgeExpiredStatuses() {
        LocalDateTime now = LocalDateTime.now();
        int viewsPurged = statusViewRepository.deleteViewsForExpiredStatuses(now);
        int statusesPurged = statusRepository.deleteExpiredStatuses(now);
        if (statusesPurged > 0) {
            logger.info("Purged {} expired statuses and {} associated views", statusesPurged, viewsPurged);
        }
        return statusesPurged;
    }
}
