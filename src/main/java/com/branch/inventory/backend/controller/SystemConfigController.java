package com.branch.inventory.backend.controller;

import com.branch.inventory.backend.dto.request.UpdateSystemConfigRequest;
import com.branch.inventory.backend.dto.response.ApiResponse;
import com.branch.inventory.backend.dto.response.SystemConfigResponse;
import com.branch.inventory.backend.service.SystemConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SystemConfigController {

    private final SystemConfigService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SystemConfigResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(service.getAll()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SystemConfigResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSystemConfigRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request)));
    }
}