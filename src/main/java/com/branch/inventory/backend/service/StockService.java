package com.branch.inventory.backend.service;

import com.branch.inventory.backend.dto.request.StockAdjustmentRequest;
import com.branch.inventory.backend.dto.response.LowStockAlertResponse;
import com.branch.inventory.backend.dto.response.StockLevelResponse;
import com.branch.inventory.backend.model.Branch;
import com.branch.inventory.backend.model.Item;
import com.branch.inventory.backend.model.StockLevel;
import com.branch.inventory.backend.model.User;
import com.branch.inventory.backend.model.enums.Role;
import com.branch.inventory.backend.repository.BranchRepository;
import com.branch.inventory.backend.repository.ItemRepository;
import com.branch.inventory.backend.repository.StockLevelRepository;
import com.branch.inventory.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {

        private final StockLevelRepository stockLevelRepository;
        private final UserRepository userRepository;
        private final BranchRepository branchRepository;
        private final ItemRepository itemRepository;
        private final AuditLogService auditLogService;

        @Transactional(readOnly = true)
        public Page<StockLevelResponse> getStock(String email, Long branchId,
                        Long itemId, String category, Pageable pageable) {
                User user = getUser(email);
                Long resolvedBranchId = resolveVisibleBranchId(user, branchId);
                return stockLevelRepository
                                .findByFilters(resolvedBranchId, itemId, category, pageable)
                                .map(this::mapToResponse);
        }

        @Transactional(readOnly = true)
        public List<StockLevelResponse> getStockByBranch(String email, Long branchId) {
                User user = getUser(email);
                Long resolvedBranchId = resolveVisibleBranchId(user, branchId);
                return stockLevelRepository.findByBranchId(resolvedBranchId)
                                .stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public StockLevelResponse getStockDetail(String email, Long branchId, Long itemId) {
                User user = getUser(email);
                resolveVisibleBranchId(user, branchId);
                StockLevel stock = stockLevelRepository.findByBranchIdAndItemId(branchId, itemId)
                                .orElseThrow(() -> new RuntimeException("Stock record not found"));
                return mapToResponse(stock);
        }

        @Transactional(readOnly = true)
        public List<LowStockAlertResponse> getLowStockAlerts(Long branchId) {
                List<StockLevel> lowStock = branchId != null
                                ? stockLevelRepository.findLowStockByBranch(branchId)
                                : stockLevelRepository.findAllLowStock();

                return lowStock.stream()
                                .map(s -> LowStockAlertResponse.builder()
                                                .branchId(s.getBranch().getId())
                                                .branchName(s.getBranch().getName())
                                                .itemId(s.getItem().getId())
                                                .itemName(s.getItem().getName())
                                                .itemCode(s.getItem().getCode())
                                                .quantityOnHand(s.getQuantityOnHand())
                                                .minimumThreshold(s.getMinimumThreshold())
                                                .deficit(s.getMinimumThreshold() - s.getQuantityOnHand())
                                                .build())
                                .collect(Collectors.toList());
        }

        @Transactional
        public StockLevelResponse adjustStock(StockAdjustmentRequest request, String email) {
                StockLevel stock = stockLevelRepository
                                .findByBranchIdAndItemId(request.getBranchId(), request.getItemId())
                                .orElseGet(() -> createStockRecord(request.getBranchId(), request.getItemId()));

                int previousQty = stock.getQuantityOnHand();
                stock.setQuantityOnHand(previousQty + request.getAdjustmentQuantity());
                stock.setLastUpdated(LocalDateTime.now());
                stockLevelRepository.save(stock);

                auditLogService.log(
                                "STOCK_ADJUSTMENT", "StockLevel", stock.getId(), email,
                                String.format("Adjusted from %d to %d. Reason: %s",
                                                previousQty, stock.getQuantityOnHand(), request.getReason()));

                return mapToResponse(stock);
        }

        private Long resolveVisibleBranchId(User user, Long requestedBranchId) {
                boolean isGlobalViewer = user.getRole() == Role.HO_ADMIN
                                || user.getRole() == Role.ADMIN
                                || user.getRole() == Role.ACCOUNTANT;

                if (!isGlobalViewer) {
                        if (user.getBranch() == null) {
                                throw new RuntimeException("User is not assigned to a branch");
                        }
                        return user.getBranch().getId();
                }
                return requestedBranchId;
        }

        private StockLevel createStockRecord(Long branchId, Long itemId) {
                Branch branch = branchRepository.findById(branchId)
                                .orElseThrow(() -> new RuntimeException("Branch not found: " + branchId));
                Item item = itemRepository.findById(itemId)
                                .orElseThrow(() -> new RuntimeException("Item not found: " + itemId));

                return StockLevel.builder()
                                .branch(branch)
                                .item(item)
                                .quantityOnHand(0)
                                .reservedQuantity(0)
                                .minimumThreshold(0)
                                .lastUpdated(LocalDateTime.now())
                                .build();
        }

        private User getUser(String email) {
                return userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        }

        private StockLevelResponse mapToResponse(StockLevel s) {
                return StockLevelResponse.builder()
                                .id(s.getId())
                                .branchId(s.getBranch().getId())
                                .branchName(s.getBranch().getName())
                                .itemId(s.getItem().getId())
                                .itemName(s.getItem().getName())
                                .itemCode(s.getItem().getCode())
                                .quantityOnHand(s.getQuantityOnHand())
                                .reservedQuantity(s.getReservedQuantity())
                                .minimumThreshold(s.getMinimumThreshold())
                                .isLowStock(s.getQuantityOnHand() <= s.getMinimumThreshold())
                                .lastUpdated(s.getLastUpdated())
                                .build();
        }
}