package org.codewith3h.finmateapplication.controller;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.NotificationResponse;
import org.codewith3h.finmateapplication.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor()
public class NotificationController {
    NotificationService notificationService;

    @GetMapping()
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAllUnreadNotificationsForUser(){
        List<NotificationResponse> listNotifications = notificationService.getAllUnreadNotificationsForUser();
        ApiResponse<List<NotificationResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Fetch all notifications for user successfully");
        apiResponse.setResult(listNotifications);
        return ResponseEntity.ok(apiResponse);
    }

    @PatchMapping("/{notification_id}")
    public ResponseEntity<Void> markAsReadNotification(@PathVariable Integer notification_id){
        notificationService.markAsReadNotification(notification_id);
        return ResponseEntity.noContent().build();
    }
}
