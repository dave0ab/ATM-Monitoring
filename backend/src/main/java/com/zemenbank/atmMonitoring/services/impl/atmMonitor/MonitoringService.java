package com.zemenbank.atmMonitoring.services.impl.atmMonitor;

import com.zemenbank.atmMonitoring.dto.response.AtmsReportResponseDTO;

import com.zemenbank.atmMonitoring.enums.EscalationLevels;
import com.zemenbank.atmMonitoring.model.atmMonitor.ATMCashBalance;
import com.zemenbank.atmMonitoring.model.atmMonitor.AtmMonitorStatus;
import com.zemenbank.atmMonitoring.model.atmMonitor.GatewayMonitorStatus;
import com.zemenbank.atmMonitoring.model.atmMonitor.PingStatus;
import com.zemenbank.atmMonitoring.model.bankworld.ATMS;
import com.zemenbank.atmMonitoring.model.bankworld.ATM_CASH;
import com.zemenbank.atmMonitoring.model.bankworld.GatewayInfo;
import com.zemenbank.atmMonitoring.repositories.atmMonitor.ATMCashBalanceRepository;
import com.zemenbank.atmMonitoring.repositories.atmMonitor.GatewayStatusReportRepository;
import com.zemenbank.atmMonitoring.repositories.atmMonitor.MonitorStatusRepository;
import com.zemenbank.atmMonitoring.repositories.atmMonitor.PingStatusRepository;
import com.zemenbank.atmMonitoring.repositories.bankworld.ATMCashRepository;
import com.zemenbank.atmMonitoring.repositories.bankworld.ATMSRepository;
import com.zemenbank.atmMonitoring.repositories.bankworld.GatewayInfoRepository;
import com.zemenbank.atmMonitoring.services.ATMCacheService;
import com.zemenbank.atmMonitoring.services.BaseServices;
import com.zemenbank.atmMonitoring.services.MailService;
import com.zemenbank.atmMonitoring.services.impl.cache.GatewayCacheServiceImpl;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class MonitoringService extends BaseServices {

    private final ATMSRepository atmsRepository;
    private final PingStatusRepository pingStatusRepository;
    private final ATMCashRepository atmCashRepository;
    private final MonitorStatusRepository MonitorStatusRepository;
    private final ATMCacheService atmCacheService;

    private final GatewayStatusReportRepository gatewayStatusReportRepository;
    private ATMCashBalanceRepository atmsCashBalanceRepository;

    private GatewayCacheServiceImpl  gatewayCacheServiceImpl;
    private final MailService mailService;

    public MonitoringService(ATMCacheService atmCacheService,
                             GatewayStatusReportRepository gatewayStatusReportRepository,
                             MailService mailService,
                             ATMCashRepository atmCashRepository,
                             MonitorStatusRepository MonitorStatusRepository,
                             ATMSRepository atmsRepository,
                             PingStatusRepository pingStatusRepository,
                             ATMCashBalanceRepository atmsCashBalanceRepository,GatewayCacheServiceImpl  gatewayCacheServiceImpl) {

        this.atmCacheService = atmCacheService;
        this.atmCashRepository = atmCashRepository;
        this.MonitorStatusRepository = MonitorStatusRepository;
        this.atmsRepository = atmsRepository;
        this.pingStatusRepository = pingStatusRepository;
        this.mailService = mailService;
        this.atmsCashBalanceRepository = atmsCashBalanceRepository;
        this.gatewayCacheServiceImpl =gatewayCacheServiceImpl;
        this.gatewayStatusReportRepository=gatewayStatusReportRepository;
    }

    @Scheduled(fixedRate = 5000)
    public List<AtmsReportResponseDTO> monitorAndSave() throws IOException {
        Set<String> processedAtmIds = new HashSet<>();
        List<AtmsReportResponseDTO> reportResponses = new ArrayList<>();

        // Step 1: Process ATMs by Ping Status
        processPingStatuses(List.of("Line/Power Issue", "Only ADSL"), processedAtmIds);
        processPingStatuses(Collections.singletonList("Both Reply"), processedAtmIds);

        // Step 2: Process ATMs with low cash
        processLowCashAtms(processedAtmIds);

        // Step 3: Mark remaining ATMs as "No Issue"
        processRemainingAtms(processedAtmIds);

        return reportResponses;
    }

    private void processPingStatuses(List<String> statuses, Set<String> processedAtmIds) {
        List<PingStatus> pingStatuses = pingStatusRepository.findBySTATUSIn(statuses);
        for (PingStatus pingStatus : pingStatuses) {
            String atmId = pingStatus.getTerminalId();

            if (processedAtmIds.add(atmId)) {
                atmsRepository.findByAtmId(atmId).ifPresent(atm -> {
                    String status = determineStatusForPingStatus(atm, pingStatus);
                    savemonitor(atm, status);
                });
            }
        }
    }

    private String determineStatusForPingStatus(ATMS atm, PingStatus pingStatus) {
        if ("Both Reply".equals(pingStatus.getSTATUS())) {
            switch (atm.getCommsState()) {
                case 0:
                    return "Offline / Both Reply";
                case 3:
                case 2:
                    return "Unknown / Both Reply";
                case 1:
                    // Step 1: Evaluate device problem status
                    String deviceProblemStatus = evaluateDeviceProblemStatus(atm);
                    if (deviceProblemStatus != null) {
                        return deviceProblemStatus;
                    }

                    // Step 2: Evaluate operational state
                    String operationalState = evaluateOperationalState(atm);

                    return operationalState != null ? operationalState : "No Issue";
                default:
                    return "No Issue";
            }
        }
        return pingStatus.getSTATUS();
    }


    private String evaluateDeviceProblemStatus(ATMS atm) {
        Optional<ATMS> atmsOpt = atmsRepository.findByAtmId(atm.getAtmId());
        if (atmsOpt.isPresent()) {
            ATMS atms = atmsOpt.get();




            // Check the primary device
            if (atms.getDeviceName() != null &&
                    (!atms.getDeviceName().contains("All Devices") &&
                            !atms.getDeviceName().contains("Cash Cassette") &&
                            !atms.getDeviceName().contains("Printer"))) {
                return atms.getDeviceName();
            }

            // Check the secondary device
            if (atms.getDevice2Name() != null &&
                    (!atms.getDevice2Name().contains("All Devices") &&
                            !atms.getDevice2Name().contains("Cash Cassette") &&
                            !atms.getDevice2Name().contains("Printer"))) {
                return atms.getDevice2Name();
            }
        }
        return null;
    }


    private String evaluateOperationalState(ATMS atm) {
        Optional<ATMS> atmsOpt = atmsRepository.findByAtmId(atm.getAtmId());
        if (atmsOpt.isPresent()) {
            ATMS atms = atmsOpt.get();
            if (!"Open".equals(atms.getActualOpStateDesc())) {
                return atms.getActualOpStateDesc();
            } else if (!"Online".equals(atms.getCommStateDesc())) {
                return atms.getCommStateDesc();
            }
        }
        // Default to "No Issue" for ATMs in perfect condition
        return "No Issue";
    }


    private void processLowCashAtms(Set<String> processedAtmIds) {
        List<ATM_CASH> lowCashAtms = atmCashRepository.findAllByCleftLessThan(10000.0);
        for (ATM_CASH atmCash : lowCashAtms) {
            String atmId = atmCash.getAtmId();

            if (processedAtmIds.add(atmId)) {
                atmsRepository.findByAtmId(atmId).ifPresent(atm -> savemonitor(atm, "Low Cash"));
            }
        }
    }

    private void processRemainingAtms(Set<String> processedAtmIds) {
        List<ATMS> allAtms = atmsRepository.findAll();
        for (ATMS atm : allAtms) {
            if (!processedAtmIds.contains(atm.getAtmId())) {
                savemonitor(atm, "No Issue");
                processedAtmIds.add(atm.getAtmId());
            }
        }
    }


    private AtmsReportResponseDTO savemonitor(ATMS atm, String status) {
        Optional<AtmMonitorStatus> existingMonitorOpt = MonitorStatusRepository.findByTerminalId(atm.getAtmId());
        AtmMonitorStatus monitor;
        LocalDateTime now = LocalDateTime.now();



        if (existingMonitorOpt.isPresent()) {

            monitor = existingMonitorOpt.get();

            String lastTransactionTime = atm.getCashTxnLastUpdate(); // e.g., "250407 17:04:42"

            // Define the formatter based on the input format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd HH:mm:ss");
            LocalDateTime parsedDateTime = null;

            // Parse lastTransactionTime into LocalDateTime, or use a fallback value
            if (lastTransactionTime != null && !lastTransactionTime.isEmpty()) {
                try {
                    parsedDateTime = LocalDateTime.parse(lastTransactionTime, formatter);
                } catch (Exception e) {
                    parsedDateTime = LocalDateTime.now(); // Use current time as fallback
                }
            } else {
                parsedDateTime = LocalDateTime.now(); // Use current time as fallback
            }


            if (!monitor.getStatus().equals(status)) {
                monitor.setStatus(status);
                monitor.setRegisteredAt(now);
                monitor.setLastTransaction(parsedDateTime);
                monitor.setSinceDateTime(now);
                monitor.setTotalDays(0L);
                monitor.setLevel(EscalationLevels.WAIT);
            } else {
                monitor.setSinceDateTime(now);
                monitor.setLastTransaction(parsedDateTime);
                monitor.setTotalDays(Duration.between(monitor.getRegisteredAt(), now).toDays());
                monitor.setLevel(determineATMStatusEscalationLevel(java.time.Duration.between(monitor.getRegisteredAt(), monitor.getSinceDateTime()).toMinutes()));
            }
        } else {

            monitor = new AtmMonitorStatus();

            String lastTransactionTime = atm.getCashTxnLastUpdate(); // e.g., "250407 17:04:42"

            // Define the formatter based on the input format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd HH:mm:ss");
            LocalDateTime parsedDateTime = null;

            // Parse lastTransactionTime into LocalDateTime, or use a fallback value
            if (lastTransactionTime != null && !lastTransactionTime.isEmpty()) {
                try {
                    parsedDateTime = LocalDateTime.parse(lastTransactionTime, formatter);
                } catch (Exception e) {
                    parsedDateTime = LocalDateTime.now(); // Use current time as fallback
                }
            } else {
                parsedDateTime = LocalDateTime.now(); // Use current time as fallback
            }


            monitor.setTerminalId(atm.getAtmId());
            monitor.setAtmName(atm.getAtmName());
            monitor.setStatus(status);
            monitor.setRegisteredAt(now);
            monitor.setLastTransaction(parsedDateTime);
            monitor.setSinceDateTime(now);
            monitor.setTotalDays(0L);
            monitor.setLevel(EscalationLevels.WAIT);
        }

        MonitorStatusRepository.save(monitor);
        return new AtmsReportResponseDTO(atm.getAtmId(), atm.getAtmName(), status, status, status ,status , status);
    }


    @Transactional
    public void saveATMSCashBalance() {
        try {
            // Fetch all records from ATM_CASH
            List<ATM_CASH> lowCashAtms = atmCashRepository.findAll();
            LocalDateTime now = LocalDateTime.now();

            for (ATM_CASH atmCash : lowCashAtms) {
                String atmId = atmCash.getAtmId(); // Assuming ATM_CASH has getAtmId()
                double balance = atmCash.getCleft(); // Assuming ATM_CASH has getBalance()

                Optional<ATMCashBalance> existingMonitorOpt = atmsCashBalanceRepository.findByAtmId(atmId);
                ATMCashBalance monitor;
                Optional<ATMS> atmsOpt = atmsRepository.findByAtmId(atmId);
                ATMS atms = atmsOpt.get();




                if (existingMonitorOpt.isPresent()) {
                    monitor = existingMonitorOpt.get();

                    String lastTransactionTime = atms.getCashTxnLastUpdate(); // e.g., "250407 17:04:42"

                    // Define the formatter based on the input format
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd HH:mm:ss");
                    LocalDateTime parsedDateTime = null;

                    // Parse lastTransactionTime into LocalDateTime, or use a fallback value
                    if (lastTransactionTime != null && !lastTransactionTime.isEmpty()) {
                        try {
                            parsedDateTime = LocalDateTime.parse(lastTransactionTime, formatter);
                        } catch (Exception e) {
                            parsedDateTime = LocalDateTime.now(); // Use current time as fallback
                        }
                    } else {
                        parsedDateTime = LocalDateTime.now(); // Use current time as fallback
                    }

                    if (monitor.getBalance() != balance) { // Update only if balance changes
                        monitor.setBalance(balance);
                        monitor.setRegisteredAt(now);
                        monitor.setLastTransaction(parsedDateTime);
                        monitor.setSinceDateTime(now);
                        monitor.setTotalDays(0L);
                        monitor.setLevel(EscalationLevels.WAIT); // Reset to WAIT on balance update
                    } else {
                        monitor.setSinceDateTime(now);
                        monitor.setLastTransaction(parsedDateTime);
                        monitor.setTotalDays(java.time.Duration.between(monitor.getRegisteredAt(), now).toMinutes());
                        monitor.setLevel(determineATMCashEscalationLevel(balance)); // Determine escalation level
                    }
                } else {

                    String lastTransactionTime = atms.getCashTxnLastUpdate(); // e.g., "250407 17:04:42"

                    // Define the formatter based on the input format
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd HH:mm:ss");
                    LocalDateTime parsedDateTime = null;

                    // Parse lastTransactionTime into LocalDateTime, or use a fallback value
                    if (lastTransactionTime != null && !lastTransactionTime.isEmpty()) {
                        try {
                            parsedDateTime = LocalDateTime.parse(lastTransactionTime, formatter);
                        } catch (Exception e) {
                            parsedDateTime = LocalDateTime.now(); // Use current time as fallback
                        }
                    } else {
                        parsedDateTime = LocalDateTime.now(); // Use current time as fallback
                    }

                    monitor = new ATMCashBalance();
                    monitor.setAtmId(atmId);
                    monitor.setName(atms.getAtmName());
                    monitor.setBalance(balance);
                    monitor.setRegisteredAt(now);
                    monitor.setLastTransaction(parsedDateTime);
                    monitor.setSinceDateTime(now);
                    monitor.setTotalDays(0L);
                    monitor.setLevel(EscalationLevels.WAIT); // Default to WAIT
                }

                try {
                    // Save or update the monitor record
                    atmsCashBalanceRepository.save(monitor);
                } catch (Exception e) {
                    // Log and continue processing other records
                    System.err.println("Error saving ATM Cash Balance for ATM ID: " + atmId + ". Error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            // Log general exceptions
            System.err.println("Error during saveATMSCashBalance execution: " + e.getMessage());
        }
    }


    private EscalationLevels determineATMStatusEscalationLevel(long elapsedMinutes) {
        if (elapsedMinutes < 30) {
            return EscalationLevels.WAIT;
        } else if (elapsedMinutes < 360) {
            return EscalationLevels.FIRST;
        } else if (elapsedMinutes < 720) {
            return EscalationLevels.SECOND;
        } else if (elapsedMinutes < 1440) {
            return EscalationLevels.THIRD;
        } else if (elapsedMinutes < 1800) {
            return EscalationLevels.FOURTH;
        } else {
            return EscalationLevels.FIFTH;
        }
    }


    private EscalationLevels determineATMCashEscalationLevel(double balance) {


        if (balance < 25000) {
            return EscalationLevels.FIFTH;
        } else if (balance < 50000) {
            return EscalationLevels.FOURTH;
        } else if (balance < 100000) {
            return EscalationLevels.THIRD;
        } else if (balance < 150000) {
            return EscalationLevels.SECOND;
        } else if (balance < 200000) {
            return EscalationLevels.FIRST;
        }

        return EscalationLevels.WAIT;
    }













    //////////////////// Gateway monitor




    public void saveGatewayStatus() {
        try {
            // Fetch all records from ATM_CASH
            List<GatewayInfo> gatewayInfos = gatewayCacheServiceImpl.findAll();

            LocalDateTime now = LocalDateTime.now();

            // Define the 8 gateways to keep
            Set<String> allowedNetworks = Set.of(
                    "sps", // Sparrow POS Gateway
                    "svf", // SVF Gateway
                    "mcb", // MCB Gateway
                    "sms", // Visa SMS
                    "vsa", // Visa Base Issuer
                    "sar", // ATM Gateway
                    "mds", // MDS Gateway
                    "mb1"  // Member Bank 1
            );

            for (GatewayInfo gatewayInfo : gatewayInfos) {
                String network = gatewayInfo.getNetwork();

                // Skip networks not in the allowed list
                if (!allowedNetworks.contains(network)) {
                    continue;
                }

                // Find existing monitor record by network
                Optional<GatewayMonitorStatus> existingMonitorOpt = gatewayStatusReportRepository.findByNetwork(network);
                GatewayMonitorStatus monitor;

                if (existingMonitorOpt.isPresent()) {
                    monitor = existingMonitorOpt.get();

                    // Determine the current combined status
                    String currentCombinedStatus = monitor.getCombinedStatus();
                    String newCombinedStatus = determineCombinedStatus(
                            network,
                            Objects.requireNonNullElse(gatewayInfo.getActualStat(), 0),
                            Objects.requireNonNullElse(gatewayInfo.getCommsStatus(), 0),
                            Objects.requireNonNullElse(gatewayInfo.getLocDesStat(), 0),
                            Objects.requireNonNullElse(gatewayInfo.getRemDesSon(), 0)
                    );

                    // If the status is different, update registeredAt
                    if (!currentCombinedStatus.equals(newCombinedStatus)) {
                        monitor.setRegisteredAt(now);  // Set current date and time
                    }

                    // Update other monitor fields
                    monitor.setCombinedStatus(newCombinedStatus);
                    monitor.setSinceDateTime(now);
                    long totalMinutes = java.time.Duration.between(monitor.getRegisteredAt(), now).toMinutes();
                    monitor.setTotalDays(totalMinutes); // Convert minutes to days
                    monitor.setLevel(determineGatewayStatusEscalationLevel(totalMinutes));
                } else {
                    // Create a new monitor record
                    monitor = new GatewayMonitorStatus();

                    monitor.setNetwork(network);
                    monitor.setNetworkName(network); // Optionally translate this if needed
                    String newCombinedStatus = determineCombinedStatus(
                            network,
                            Objects.requireNonNullElse(gatewayInfo.getActualStat(), 0),
                            Objects.requireNonNullElse(gatewayInfo.getCommsStatus(), 0),
                            Objects.requireNonNullElse(gatewayInfo.getLocDesStat(), 0),
                            Objects.requireNonNullElse(gatewayInfo.getRemDesSon(), 0)
                    );
                    monitor.setCombinedStatus(newCombinedStatus);
                    monitor.setRegisteredAt(now); // Set registeredAt to now
                    monitor.setSinceDateTime(now);
                    monitor.setTotalDays(0L);
                    monitor.setLevel(EscalationLevels.WAIT); // Default to WAIT
                }

                try {
                    // Save or update the monitor record
                    gatewayStatusReportRepository.save(monitor);
                } catch (Exception e) {
                    // Handle any errors that may occur during saving
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            // Handle any errors that may occur during the method execution
            e.printStackTrace();
        }
    }


    private String determineCombinedStatus(String network, Integer actualStat, Integer commsStat, Integer locDesStat, Integer remDesStat) {
        // List to hold descriptions of offline components
        List<String> offlineComponents = new ArrayList<>();
        List<String> problemComponents = new ArrayList<>();

        // Special case: For "sar", remDesStat = 0 and actualStat = 0 are treated as okay
        boolean isSarAndRemDesStatOkay = "sar".equalsIgnoreCase(network) &&
                Objects.requireNonNullElse(remDesStat, 0) == 0 &&
                Objects.requireNonNullElse(actualStat, 0) == 0;

        // Check and classify statuses
        if (!("sar".equalsIgnoreCase(network) && Objects.requireNonNullElse(actualStat, 0) == 0)) {
            if (Objects.requireNonNullElse(actualStat, 0) == 0) {
                offlineComponents.add("Actual Status");
            } else if (actualStat == 2 || actualStat == 3) {
                problemComponents.add("Actual Status");
            }
        }



        if (Objects.requireNonNullElse(commsStat, 0) == 0) {
            offlineComponents.add("Communication Status");
        } else if (commsStat == 2 || commsStat == 3) {
            problemComponents.add("Communication Status");
        }

        if (Objects.requireNonNullElse(locDesStat, 0) == 0) {
            offlineComponents.add("Local Description Status");
        } else if (locDesStat == 2 || locDesStat == 3) {
            problemComponents.add("Local Description Status");
        }

        // Check remDesStat with the special case for "sar"
        if (!isSarAndRemDesStatOkay) {
            if (Objects.requireNonNullElse(remDesStat, 0) == 0) {
                offlineComponents.add("Remote Description Status");
            } else if (remDesStat == 2 || remDesStat == 3) {
                problemComponents.add("Remote Description Status");
            }
        }

        // Determine combined status
        String combinedStatus;
        if (offlineComponents.isEmpty() && problemComponents.isEmpty()) {
            combinedStatus = "Online All";
        } else if (!offlineComponents.isEmpty()) {
            combinedStatus = "Offline: " + String.join(", ", offlineComponents);
        } else {
            combinedStatus = "Problem: " + String.join(", ", problemComponents);
        }

        // Return combined status with network name
        return String.format("%s: %s", network, combinedStatus);
    }







    private EscalationLevels determineGatewayStatusEscalationLevel(long elapsedMinutes) {
        if (elapsedMinutes < 1) {
            return EscalationLevels.WAIT;
        } else if (elapsedMinutes < 5) {
            return EscalationLevels.FIRST;
        } else if (elapsedMinutes < 10) {
            return EscalationLevels.SECOND;
        } else if (elapsedMinutes < 30) {
            return EscalationLevels.THIRD;
        } else if (elapsedMinutes < 60) {
            return EscalationLevels.FOURTH;
        } else {
            return EscalationLevels.FIFTH;
        }
    }

}