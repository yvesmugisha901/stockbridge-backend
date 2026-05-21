package com.branch.inventory.backend.service;

import com.branch.inventory.backend.model.StockLevel;
import com.branch.inventory.backend.model.TransferRequest;
import com.branch.inventory.backend.model.enums.TransferStatus;
import com.branch.inventory.backend.repository.StockLevelRepository;
import com.branch.inventory.backend.repository.TransferRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final StockLevelRepository stockLevelRepository;
    private final TransferRequestRepository transferRequestRepository;

    @Transactional(readOnly = true)
    public Object getStockLevelReport(Long branchId, String category, Long itemId) {
        return stockLevelRepository.findByFiltersForReport(branchId, itemId, category);
    }

    @Transactional(readOnly = true)
    public byte[] exportStockLevelReportCsv(Long branchId, String category, Long itemId) {
        List<StockLevel> stocks = stockLevelRepository.findByFiltersForReport(branchId, itemId, category);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("Branch,Item Code,Item Name,Category,Unit,Qty On Hand,Reserved,Min Threshold,Low Stock");

        for (StockLevel s : stocks) {
            boolean low = s.getQuantityOnHand() <= s.getMinimumThreshold();
            pw.printf("%s,%s,%s,%s,%s,%d,%d,%d,%s%n",
                    s.getBranch().getName(),
                    s.getItem().getCode(),
                    s.getItem().getName(),
                    s.getItem().getCategory(),
                    s.getItem().getUnitOfMeasure(),
                    s.getQuantityOnHand(),
                    s.getReservedQuantity(),
                    s.getMinimumThreshold(),
                    low ? "YES" : "NO");
        }
        return sw.toString().getBytes();
    }

    @Transactional(readOnly = true)
    public Object getTransferHistoryReport(Long branchId, String status,
            Long itemId, String fromDate, String toDate) {
        LocalDate from = fromDate != null ? LocalDate.parse(fromDate) : null;
        LocalDate to = toDate != null ? LocalDate.parse(toDate) : null;
        TransferStatus ts = status != null ? TransferStatus.valueOf(status) : null;
        return transferRequestRepository.findForHistoryReport(branchId, ts, itemId, from, to);
    }

    @Transactional(readOnly = true)
    public byte[] exportTransferHistoryReportCsv(Long branchId, String status,
            Long itemId, String fromDate, String toDate) {
        LocalDate from = fromDate != null ? LocalDate.parse(fromDate) : null;
        LocalDate to = toDate != null ? LocalDate.parse(toDate) : null;
        TransferStatus ts = status != null ? TransferStatus.valueOf(status) : null;

        List<TransferRequest> transfers = transferRequestRepository
                .findForHistoryReport(branchId, ts, itemId, from, to);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("ID,Source Branch,Destination Branch,Item Code,Item Name,Qty,Total Value,Status,Requested At");

        for (TransferRequest t : transfers) {
            pw.printf("%d,%s,%s,%s,%s,%d,%s,%s,%s%n",
                    t.getId(),
                    t.getSourceBranch().getName(),
                    t.getDestinationBranch().getName(),
                    t.getItem().getCode(),
                    t.getItem().getName(),
                    t.getQuantity(),
                    t.getTotalValue(),
                    t.getStatus().name(),
                    t.getRequestedAt());
        }
        return sw.toString().getBytes();
    }

    @Transactional(readOnly = true)
    public Object getLowStockReport(Long branchId, String category) {
        return branchId != null
                ? stockLevelRepository.findLowStockByBranchAndCategory(branchId, category)
                : stockLevelRepository.findAllLowStockByCategory(category);
    }

    @Transactional(readOnly = true)
    public byte[] exportLowStockReportCsv(Long branchId, String category) {
        List<StockLevel> stocks = branchId != null
                ? stockLevelRepository.findLowStockByBranchAndCategory(branchId, category)
                : stockLevelRepository.findAllLowStockByCategory(category);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("Branch,Item Code,Item Name,Category,Qty On Hand,Min Threshold,Shortage");

        for (StockLevel s : stocks) {
            int shortage = s.getMinimumThreshold() - s.getQuantityOnHand();
            pw.printf("%s,%s,%s,%s,%d,%d,%d%n",
                    s.getBranch().getName(),
                    s.getItem().getCode(),
                    s.getItem().getName(),
                    s.getItem().getCategory(),
                    s.getQuantityOnHand(),
                    s.getMinimumThreshold(),
                    shortage);
        }
        return sw.toString().getBytes();
    }
}