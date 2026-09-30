package com.connectly.repository;

import com.connectly.entity.StatusView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StatusViewRepository extends JpaRepository<StatusView, Long> {
    List<StatusView> findByStatusId(Long statusId);
    List<StatusView> findByStatusIdOrderByViewedAtDesc(Long statusId);
    Optional<StatusView> findByStatusIdAndViewerId(Long statusId, Long viewerId);
    boolean existsByStatusIdAndViewerId(Long statusId, Long viewerId);
    long countByStatusId(Long statusId);

    @Modifying
    @Query("DELETE FROM StatusView sv WHERE sv.status.id = :statusId")
    void deleteByStatusId(@Param("statusId") Long statusId);

    @Modifying
    @Query("DELETE FROM StatusView sv WHERE sv.status.id IN (SELECT s.id FROM Status s WHERE s.expiresAt < :now)")
    int deleteViewsForExpiredStatuses(@Param("now") java.time.LocalDateTime now);
}
