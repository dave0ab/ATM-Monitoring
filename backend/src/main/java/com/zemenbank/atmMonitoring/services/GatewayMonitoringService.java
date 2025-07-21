package com.zemenbank.atmMonitoring.services;

import com.zemenbank.atmMonitoring.dto.response.GatewayInfoResponse;
import com.zemenbank.atmMonitoring.model.bankworld.GatewayInfo;

import java.util.List;
import java.util.Optional;

public interface GatewayMonitoringService {
    List<GatewayInfoResponse> getAllGateways();

    double calculateUptime(GatewayInfo gateway);

    Optional<GatewayInfo> getGatewayById(String systemId);
    Integer getUptime(String systemId);
    Integer getDowntime(String systemId);
    GatewayInfo updateGatewayStatus(GatewayInfo gatewayInfo);
}
