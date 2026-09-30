package com.connectly.repository;

import com.connectly.constant.ConnectionStatus;
import com.connectly.entity.ConnectionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConnectionRequestRepository extends JpaRepository<ConnectionRequest, Long> {

    List<ConnectionRequest> findByReceiverIdAndStatusOrderByCreatedAtDesc(Long receiverId, ConnectionStatus status);

    List<ConnectionRequest> findBySenderIdAndStatusOrderByCreatedAtDesc(Long senderId, ConnectionStatus status);

    @Query("SELECT cr FROM ConnectionRequest cr WHERE " +
           "(cr.sender.id = :userId OR cr.receiver.id = :userId) AND cr.status = 'ACCEPTED' " +
           "ORDER BY cr.updatedAt DESC")
    List<ConnectionRequest> findAcceptedConnections(@Param("userId") Long userId);

    @Query("SELECT cr FROM ConnectionRequest cr WHERE " +
           "(cr.sender.id = :user1 AND cr.receiver.id = :user2) OR " +
           "(cr.sender.id = :user2 AND cr.receiver.id = :user1)")
    Optional<ConnectionRequest> findBetweenUsers(@Param("user1") Long user1, @Param("user2") Long user2);

    @Query("SELECT COUNT(cr) > 0 FROM ConnectionRequest cr WHERE " +
           "((cr.sender.id = :user1 AND cr.receiver.id = :user2) OR (cr.sender.id = :user2 AND cr.receiver.id = :user1)) " +
           "AND cr.status = 'ACCEPTED'")
    boolean areConnected(@Param("user1") Long user1, @Param("user2") Long user2);

    @Query("SELECT CASE WHEN cr.sender.id = :userId THEN cr.receiver.id ELSE cr.sender.id END " +
           "FROM ConnectionRequest cr WHERE " +
           "(cr.sender.id = :userId OR cr.receiver.id = :userId) AND cr.status = 'ACCEPTED'")
    List<Long> findConnectedUserIds(@Param("userId") Long userId);
}
