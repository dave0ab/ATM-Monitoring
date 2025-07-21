package com.zemenbank.atmMonitoring.services.impl.scheduled;

import com.zemenbank.atmMonitoring.dto.response.AtmsReportResponseDTO;
import com.zemenbank.atmMonitoring.dto.response.AtmsResponse;
import com.zemenbank.atmMonitoring.dto.response.GatewayInfoResponse;
import com.zemenbank.atmMonitoring.model.atmMonitor.*;
import com.zemenbank.atmMonitoring.model.bankworld.ATMS;
import com.zemenbank.atmMonitoring.model.bankworld.ATM_CASH;
import com.zemenbank.atmMonitoring.model.bankworld.ATM_Network;
import com.zemenbank.atmMonitoring.model.bankworld.GatewayInfo;
import com.zemenbank.atmMonitoring.repositories.atmMonitor.AtmLocationRepository;
import com.zemenbank.atmMonitoring.repositories.atmMonitor.AtmStatusReportRepository;
import com.zemenbank.atmMonitoring.repositories.atmMonitor.MonitorStatusRepository;
import com.zemenbank.atmMonitoring.repositories.atmMonitor.PingStatusRepository;
import com.zemenbank.atmMonitoring.repositories.bankworld.ATMCashRepository;
import com.zemenbank.atmMonitoring.repositories.bankworld.ATMSRepository;
import com.zemenbank.atmMonitoring.repositories.bankworld.ATM_NetworkRepository;
import com.zemenbank.atmMonitoring.repositories.bankworld.GatewayInfoRepository;
import com.zemenbank.atmMonitoring.services.ATMCacheService;
import com.zemenbank.atmMonitoring.services.GatewayCacheService;
import com.zemenbank.atmMonitoring.services.impl.atmMonitor.ReportServiceImpl;
import com.zemenbank.atmMonitoring.services.impl.atmMonitor.TransactionServiceImpl;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.zemenbank.atmMonitoring.dto.response.AtmsResponse.mapAtmsToResponse;

@Service
public class PollingService {

    private final ATMSRepository atmsRepository;
    private final ATMCashRepository aTMCashRepository;
    private final GatewayInfoRepository gatewayInfoRepository ;
    private final ATM_NetworkRepository atmNetworkRepository ;
    private final SimpMessagingTemplate messagingTemplate;
    private final AtmLocationRepository atmLocationRepository;

    private  final MonitorStatusRepository monitorStatusRepository;

    private final ATM_NetworkRepository atm_NetworkRepository;

    private final AtmStatusReportRepository atmStatusReportRepository;

    private final TransactionServiceImpl transactionService;

    private PingStatusRepository pingStatusRepository;

    private final PingService pingService;
    private final Map<Long, AtmsResponse> atmCache = new HashMap<>();
    private final Map<String, GatewayInfoResponse> gatewayCache = new HashMap<>();

    private final ReportServiceImpl reportService;
    private final ATMCacheService atmCacheService;

    private final GatewayCacheService gatewayCacheService;



    @Autowired
    public PollingService(TransactionServiceImpl transactionService , GatewayCacheService gatewayCacheService, MonitorStatusRepository monitorStatusRepository, ATMCacheService atmCacheService , ATMSRepository atmsRepository, SimpMessagingTemplate messagingTemplate, PingStatusRepository pingStatusRepository , AtmLocationRepository atmLocationRepository , ATM_NetworkRepository atm_NetworkRepository , PingService pingService, GatewayInfoRepository gatewayInfoRepository , ReportServiceImpl reportService , AtmStatusReportRepository atmStatusReportRepository , ATMCashRepository aTMCashRepository) {
        this.atmsRepository = atmsRepository;
        this.messagingTemplate = messagingTemplate;
        this.gatewayInfoRepository =gatewayInfoRepository;
        this.atmNetworkRepository = atm_NetworkRepository;
        this.pingService =pingService;
        this.atm_NetworkRepository =atm_NetworkRepository;
        this.pingStatusRepository= pingStatusRepository;
        this.atmLocationRepository =atmLocationRepository;
        this.reportService = reportService;
        this.atmStatusReportRepository =atmStatusReportRepository;
        this.aTMCashRepository =aTMCashRepository;
        this.atmCacheService =atmCacheService;
        this.gatewayCacheService = gatewayCacheService;
        this.monitorStatusRepository = monitorStatusRepository;
        this.transactionService=transactionService;
    }


