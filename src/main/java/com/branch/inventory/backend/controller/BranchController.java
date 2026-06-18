package com.branch.inventory.backend.controller;

import com.branch.inventory.backend.dto.request.CreateBranchRequest;
import com.branch.inventory.backend.dto.request.UpdateBranchRequest;
import com.branch.inventory.backend.dto.response.ApiResponse;
import com.branch.inventory.backend.dto.response.BranchResponse;
import com.branch.inventory.backend.dto.response.BranchSummaryResponse;
import com.branch.inventory.backend.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(
            @Valid @RequestBody CreateBranchRequest request) {
        BranchResponse branch = branchService.createBranch(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(branch));
    }

    /**
     * GET /api/v1/branches
     * Public — no auth required (used by self-registration dropdown).
     * FR-08
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BranchSummaryResponse>>> getAllBranches() {
        List<BranchSummaryResponse> branches = branchService.getAllBranches();
        return ResponseEntity.ok(ApiResponse.success(branches));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranchById(@PathVariable Long id) {
        BranchResponse branch = branchService.getBranchById(id);
        return ResponseEntity.ok(ApiResponse.success(branch));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBranchRequest request) {
        BranchResponse updated = branchService.updateBranch(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deactivateBranch(@PathVariable Long id) {
        branchService.deactivateBranch(id);
        return ResponseEntity.ok(ApiResponse.success("Branch deactivated successfully."));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> activateBranch(@PathVariable Long id) {
        branchService.activateBranch(id);
        return ResponseEntity.ok(ApiResponse.success("Branch activated successfully."));
    }
}
    