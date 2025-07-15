package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification,Integer> {
    List<Notification> findAllByUserIdAndIsRead(Integer userId, Boolean isRead);
}
