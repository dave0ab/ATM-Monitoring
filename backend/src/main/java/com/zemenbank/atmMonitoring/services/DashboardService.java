package com.zemenbank.atmMonitoring.services;

import com.zemenbank.atmMonitoring.dto.response.DashboardResponse;
import com.zemenbank.atmMonitoring.dto.response.UserResponse;
import com.zemenbank.atmMonitoring.enums.Category;
import com.zemenbank.atmMonitoring.model.atmMonitor.Tips;

import java.util.List;

public interface DashboardService {

    UserResponse getProfileDetail();

    DashboardResponse getDashboardDetail();
}
