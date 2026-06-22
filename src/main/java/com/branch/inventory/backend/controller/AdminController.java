package com.branch.inventory.backend.controller;

import com.branch.inventory.backend.dto.response.ApiResponse;
import com.branch.inventory.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getStats()));
    }

    @GetMapping("/activity")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getActivity() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getRecentActivity()));
    }
}