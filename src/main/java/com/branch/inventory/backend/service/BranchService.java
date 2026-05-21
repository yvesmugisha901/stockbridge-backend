package com.branch.inventory.backend.service;

import com.branch.inventory.backend.dto.request.CreateBranchRequest;
import com.branch.inventory.backend.dto.request.UpdateBranchRequest;
import com.branch.inventory.backend.dto.response.BranchResponse;
import com.branch.inventory.backend.dto.response.BranchSummaryResponse;
import com.branch.inventory.backend.model.Branch;
import com.branch.inventory.backend.repository.BranchRepository;
import com.branch.inventory.backend.repository.StockLevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;
    private final StockLevelRepository stockLevelRepository;

    @Transactional
    public BranchResponse createBranch(CreateBranchRequest request) {
        if (branchRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Branch code already exists: " + request.getCode());
        }

        Branch branch = Branch.builder()
                .name(request.getName())
                .code(request.getCode())
                .location(request.getLocation())
                .contactInfo(request.getContactInfo())
                .active(true)
                .build();

        return mapToResponse(branchRepository.save(branch));
    }

    @Transactional(readOnly = true)
    public List<BranchSummaryResponse> getAllBranches() {
        return branchRepository.findAll().stream()
                .map(branch -> BranchSummaryResponse.builder()
                        .id(branch.getId())
                        .name(branch.getName())
                        .code(branch.getCode())
                        .location(branch.getLocation())
                        .contactInfo(branch.getContactInfo())
                        .active(branch.isActive())
                        .totalItems((int) stockLevelRepository.countDistinctItemsByBranch(branch.getId())) // cast long
                                                                                                           // -> int
                        .lowStockCount((int) stockLevelRepository.countLowStockByBranch(branch.getId())) // cast long ->
                                                                                                         // int
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BranchResponse getBranchById(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found: " + id));
        return mapToResponse(branch);
    }

    @Transactional
    public BranchResponse updateBranch(Long id, UpdateBranchRequest request) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found: " + id));

        if (request.getName() != null)
            branch.setName(request.getName());
        if (request.getLocation() != null)
            branch.setLocation(request.getLocation());
        if (request.getContactInfo() != null)
            branch.setContactInfo(request.getContactInfo());

        return mapToResponse(branchRepository.save(branch));
    }

    @Transactional
    public void deactivateBranch(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found: " + id));
        branch.setActive(false);
        branchRepository.save(branch);
    }

    @Transactional
    public void activateBranch(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found: " + id));
        branch.setActive(true);
        branchRepository.save(branch);
    }

    private BranchResponse mapToResponse(Branch branch) {
        return BranchResponse.builder()
                .id(branch.getId())
                .name(branch.getName())
                .code(branch.getCode())
                .location(branch.getLocation())
                .contactInfo(branch.getContactInfo())
                .active(branch.isActive())
                .build();
    }
}