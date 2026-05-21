package com.branch.inventory.backend.service;

import com.branch.inventory.backend.dto.request.CreateItemRequest;
import com.branch.inventory.backend.dto.request.UpdateItemRequest;
import com.branch.inventory.backend.dto.response.ItemResponse;
import com.branch.inventory.backend.model.Item;
import com.branch.inventory.backend.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public ItemResponse createItem(CreateItemRequest request) {
        if (itemRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Item code already exists: " + request.getCode());
        }

        Item item = Item.builder()
                .name(request.getName())
                .code(request.getCode())
                .category(request.getCategory())
                .unitOfMeasure(request.getUnitOfMeasure())
                .unitPrice(request.getUnitPrice())
                .active(true)
                .build();

        return mapToResponse(itemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public Page<ItemResponse> searchItems(String keyword, String category, Pageable pageable) {
        if (keyword != null && category != null) {
            return itemRepository
                    .findByNameContainingIgnoreCaseAndCategoryAndActiveTrue(keyword, category, pageable)
                    .map(this::mapToResponse);
        } else if (keyword != null) {
            return itemRepository
                    .findByNameContainingIgnoreCaseAndActiveTrue(keyword, pageable)
                    .map(this::mapToResponse);
        } else if (category != null) {
            return itemRepository
                    .findByCategoryAndActiveTrue(category, pageable)
                    .map(this::mapToResponse);
        }
        return itemRepository.findByActiveTrue(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ItemResponse getItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found: " + id));
        return mapToResponse(item);
    }

    @Transactional
    public ItemResponse updateItem(Long id, UpdateItemRequest request) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found: " + id));

        if (request.getName() != null)
            item.setName(request.getName());
        if (request.getCategory() != null)
            item.setCategory(request.getCategory());
        if (request.getUnitOfMeasure() != null)
            item.setUnitOfMeasure(request.getUnitOfMeasure());
        if (request.getUnitPrice() != null)
            item.setUnitPrice(request.getUnitPrice());

        return mapToResponse(itemRepository.save(item));
    }

    @Transactional
    public void deactivateItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found: " + id));
        item.setActive(false);
        itemRepository.save(item);
    }

    private ItemResponse mapToResponse(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .code(item.getCode())
                .category(item.getCategory())
                .unitOfMeasure(item.getUnitOfMeasure())
                .unitPrice(item.getUnitPrice())
                .active(item.isActive())
                .build();
    }
}