    @Scheduled(fixedRate = 15000)
    public void pollAtmStatus() {
        Pageable pageable = PageRequest.of(0, 1000);  // Define paging limits
        Page<ATMS> pagedAtms = atmCacheService.getAllAtms(pageable);

        Map<Long, AtmsResponse> currentAtmMap = pagedAtms.getContent().stream().map(atm -> {
            ATM_Network gatewayServer = atmNetworkRepository.findByAtmId(atm.getAtmId());
            PingStatus pingStatusEntity = pingStatusRepository.findByTerminalId(atm.getAtmId());
            ATM_CASH atmCash = aTMCashRepository.findFirstByAtmId(atm.getAtmId());
            AtmLocation atmLocation = atmLocationRepository.findByTId(atm.getAtmId());
            Optional<AtmMonitorStatus> atmMonitorStatusopt= monitorStatusRepository.findByTerminalId(atm.getAtmId());
            AtmMonitorStatus atmMonitorStatus =atmMonitorStatusopt.get();


            String atmIP = gatewayServer != null ? gatewayServer.getAddr() : null;
            String pingStatus = pingStatusEntity != null ? pingStatusEntity.getSTATUS() : "UNKNOWN";
            String cashAdd = atmCash != null ? atmCash.getCadd().toString() : null;
            String cashLeft = atmCash != null ? atmCash.getCleft().toString() : null;

            String latitude = atmLocation != null ? atmLocation.getLA() : "0";
            String longitude = atmLocation != null ? atmLocation.getLO() : "0";
            String mapsLink = String.format("https://www.google.com/maps?q=%s,%s&output=embed", latitude, longitude);

            return mapAtmsToResponse(atm, pingStatus, atmIP, mapsLink, cashAdd, cashLeft ,atmMonitorStatus.getRemark());
        }).collect(Collectors.toMap(AtmsResponse::getAtmIdd, atmResponse -> atmResponse));

        // Compare cached and current statuses
        for (Map.Entry<Long, AtmsResponse> entry : currentAtmMap.entrySet()) {
            Long atmId = entry.getKey();
            AtmsResponse currentStatus = entry.getValue();
            AtmsResponse cachedStatus = atmCache.get(atmId);

            // Compare the essential fields only (ignore fields like timestamps)
            if (cachedStatus == null || !compareAtmsResponseWithoutTimestamps(cachedStatus, currentStatus)) {

                messagingTemplate.convertAndSend("/topic/atms/status", currentStatus);
                atmCache.put(atmId, currentStatus);
            } else {

            }
        }


    }



    private boolean compareAtmsResponseWithoutTimestamps(AtmsResponse cached, AtmsResponse current) {
        return Objects.equals(cached.getSuccessTxnLastUpdate(), current.getSuccessTxnLastUpdate())&&
                Objects.equals(cached.getCommsState(),current.getCommsState())&&
                Objects.equals(cached.getCashAdd(),current.getCashAdd())&&
                Objects.equals(cached.getCashLeft(),current.getCashLeft())&&
                Objects.equals(cached.getCashTxnLastUpdate(),current.getCashTxnLastUpdate())&&
                Objects.equals(cached.getDevice2(),current.getDevice2())&&
                Objects.equals(cached.getDevice(),current.getDevice ())&&
                Objects.equals(cached.getAtmIP(),current.getAtmIP())&&
                Objects.equals(cached.getDeviceName(),current.getDeviceName())&&
                Objects.equals(cached.getActualOpState(), current.getActualOpState());
    }

    //
    @Scheduled(fixedRate = 15000)
    public void pollGatewayServerStatus() {
        // Fetch cached data


        List<GatewayInfo> currentGatewayServers = gatewayCacheService.findAll();

        GatewayInfoResponse gatewayInfoResponseInstance = new GatewayInfoResponse();
        Map<String, GatewayInfoResponse> currentGatewayServerMap = currentGatewayServers.stream()
                .map(gatewayInfoResponseInstance::mapGatewayInfoToResponse)
                .collect(Collectors.toMap(GatewayInfoResponse::getGetwayServerNetwork, gateway -> gateway));

        // Compare cached data with current data and broadcast updates
        for (Map.Entry<String, GatewayInfoResponse> entry : currentGatewayServerMap.entrySet()) {
            String network = entry.getKey();
            GatewayInfoResponse currentStatus = entry.getValue();
            GatewayInfoResponse cachedStatus = gatewayCache.get(network);

            if (cachedStatus == null || !cachedStatus.equals(currentStatus)) {
                // If there's a change, broadcast it
                messagingTemplate.convertAndSend("/topic/gateway/updates", currentStatus);
                gatewayCache.put(network, currentStatus); // Update cache with the latest status
            } else {

            }
        }

    }



