package com.branch.inventory.backend.service;

import com.branch.inventory.backend.model.enums.TransferStatus;
import com.branch.inventory.backend.repository.BranchRepository;
import com.branch.inventory.backend.repository.ItemRepository;
import com.branch.inventory.backend.repository.TransferRequestRepository;
import com.branch.inventory.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final ItemRepository itemRepository;
    private final TransferRequestRepository transferRequestRepository;

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // ── Users ────────────────────────────────────────────────────────────
        stats.put("totalUsers", userRepository.count());

        LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        stats.put("newUsersThisMonth", userRepository.countByCreatedAtAfter(startOfMonth));

        // ── Branches ─────────────────────────────────────────────────────────
        stats.put("activeBranches", (long) branchRepository.findByActiveTrue().size());

        // ── Inventory items ──────────────────────────────────────────────────
        stats.put("activeItems", (long) itemRepository.findByActiveTrue().size());
        stats.put("lowStockCount", 0L); // placeholder until reorder_point is added

        // ── Transfers ────────────────────────────────────────────────────────
        stats.put("totalTransfers", transferRequestRepository.count());

        long pending = transferRequestRepository
                .findByStatusIn(List.of(TransferStatus.PENDING, TransferStatus.MANAGER_APPROVED))
                .size();
        stats.put("pendingTransfers", pending);

        // ── System ───────────────────────────────────────────────────────────
        stats.put("systemStatus", "OK");

        return stats;
    }

    public List<Map<String, Object>> getRecentActivity() {
        // PageRequest.of(page, size) — first 20 rows, no LIMIT parameter needed
        return transferRequestRepository.findRecentActivity(PageRequest.of(0, 20));
    }
}
