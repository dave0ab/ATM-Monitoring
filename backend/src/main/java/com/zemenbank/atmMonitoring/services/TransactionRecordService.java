package com.zemenbank.atmMonitoring.services;

import com.zemenbank.atmMonitoring.dto.response.AtmsResponse;
import com.zemenbank.atmMonitoring.dto.response.DashboardResponse;
import com.zemenbank.atmMonitoring.dto.response.TransactionRecordResponse;
import com.zemenbank.atmMonitoring.dto.response.UserResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRecordService {


    Page<TransactionRecordResponse> getAllTransactions(
            int pageNumber,
            int pageSize,
            String orderStatus,
            String search,
            String brand,
            String transactionType,
            String currency,
            LocalDate  startDate,  // 👈 new
            LocalDate endDate
    );

    List<TransactionRecordResponse> getTransactionsByOrderId(String orderId);
}
