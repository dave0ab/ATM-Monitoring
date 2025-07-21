package com.zemenbank.atmMonitoring.services.impl.scheduled;

import com.zemenbank.atmMonitoring.model.atmMonitor.PingStatus;
import com.zemenbank.atmMonitoring.repositories.atmMonitor.PingStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Component
public class PingService {
    private static final Logger logger = LoggerFactory.getLogger(PingService.class);

    @Autowired
    private PingStatusRepository pingStatusRepository;

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    // Method to ping a specific IP and save its status in the database
    public String pingAndSaveStatus(String terminalId, String ipAddress, String type) {
        String status = ping(ipAddress);
        savePingStatus(terminalId, ipAddress, status, type);
        return status;
    }






        public String ping(String ipAddress) {
            List<Boolean> pingResponses = new ArrayList<>();
            boolean unreachable = false;
            boolean adslReplyOnly = false;

            try {
                InetAddress inetAddress = InetAddress.getByName(ipAddress);

                // Attempt to ping the IP multiple times
                for (int i = 0; i < 5; i++) { // Perform 5 pings
                    if (inetAddress.isReachable(1000)) { // 1-second timeout
                        pingResponses.add(true); // Success response
                    } else {
                        pingResponses.add(false); // Failure response
                        unreachable = true; // Mark as unreachable if even one ping fails
                    }
                    Thread.sleep(200); // Small delay between attempts
                }
            } catch (Exception e) {
                return "Line/Power Issue"; // Return error status if an exception occurs
            }

            // Count successful pings
            long successfulPings = pingResponses.stream().filter(Boolean::booleanValue).count();

            // Evaluate results based on response count
            if (successfulPings >= 3) { // Equivalent of "Both Reply"
                return "Both Reply";
            } else if (unreachable && successfulPings == 0) {
                return "Line/Power Issue"; // All pings failed
            } else if (successfulPings > 0 && successfulPings < 3) {
                return "Only ADSL"; // Partial success, infer "Only ADSL"
            } else {
                return "Line/Power Issue"; // Default case
            }
        }




    public String getStatus(String terminalId, String hostIp) {



        String hostStatus = pingAndSaveStatus(terminalId, hostIp.trim(), "Host");
        String modemIp = getModemIp(hostIp.trim());
        String modemStatus = pingAndSaveStatus(terminalId, modemIp.trim(), "Modem");

        String overallStatus;

        if ("Both Reply".equals(hostStatus)) {

            overallStatus = "Both Reply";
        } else if ("Line/Power Issue".equals(hostStatus) && "Both Reply".equals(modemStatus)) {
            overallStatus = "Only ADSL";

            savePingStatus(terminalId, modemIp, overallStatus, "Modem");
        } else if ("Line/Power Issue".equals(hostStatus) && "Line/Power Issue".equals(modemStatus)) {
            overallStatus = "Line/Power Issue";
            if (hostIp.startsWith("172.")) {
                overallStatus = "WTTX (Power Issue)"; // Return the IP as is without any parsing or modification
            }

            savePingStatus(terminalId, hostIp, overallStatus, "Host");
        } else {
            overallStatus = "Line/Power Issue";
            savePingStatus(terminalId, hostIp, overallStatus, "Host");
        }


        return overallStatus;
    }



    public CompletableFuture<String> getStatusAsync(String terminalId, String hostIp) {
        return CompletableFuture.supplyAsync(() -> getStatus(terminalId ,hostIp), executor);
    }



    private String getModemIp(String hostIp) {
        // Check if IP starts with "172."
        if (hostIp.startsWith("172.")) {
            return hostIp; // Return the IP as is without any parsing or modification
        }

        if (hostIp.startsWith("A-NCR")) {
            return hostIp;
        }

        // Continue with existing logic for other cases
        String[] parts = hostIp.split("\\.");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid IP address format: " + hostIp);
        }

        int lastOctet = Integer.parseInt(parts[3]);
        String modemIp = parts[0] + "." + parts[1] + "." + parts[2] + "." + (lastOctet - 1);

        return modemIp;
    }

    public void savePingStatus(String terminalId, String ipAddress, String status, String type) {
        PingStatus existingPingStatus = pingStatusRepository.findByTerminalId(terminalId);

        if (existingPingStatus != null) {
            // Update existing record
            existingPingStatus.setSTATUS(status);
            existingPingStatus.setIP_ADDRESS(ipAddress);  // Update IP address if it has changed
            existingPingStatus.setTIMESTAMP(LocalDateTime.now());
            existingPingStatus.setDETAILS(type);
            pingStatusRepository.save(existingPingStatus);

        } else {
            // Create new record
            PingStatus newPingStatus = new PingStatus();
            newPingStatus.setTerminalId(terminalId);
            newPingStatus.setIP_ADDRESS(ipAddress);
            newPingStatus.setSTATUS(status);
            newPingStatus.setTIMESTAMP(LocalDateTime.now());
            newPingStatus.setDETAILS(type);
            pingStatusRepository.save(newPingStatus);

        }
    }

}
