package com.zemenbank.atmMonitoring.repositories.atmMonitor;



import com.zemenbank.atmMonitoring.dto.response.AtmsReportResponseDTO;
import com.zemenbank.atmMonitoring.model.atmMonitor.AtmStatusReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AtmStatusReportRepository extends JpaRepository<AtmStatusReport, Long> {

    Optional<AtmStatusReport> findByTerminalId(String terminalId);


}