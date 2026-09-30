package com.connectly.repository;

import com.connectly.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StatusRepository extends JpaRepository<Status, Long> {
    List<Status> findByUserIdAndExpiresAtAfterOrderByCreatedAtDesc(Long userId, LocalDateTime now);
    List<Status> findByUserIdInAndExpiresAtAfterOrderByCreatedAtDesc(List<Long> userIds, LocalDateTime now);
    Optional<Status> findByIdAndExpiresAtAfter(Long id, LocalDateTime now);
    List<Status> findByExpiresAtBefore(LocalDateTime now);

    @Modifying
    @Query("DELETE FROM Status s WHERE s.expiresAt < :now")
    int deleteExpiredStatuses(@Param("now") LocalDateTime now);
}
