    package com.branch.inventory.backend.controller;

    import com.branch.inventory.backend.dto.response.ApiResponse;
    import com.branch.inventory.backend.dto.response.NotificationResponse;
    import com.branch.inventory.backend.model.User;
    import com.branch.inventory.backend.repository.UserRepository;
    import com.branch.inventory.backend.service.NotificationService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.core.annotation.AuthenticationPrincipal;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.web.bind.annotation.*;
    import java.util.List;
    import java.util.Map;

    @RestController
    @RequestMapping("/api/v1/notifications")
    @RequiredArgsConstructor
    public class NotificationController {

        private final NotificationService notificationService;
        private final UserRepository userRepository;

        @GetMapping
        public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAll(
                @AuthenticationPrincipal UserDetails userDetails) {
            Long userId = resolveId(userDetails);
            return ResponseEntity.ok(ApiResponse.success(notificationService.getForUser(userId)));
        }

        @GetMapping("/unread-count")
        public ResponseEntity<ApiResponse<Map<String, Long>>> unreadCount(
                @AuthenticationPrincipal UserDetails userDetails) {
            Long count = notificationService.getUnreadCount(resolveId(userDetails));
            return ResponseEntity.ok(ApiResponse.success(Map.of("count", count)));
        }

        @PatchMapping("/mark-all-read")
        public ResponseEntity<ApiResponse<String>> markAllRead(
                @AuthenticationPrincipal UserDetails userDetails) {
            notificationService.markAllRead(resolveId(userDetails));
            return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
        }

        @PatchMapping("/{id}/read")
        public ResponseEntity<ApiResponse<String>> markRead(@PathVariable Long id) {
            notificationService.markRead(id);
            return ResponseEntity.ok(ApiResponse.success("Notification marked as read"));
        }

        private Long resolveId(UserDetails userDetails) {
            return userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found")).getId();
        }
    }