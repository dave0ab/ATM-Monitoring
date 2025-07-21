package com.zemenbank.atmMonitoring.controller.v1.atmMonitor;

import com.zemenbank.atmMonitoring.dto.response.TransactionRecordResponse;
import com.zemenbank.atmMonitoring.services.TransactionRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Tag(
        name = "Transaction REST APIs",
        description = "APIs to manage user profiles and related dashboard functionalities"
)
@RestController
@RequestMapping("/v1/MPGS_Transactions")
@RequiredArgsConstructor
public class TransactionRecordController {

    private final TransactionRecordService transactionRecordService;

    @Operation(summary = "Fetch all MPGS transactions with pagination and filters")
    @GetMapping()
    public Page<TransactionRecordResponse> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String orderStatus,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) LocalDate  startDate,  // 👈 new
            @RequestParam(required = false) LocalDate endDate
    ) {
        return transactionRecordService.getAllTransactions(
                page, size, orderStatus, search, brand, transactionType, currency ,startDate ,endDate
        );
    }


    @GetMapping("/{orderId}")
    public ResponseEntity<List<TransactionRecordResponse>> getByOrderId(@PathVariable String orderId) {
        List<TransactionRecordResponse> responses = transactionRecordService.getTransactionsByOrderId(orderId);
        if (responses.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(responses);
    }


}
