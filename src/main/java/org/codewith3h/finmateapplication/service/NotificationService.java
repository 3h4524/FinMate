package org.codewith3h.finmateapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.response.NotificationResponse;
import org.codewith3h.finmateapplication.entity.Notification;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.mapper.NotificationMapper;
import org.codewith3h.finmateapplication.repository.NotificationRepository;
import org.codewith3h.finmateapplication.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserRepository userRepository;

    public void createNotification(String title, String message, String type, String relatedEntityType, Integer relatedEntityId) {

        Integer userId = Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        User user = userRepository.findById(userId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND));

        log.info("Creating notification at {} for {}", relatedEntityType, relatedEntityId);
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .build();
        notificationRepository.save(notification);
    }

    public void deleteNotification(Integer notificationId) {
        notificationRepository.deleteById(notificationId);
        log.info("Notification deleted successful!");
    }

    public void markAsReadNotification(Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        Objects.requireNonNull(notification).setIsRead(true);
        notificationRepository.save(notification);
        log.info("Notification mark as read successful!");
    }

    public List<NotificationResponse> getAllUnreadNotificationsForUser() {
        int userId = Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        log.info("Getting all notifications for user {}", userId);
        List<Notification> notifications = notificationRepository.findAllByUserIdAndIsRead(userId, false);

        return notifications.stream().map(notificationMapper::toDto).collect(Collectors.toList());
    }

}
