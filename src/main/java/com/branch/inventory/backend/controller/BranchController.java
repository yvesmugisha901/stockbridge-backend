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

/**
 * BranchController – manages branch records.
 * FR-06: Create Branch
 * FR-07: Edit / Deactivate Branch
 * FR-08: Branch Overview
 */
@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    /**
     * POST /api/v1/branches
     * Admin creates a new branch with name, location, code, and contact info.
     * FR-06
     */
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
     * Returns all branches with their current stock summaries.
     * Accessible by HO_ADMIN and ADMIN.
     * FR-08
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HO_ADMIN')")
    public ResponseEntity<ApiResponse<List<BranchSummaryResponse>>> getAllBranches() {
        List<BranchSummaryResponse> branches = branchService.getAllBranches();
        return ResponseEntity.ok(ApiResponse.success(branches));
    }

    /**
     * GET /api/v1/branches/{id}
     * Returns details of a specific branch.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HO_ADMIN')")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranchById(@PathVariable Long id) {
        BranchResponse branch = branchService.getBranchById(id);
        return ResponseEntity.ok(ApiResponse.success(branch));
    }

    /**
     * PUT /api/v1/branches/{id}
     * Admin updates branch details.
     * FR-07
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBranchRequest request) {
        BranchResponse updated = branchService.updateBranch(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * PATCH /api/v1/branches/{id}/deactivate
     * Admin marks a branch as inactive.
     * FR-07
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deactivateBranch(@PathVariable Long id) {
        branchService.deactivateBranch(id);
        return ResponseEntity.ok(ApiResponse.success("Branch deactivated successfully."));
    }

    /**
     * PATCH /api/v1/branches/{id}/activate
     * Admin re-activates an inactive branch.
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> activateBranch(@PathVariable Long id) {
        branchService.activateBranch(id);
        return ResponseEntity.ok(ApiResponse.success("Branch activated successfully."));
    }
}