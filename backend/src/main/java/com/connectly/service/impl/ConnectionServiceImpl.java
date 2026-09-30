package com.connectly.service.impl;

import com.connectly.constant.ConnectionStatus;
import com.connectly.dto.request.SendConnectionRequest;
import com.connectly.dto.response.ConnectionResponse;
import com.connectly.dto.response.ConnectionStatusResponse;
import com.connectly.entity.ConnectionRequest;
import com.connectly.entity.User;
import com.connectly.exception.BadRequestException;
import com.connectly.exception.ResourceNotFoundException;
import com.connectly.exception.UnauthorizedException;
import com.connectly.repository.ConnectionRequestRepository;
import com.connectly.repository.UserRepository;
import com.connectly.service.ConnectionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConnectionServiceImpl implements ConnectionService {

    private final ConnectionRequestRepository connectionRepository;
    private final UserRepository userRepository;
    private final com.connectly.repository.BlockedUserRepository blockedUserRepository;

    public ConnectionServiceImpl(
            ConnectionRequestRepository connectionRepository,
            UserRepository userRepository,
            com.connectly.repository.BlockedUserRepository blockedUserRepository
    ) {
        this.connectionRepository = connectionRepository;
        this.userRepository = userRepository;
        this.blockedUserRepository = blockedUserRepository;
    }

    @Override
    @Transactional
    public ConnectionResponse sendRequest(SendConnectionRequest request, User currentUser) {
        Long receiverId = request.getReceiverId();

        // 1. Rule: Cannot send request to self
        if (currentUser.getId().equals(receiverId)) {
            throw new BadRequestException("You cannot send a connection request to yourself");
        }

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + receiverId));

        return processSendRequest(currentUser, receiver);
    }

    @Override
    @Transactional
    public ConnectionResponse sendRequestByEmail(String email, User currentUser) {
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("Please provide a valid email address");
        }

        String cleanEmail = email.trim().toLowerCase();
        if (cleanEmail.equalsIgnoreCase(currentUser.getEmail())) {
            throw new BadRequestException("You cannot send a connection request to yourself");
        }

        User receiver = userRepository.findByEmailIgnoreCase(cleanEmail)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with email: " + cleanEmail));

        return processSendRequest(currentUser, receiver);
    }

    private ConnectionResponse processSendRequest(User sender, User receiver) {
        // Enforce blocking rule: Cannot send connection requests if either user blocked the other
        if (blockedUserRepository.existsByUserIdAndBlockedUserId(sender.getId(), receiver.getId()) ||
            blockedUserRepository.existsByUserIdAndBlockedUserId(receiver.getId(), sender.getId())) {
            throw new BadRequestException("Cannot send connection request. Communication is blocked between these users.");
        }

        Optional<ConnectionRequest> existing = connectionRepository.findBetweenUsers(sender.getId(), receiver.getId());

        if (existing.isPresent()) {
            ConnectionRequest conn = existing.get();

            // 2. Rule: Already connected
            if (conn.getStatus() == ConnectionStatus.ACCEPTED) {
                throw new BadRequestException("You are already connected with " + receiver.getName());
            }

            // 3. Rule: Duplicate pending request
            if (conn.getStatus() == ConnectionStatus.PENDING) {
                if (conn.getSender().getId().equals(sender.getId())) {
                    throw new BadRequestException("You have already sent a connection request to " + receiver.getName());
                } else {
                    // Receiver is sending to someone who already sent them a request -> auto accept
                    conn.setStatus(ConnectionStatus.ACCEPTED);
                    ConnectionRequest saved = connectionRepository.save(conn);
                    return ConnectionResponse.fromEntity(saved, sender);
                }
            }

            // If previously rejected, allow re-sending
            if (conn.getStatus() == ConnectionStatus.REJECTED) {
                conn.setSender(sender);
                conn.setReceiver(receiver);
                conn.setStatus(ConnectionStatus.PENDING);
                ConnectionRequest saved = connectionRepository.save(conn);
                return ConnectionResponse.fromEntity(saved, sender);
            }
        }

        // Create new pending connection request
        ConnectionRequest newRequest = new ConnectionRequest(sender, receiver, ConnectionStatus.PENDING);
        ConnectionRequest saved = connectionRepository.save(newRequest);
        return ConnectionResponse.fromEntity(saved, sender);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConnectionResponse> getIncomingRequests(User currentUser) {
        return connectionRepository.findByReceiverIdAndStatusOrderByCreatedAtDesc(currentUser.getId(), ConnectionStatus.PENDING)
                .stream()
                .map(cr -> ConnectionResponse.fromEntity(cr, currentUser))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConnectionResponse> getOutgoingRequests(User currentUser) {
        return connectionRepository.findBySenderIdAndStatusOrderByCreatedAtDesc(currentUser.getId(), ConnectionStatus.PENDING)
                .stream()
                .map(cr -> ConnectionResponse.fromEntity(cr, currentUser))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ConnectionResponse acceptRequest(Long requestId, User currentUser) {
        ConnectionRequest conn = connectionRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection request not found with ID: " + requestId));

        // Authorization rule: Only receiver can accept
        if (!conn.getReceiver().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to accept this connection request");
        }

        if (conn.getStatus() == ConnectionStatus.ACCEPTED) {
            return ConnectionResponse.fromEntity(conn, currentUser);
        }

        conn.setStatus(ConnectionStatus.ACCEPTED);
        ConnectionRequest saved = connectionRepository.save(conn);
        return ConnectionResponse.fromEntity(saved, currentUser);
    }

    @Override
    @Transactional
    public ConnectionResponse rejectRequest(Long requestId, User currentUser) {
        ConnectionRequest conn = connectionRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection request not found with ID: " + requestId));

        // Authorization rule: Only receiver can reject
        if (!conn.getReceiver().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to reject this connection request");
        }

        conn.setStatus(ConnectionStatus.REJECTED);
        ConnectionRequest saved = connectionRepository.save(conn);
        return ConnectionResponse.fromEntity(saved, currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConnectionResponse> getConnectedFriends(User currentUser) {
        return connectionRepository.findAcceptedConnections(currentUser.getId())
                .stream()
                .map(cr -> ConnectionResponse.fromEntity(cr, currentUser))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeConnection(Long requestId, User currentUser) {
        ConnectionRequest conn = connectionRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found with ID: " + requestId));

        // Authorization rule: Either participant can remove/disconnect
        if (!conn.getSender().getId().equals(currentUser.getId()) && !conn.getReceiver().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to remove this connection");
        }

        connectionRepository.delete(conn);
    }

    @Override
    @Transactional(readOnly = true)
    public ConnectionStatusResponse getConnectionStatus(Long targetUserId, User currentUser) {
        if (currentUser.getId().equals(targetUserId)) {
            return new ConnectionStatusResponse(targetUserId, "SELF", null);
        }

        Optional<ConnectionRequest> connOpt = connectionRepository.findBetweenUsers(currentUser.getId(), targetUserId);

        if (connOpt.isEmpty()) {
            return new ConnectionStatusResponse(targetUserId, "NONE", null);
        }

        ConnectionRequest conn = connOpt.get();
        if (conn.getStatus() == ConnectionStatus.ACCEPTED) {
            return new ConnectionStatusResponse(targetUserId, "CONNECTED", conn.getId());
        } else if (conn.getStatus() == ConnectionStatus.PENDING) {
            if (conn.getSender().getId().equals(currentUser.getId())) {
                return new ConnectionStatusResponse(targetUserId, "REQUEST_SENT", conn.getId());
            } else {
                return new ConnectionStatusResponse(targetUserId, "REQUEST_RECEIVED", conn.getId());
            }
        } else {
            return new ConnectionStatusResponse(targetUserId, "REJECTED", conn.getId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean areUsersConnected(Long user1Id, Long user2Id) {
        return connectionRepository.areConnected(user1Id, user2Id);
    }
}
