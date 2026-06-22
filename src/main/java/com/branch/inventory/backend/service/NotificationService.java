package com.branch.inventory.backend.service;

import com.branch.inventory.backend.dto.response.NotificationResponse;
import com.branch.inventory.backend.model.Notification;
import com.branch.inventory.backend.model.User;
import com.branch.inventory.backend.repository.NotificationRepository;
import com.branch.inventory.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public List<NotificationResponse> getForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    public long getUnreadCount(Long userId) { // ← added {
        return notificationRepository.countUnreadByUserId(userId); // ← was "user )"
    } // ← added }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllReadByUserId(userId);
    }

    @Transactional
    public void markRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void send(Long userId, String title, String message, String type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        notificationRepository.save(Notification.builder()
                .user(user).title(title).message(message).type(type).read(false)
                .build());
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId()).title(n.getTitle()).message(n.getMessage())
                .type(n.getType()).read(n.isRead()).createdAt(n.getCreatedAt())
                .build();
    }
}