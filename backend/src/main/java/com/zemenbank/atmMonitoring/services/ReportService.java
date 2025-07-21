package com.zemenbank.atmMonitoring.services;

import com.zemenbank.atmMonitoring.dto.response.AtmsReportResponseDTO;
import com.zemenbank.atmMonitoring.model.atmMonitor.AtmStatusReport;

import java.util.List;
import java.util.Optional;

public interface ReportService {


    List<AtmsReportResponseDTO> generateAllAtmsReport();
    String exportAtmsReportToExcel(String type);

    List<AtmStatusReport> getAllAtmStatuses();

    Optional<AtmsReportResponseDTO> getAtmStatusByTerminalId(String terminalId);

    String exportAtmsReportToTxt(String type);
}
