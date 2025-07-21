package com.zemenbank.atmMonitoring.services;

import com.zemenbank.atmMonitoring.dto.response.AtmsResponse;
import com.zemenbank.atmMonitoring.dto.response.GatewayInfoResponse;
import com.zemenbank.atmMonitoring.model.bankworld.ATMS;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public interface ATMCacheService {
    Optional<ATMS> findById(Long atmId);
    Page<ATMS> getAllAtms(Pageable pageable);
    Page<ATMS> getAtmsByStatus(String status, Pageable pageable);
    Page<ATMS> getAtmsByDeviceName(String deviceName, Pageable pageable);
    Page<ATMS> searchAtms(String search, Pageable pageable);
    Page<ATMS> getAtmsByTIDs(List<String> tids, Pageable pageable);
}
