package com.zemenbank.atmMonitoring.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardResponse {

    private String totalAtms;
    private String atmsOnline;
    private String atmsWithIssues;
    private String activeAlerts;
}

