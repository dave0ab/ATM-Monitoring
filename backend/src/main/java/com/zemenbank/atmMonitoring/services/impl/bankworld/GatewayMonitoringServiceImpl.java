package com.zemenbank.atmMonitoring.services.impl.bankworld;

import com.zemenbank.atmMonitoring.dto.response.GatewayInfoResponse;
import com.zemenbank.atmMonitoring.model.bankworld.GatewayInfo;
import com.zemenbank.atmMonitoring.services.GatewayCacheService;
import com.zemenbank.atmMonitoring.services.GatewayMonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GatewayMonitoringServiceImpl implements GatewayMonitoringService {

    private final GatewayCacheService gatewayCacheService;


    public GatewayMonitoringServiceImpl(GatewayCacheService gatewayCacheService) {
        this.gatewayCacheService = gatewayCacheService;
    }

    @Override
    public List<GatewayInfoResponse> getAllGateways() {
        List<String> allowedNetworks = Arrays.asList("sps", "vsa", "sar", "sms", "svf", "mds", "mcb", "mb1");
        Map<String, String> networkTranslations = Map.of(
                "sps", "Sparrow POS Gateway",
                "svf", "SVF Gateway",
                "mcb", "MCB Gateway",
                "sms", "Visa SMS",
                "vsa", "Visa Base Issuer",
                "sar", "ATM Gateway",
                "mds", "MDS Gateway",
                "mb1", "Member Bank 1"
        );

        return gatewayCacheService.findAll().stream()
                .filter(gateway -> allowedNetworks.contains(gateway.getNetwork()))
                .map(gateway -> new GatewayInfoResponse(
                        gateway,
                        networkTranslations.getOrDefault(gateway.getNetwork(), "Unknown Gateway"),
                        translateStatus(gateway.getCommsStatus()),
                        translateStatus(gateway.getActualStat()),
                        translateStatus(gateway.getLineStat() != null ? gateway.getLineStat() : 3)
                ))
                .distinct()
                .collect(Collectors.toList());
    }

    private String translateStatus(int status) {
        return switch (status) {
            case 0 -> "Offline";
            case 1 -> "Online";
            case 2 -> "Maintenance";
            case 3 -> "Sign OFF";
            default -> "Sign OFF";
        };
    }

    @Override
    public double calculateUptime(GatewayInfo gateway) {
        long actualUpTime = Optional.ofNullable(gateway.getActualUpTime()).orElse(0L);
        long actualDownTime = Optional.ofNullable(gateway.getActualDownTime()).orElse(0L);
        long actualResetTime = Optional.ofNullable(gateway.getActualResetTime()).orElse(0L);

        long totalTime = actualUpTime + actualDownTime + actualResetTime;
        return totalTime > 0 ? ((double) actualUpTime / totalTime) * 100 : 0;
    }

    @Override
    public Optional<GatewayInfo> getGatewayById(String network) {
        return gatewayCacheService.findByNetwork(network);
    }

    @Override
    public Integer getUptime(String network) {
        return gatewayCacheService.getUptime(network);
    }

    @Override
    public Integer getDowntime(String network) {
        return gatewayCacheService.getDowntime(network);
    }

    @Override
    public GatewayInfo updateGatewayStatus(GatewayInfo gatewayInfo) {
        return gatewayCacheService.updateGatewayStatus(gatewayInfo);
    }
}
