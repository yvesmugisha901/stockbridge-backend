package com.branch.inventory.backend.controller;

import com.branch.inventory.backend.dto.request.CreateItemRequest;
import com.branch.inventory.backend.dto.request.UpdateItemRequest;
import com.branch.inventory.backend.dto.response.ApiResponse;
import com.branch.inventory.backend.dto.response.ItemResponse;
import com.branch.inventory.backend.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * ItemController – manages the master item catalogue.
 * FR-09: Item Catalogue (HO_ADMIN can add/edit/deactivate items)
 * FR-29: Global Search support
 */
@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    /**
     * POST /api/v1/items
     * HO_ADMIN adds a new item to the master catalogue.
     * Fields: name, code, category, unit of measure.
     * FR-09
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('HO_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<ItemResponse>> createItem(
            @Valid @RequestBody CreateItemRequest request) {
        ItemResponse item = itemService.createItem(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(item));
    }

    /**
     * GET /api/v1/items
     * Returns paginated catalogue with optional keyword/category search.
     * FR-09, FR-29
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<ItemResponse>>> getItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            Pageable pageable) {
        Page<ItemResponse> items = itemService.searchItems(keyword, category, pageable);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    /**
     * GET /api/v1/items/{id}
     * Returns details of a specific catalogue item.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ItemResponse>> getItemById(@PathVariable Long id) {
        ItemResponse item = itemService.getItemById(id);
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    /**
     * PUT /api/v1/items/{id}
     * HO_ADMIN edits an existing catalogue item.
     * FR-09
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('HO_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<ItemResponse>> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateItemRequest request) {
        ItemResponse updated = itemService.updateItem(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * PATCH /api/v1/items/{id}/deactivate
     * HO_ADMIN removes an item from active use without deleting it.
     * FR-09
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('HO_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> deactivateItem(@PathVariable Long id) {
        itemService.deactivateItem(id);
        return ResponseEntity.ok(ApiResponse.success("Item deactivated successfully."));
    }
}