    @Scheduled(fixedRate = 15000)
    public void pingOfflineAtms() {


        List<ATMS> currentAtms = atmsRepository.findAll();




        currentAtms.forEach(atm -> {
            ATM_Network gatewayServers = atmNetworkRepository.findByAtmId(atm.getAtmId());

            PingStatus existingPingStatus = pingStatusRepository.findByTerminalId(atm.getAtmId());

            if (existingPingStatus == null) {

                PingStatus newPingStatus = new PingStatus();
                newPingStatus.setTerminalId(atm.getAtmId());
                newPingStatus.setIP_ADDRESS(gatewayServers.getAddr());
                newPingStatus.setSTATUS("Both Reply");
                newPingStatus.setTIMESTAMP(LocalDateTime.now());
                newPingStatus.setDETAILS("host");
                pingStatusRepository.save(newPingStatus);
            }

            if (gatewayServers != null) {



                if("1".equalsIgnoreCase(atm.getCommsState().toString())) {

                    String gatewayIp = gatewayServers.getAddr();
                    pingService.savePingStatus(atm.getAtmId(),gatewayIp,"Both Reply","host");

                }
           else
                {



                    String gatewayIp = gatewayServers.getAddr();



                    CompletableFuture<String> pingStatusFuture = pingService.getStatusAsync(gatewayServers.getAtmId(), gatewayIp);

                    pingStatusFuture.thenAccept(status -> {


                    }).exceptionally(ex -> {

                        return null;
                    });

                }


            }
        });
    }




    private static final String CONSTANT_SUFFIX = "502285";
    public String parseTerminalId(String id) {
        // If ID is already in the long format, return it as is
        if (id.endsWith(CONSTANT_SUFFIX)) {
            return id;
        }

        // Remove the leading "1" if present
        if (id.startsWith("1")) {
            id = id.substring(1);
        }

        // Append the constant suffix to form the long format
        return id + CONSTANT_SUFFIX;
    }

    @Transactional
    public void updateAllTerminalIds() {
        List<AtmLocation> atmLocations = atmLocationRepository.findAll();

        // Step 1: Create new records with updated TIDs
        List<AtmLocation> newRecords = new ArrayList<>();
        List<String> originalShortIds = new ArrayList<>();

        for (AtmLocation atmLocation : atmLocations) {
            String currentId = atmLocation.getTID();
            String parsedId = parseTerminalId(currentId);

            // If parsedId is different, prepare for insertion
            if (!currentId.equals(parsedId)) {
                // Track the original ID for later deletion
                originalShortIds.add(currentId);

                // Create a new record with the updated TID
                AtmLocation newLocation = new AtmLocation();
                newLocation.setTID(parsedId);
                newLocation.setLO(atmLocation.getLO());
                newLocation.setLA(atmLocation.getLA());
                newRecords.add(newLocation);
            }
        }

        // Save the new records with updated TIDs
        atmLocationRepository.saveAll(newRecords);

        // Step 2: Delete original records with the old (short) TIDs
        if (!originalShortIds.isEmpty()) {
            atmLocationRepository.deleteAllByTIDIn(originalShortIds);
        }

        // Final verification to ensure no short IDs remain in the database
        verifyNoShortIds();
    }

    private void verifyNoShortIds() {
        List<AtmLocation> atmLocations = atmLocationRepository.findAll();
        for (AtmLocation atmLocation : atmLocations) {
            String currentId = atmLocation.getTID();
            if (!currentId.endsWith(CONSTANT_SUFFIX)) {
                throw new IllegalStateException("Update failed: Found a short ID in the database.");
            }
        }
    }





    //@Scheduled(fixedRate = 60000)
    public void pollAndSaveAtmReportStatus() {
        try {
            List<AtmsReportResponseDTO> reportData = reportService.generateAllAtmsReport();

            for (AtmsReportResponseDTO report : reportData) {
                Optional<AtmStatusReport> existingReport = atmStatusReportRepository.findByTerminalId(report.getTerminalId());
                AtmStatusReport entity = existingReport.map(atm -> {
                    atm.setAtmName(report.getAtmName());
                    atm.setStatus(report.getStatus());
                    return atm;
                }).orElseGet(() -> convertToEntity(report));

                atmStatusReportRepository.save(entity);
            }

            System.out.println("ATM Status Report updated successfully: " + LocalDateTime.now());
        } catch (Exception e) {
            System.err.println("Error while polling and saving ATM statuses: " + e.getMessage());
        }
    }


    private AtmStatusReport convertToEntity(AtmsReportResponseDTO dto) {
        return AtmStatusReport.builder()
                .terminalId(dto.getTerminalId())
                .atmName(dto.getAtmName())
                .status(dto.getStatus())
                .build();
    }



    @Scheduled(fixedRate = 60000)
    public void scheduledFetch() {
        transactionService.fetchAndStoreTransactions();

    }





}
