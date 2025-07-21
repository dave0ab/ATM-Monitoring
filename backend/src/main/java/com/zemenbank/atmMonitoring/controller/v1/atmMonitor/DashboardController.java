package com.zemenbank.atmMonitoring.controller.v1.atmMonitor;

import com.zemenbank.atmMonitoring.dto.response.DashboardResponse;
import com.zemenbank.atmMonitoring.dto.response.UserResponse;
import com.zemenbank.atmMonitoring.services.DashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Dashboard REST APIs",
        description = "APIs to manage user profiles and related dashboard functionalities"
)
@RestController
@RequestMapping("/v1/dashboard")
@RequiredArgsConstructor  // Lombok automatically generates the constructor
public class DashboardController {

    private final DashboardService dashboardService;  // Lombok will inject this


    @GetMapping("/profile")
    public UserResponse getProfileDetail() {


        return dashboardService.getProfileDetail();

    }

    @GetMapping("/details")
    public DashboardResponse getDashboardDetail() {
        return dashboardService.getDashboardDetail();
    }
}
