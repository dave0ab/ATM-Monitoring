package com.zemenbank.atmMonitoring.controller.v1.bankworld;

import com.zemenbank.atmMonitoring.dto.response.GatewayInfoResponse;
import com.zemenbank.atmMonitoring.model.bankworld.GatewayInfo;
import com.zemenbank.atmMonitoring.services.GatewayMonitoringService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "Foreclosure REST APIs for Legal Department user",
        description = "REST APIs - Retrieve and Manage ATMs")
@RestController
@RequestMapping("/v1/gateways")
@RequiredArgsConstructor
public class GatewayMonitoringController {

    private final GatewayMonitoringService gatewayMonitoringService;


    // Get all gateways and their statuses
    @GetMapping
    public ResponseEntity<List<GatewayInfoResponse>> getAllGateways() {
        List<GatewayInfoResponse> gateways = gatewayMonitoringService.getAllGateways();
        return ResponseEntity.ok(gateways);
    }

    // Get a specific gateway by its system ID
    @GetMapping("/{systemId}")
    public ResponseEntity<GatewayInfo> getGatewayById(@PathVariable String systemId) {
        Optional<GatewayInfo> gatewayInfo = gatewayMonitoringService.getGatewayById(systemId);
        return gatewayInfo.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get uptime of a specific gateway
    @GetMapping("/{systemId}/uptime")
    public ResponseEntity<Integer> getGatewayUptime(@PathVariable String systemId) {
        Integer uptime = gatewayMonitoringService.getUptime(systemId);
        return ResponseEntity.ok(uptime);
    }

    // Get downtime of a specific gateway
    @GetMapping("/{systemId}/downtime")
    public ResponseEntity<Integer> getGatewayDowntime(@PathVariable String systemId) {
        Integer downtime = gatewayMonitoringService.getDowntime(systemId);
        return ResponseEntity.ok(downtime);
    }

    // Update gateway status (useful for monitoring updates)
    @PutMapping("/{systemId}")
    public ResponseEntity<GatewayInfo> updateGatewayStatus(@PathVariable String systemId,
                                                           @RequestBody GatewayInfo gatewayInfo) {
        if (!systemId.equals(gatewayInfo.getSystemId())) {
            return ResponseEntity.badRequest().build();
        }
        GatewayInfo updatedGateway = gatewayMonitoringService.updateGatewayStatus(gatewayInfo);
        return ResponseEntity.ok(updatedGateway);
    }
}
