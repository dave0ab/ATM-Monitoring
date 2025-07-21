package com.zemenbank.atmMonitoring.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AtmsReportResponseDTO {

    private String terminalId;
    private String atmName;
    private String status;
    private String problem;      // New field for problem
    private String cashBalance;

    private String registeredAt;
    private String totalDays;
}
