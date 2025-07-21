package com.zemenbank.atmMonitoring.dto.response;

import com.zemenbank.atmMonitoring.model.bankworld.GatewayInfo;
import lombok.Data;

import java.util.Map;

@Data
public class GatewayInfoResponse {
    private String systemId;
    private String network;
    private String ignoreRemDesStat;
    private String locDesStat;
    private String remDesStat;
    private Long actualUpTime;
    private Long actualDownTime;
    private Long actualResetTime;
    private String commsStatus;
    private String actualStat;
    private String lineStat;
    private Long lineChangeTime;
    private Long actualChangeTime;
    private String locDesSon;
    private String remDesSon;
    private Long lastIncEchoTime;
    private String instance;
    private String serverAddr;
    private String serverPort;
    private String sourceStationId;
    private  String serverName;
    private double uptimePercentage; // Field to store the uptime percentage

    // Constructor for simplified response creation in stream mapping
    public GatewayInfoResponse(GatewayInfo gatewayInfo, String serverName, String commsStatus, String actualStat, String lineStat) {
        this.systemId = gatewayInfo.getSystemId();
        this.network = gatewayInfo.getNetwork();
        this.ignoreRemDesStat = translateStatus(gatewayInfo.getIgnoreRemDesStat());
        this.locDesStat = translateStatus(gatewayInfo.getLocDesStat());
        this.remDesStat = translateStatus(gatewayInfo.getRemDesStat());
        this.actualUpTime = gatewayInfo.getActualUpTime();
        this.actualDownTime = gatewayInfo.getActualDownTime();
        this.actualResetTime = gatewayInfo.getActualResetTime();
        this.commsStatus = commsStatus;
        this.actualStat = actualStat;
        this.lineStat = lineStat;
        this.lineChangeTime = gatewayInfo.getLineChangeTime();
        this.actualChangeTime = gatewayInfo.getActualChangeTime();
        this.locDesSon = translateStatus(gatewayInfo.getLocDesSon());
        this.remDesSon = translateStatus(gatewayInfo.getRemDesSon());
        this.lastIncEchoTime = gatewayInfo.getLastIncEchoTime();
        this.instance = gatewayInfo.getInstance() != null ? gatewayInfo.getInstance().toString() : null;
        this.serverAddr = gatewayInfo.getServerAddr();
        this.serverPort = gatewayInfo.getServerPort() != null ? gatewayInfo.getServerPort().toString() : null;
        this.sourceStationId = gatewayInfo.getSourceStationId();
        this.serverName = serverName;
        this.uptimePercentage = calculateUptime(gatewayInfo); // Calculate and set uptime percentage
    }

    public GatewayInfoResponse() {

    }

    private String translateStatus(Integer status) {
        if (status == null) {
            return "Undefined";
        }

        return switch (status) {
            case 0 -> "Offline";
            case 1 -> "Online";
            case 2 -> "Maintenance";
            case 3 -> "Sign OFF";
            default -> "Undefined";
        };
    }

    private double calculateUptime(GatewayInfo gateway) {
        long upTime = gateway.getActualUpTime() != null ? gateway.getActualUpTime() : 0;
        long downTime = gateway.getActualDownTime() != null ? gateway.getActualDownTime() : 0;
        long resetTime = gateway.getActualResetTime() != null ? gateway.getActualResetTime() : 0;

        long totalTime = upTime + downTime + resetTime;
        return totalTime > 0 ? ((double) upTime / totalTime) * 100 : 0;
    }






    public GatewayInfoResponse mapGatewayInfoToResponse(GatewayInfo gatewayInfo) {

        // Translate each status field
        String commsStatus = translateStatus(gatewayInfo.getCommsStatus());
        String actualStat = translateStatus(gatewayInfo.getActualStat());
        String lineStat = translateStatus(gatewayInfo.getLineStat() != null ? gatewayInfo.getLineStat() : 3);

        // Map network to server name translation
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

        // Retrieve the translated server name based on the network
        String translatedServerName = networkTranslations.getOrDefault(gatewayInfo.getNetwork(), "Unknown Gateway");

        return new GatewayInfoResponse(
                gatewayInfo,
                translatedServerName,  // Use the translated server name here
                commsStatus,
                actualStat,
                lineStat != null ? lineStat : String.valueOf(0)
        );
    }


    public String getGetwayServerNetwork() {
        return network;
    }


}
