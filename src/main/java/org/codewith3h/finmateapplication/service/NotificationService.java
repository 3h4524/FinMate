package org.codewith3h.finmateapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.NotificationRequest;
import org.codewith3h.finmateapplication.entity.Notification;
import org.codewith3h.finmateapplication.mapper.NotificationMapper;
import org.codewith3h.finmateapplication.repository.NotificationRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public void createNotification(NotificationRequest dto){
        log.info("Creating notification at {} for {}", dto.getRelatedEntityType(),  dto.getRelatedEntityId());
        Notification notification = notificationMapper.toEntity(dto);
        notificationRepository.save(notification);
    }

}
