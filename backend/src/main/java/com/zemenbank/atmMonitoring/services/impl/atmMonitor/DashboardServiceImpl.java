package com.zemenbank.atmMonitoring.services.impl.atmMonitor;

import com.zemenbank.atmMonitoring.dto.response.DashboardResponse;
import com.zemenbank.atmMonitoring.dto.response.UserResponse;
import com.zemenbank.atmMonitoring.model.atmMonitor.ATMAccesses;
import com.zemenbank.atmMonitoring.model.atmMonitor.User;
import com.zemenbank.atmMonitoring.repositories.atmMonitor.*;
import com.zemenbank.atmMonitoring.repositories.bankworld.ATMCashRepository;
import com.zemenbank.atmMonitoring.repositories.bankworld.ATMSRepository;
import com.zemenbank.atmMonitoring.repositories.bankworld.ATM_NetworkRepository;
import com.zemenbank.atmMonitoring.security.AuthService;
import com.zemenbank.atmMonitoring.services.ATMCacheService;
import com.zemenbank.atmMonitoring.services.BaseServices;
import com.zemenbank.atmMonitoring.services.DashboardService;
import org.springframework.data.domain.Page;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Service
public class DashboardServiceImpl extends BaseServices implements DashboardService {
    private final ATMSRepository atmsRepository;

    private final ATM_NetworkRepository atm_NetworkRepository;
    private final ATMCashRepository aTMCashRepository;


    private PingStatusRepository  pingStatusRepository;

    private ATMAccesses atmAccesses;

    private final AuthService authService;
    private ATMAccessRepository atmAccessRepository;

    private final SimpMessagingTemplate messagingTemplate;
    private final   AtmLocationRepository atmLocationRepository;
    private final AtmStatusReportRepository atmStatusReportRepository;
    private final ATMCacheService cachingService;

    private final UserRepository userRepository;

    private final MonitorStatusRepository monitorStatusRepository;

    private final GatewayStatusReportRepository gatewayStatusReportRepository;

    public DashboardServiceImpl(
            ATMSRepository atmsRepository,
            AuthService authService,
            ATM_NetworkRepository atm_NetworkRepository,
            AtmLocationRepository atmLocationRepository,
            ATMCashRepository aTMCashRepository,
            PingStatusRepository pingStatusRepository,
            SimpMessagingTemplate messagingTemplate,
            AtmStatusReportRepository atmStatusReportRepository,
            ATMAccessRepository atmAccessRepository,
            ATMCacheService atmCacheService,
            UserRepository userRepository,
            MonitorStatusRepository monitorStatusRepository,
            GatewayStatusReportRepository gatewayStatusReportRepository

            // Removed ATMAccesses here
    ) {
        this.atmsRepository = atmsRepository;
        this.authService = authService;
        this.messagingTemplate = messagingTemplate;
        this.atm_NetworkRepository = atm_NetworkRepository;
        this.pingStatusRepository = pingStatusRepository;
        this.atmLocationRepository = atmLocationRepository;
        this.atmStatusReportRepository = atmStatusReportRepository;
        this.aTMCashRepository = aTMCashRepository;
        this.atmAccessRepository = atmAccessRepository;
        this.cachingService =atmCacheService;// Correctly injected the repository
        this.userRepository =userRepository;
        this.monitorStatusRepository =monitorStatusRepository;
        this.gatewayStatusReportRepository =gatewayStatusReportRepository;
    }

    @Override
    public UserResponse getProfileDetail() {
        String username = null;

        try {
            // Get the logged-in user's username
            username = authService.getLoggedInUserId();
            logger.info("Fetching profile detail for username: " + username);

            // Fetch the user from the repository based on their ID
            Optional<User> optionalUser = userRepository.findById(Long.valueOf(username));

            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                logger.info("User found in database: " + user.getDomainId());

                // Map User entity to UserResponse DTO
                return UserResponse.builder()
                        .id(user.getUser_id())
                        .domainId(user.getDomainId())
                        .role(user.getRole().name()) // Convert enum to string (e.g., ADMIN, IT, etc.)
                        .status(user.getStatus())
                        .notificationLevel(user.getNotificationLevel())
                        .createdAt(user.getCreatedAt().toString()) // Convert LocalDateTime to String
                        .lastSignOn(user.getLastSignedOn().toString()) // Convert Instant to String
                        .build();
            } else {
                logger.warn("No user found for username: " + username);
            }
        } catch (Exception e) {
            logger.error("Error fetching profile detail for username: " + username, e);
        }

        // Return null if no user found or an error occurs
        return null;
    }



    @Override
    public DashboardResponse getDashboardDetail() {
        String userId = authService.getLoggedInUserId();
        logger.info("Fetching profile detail for username: " + userId);

        // Get the user's roles
        String userRoles = authService.getUserRoles();


        Optional<User> userOpt  = userRepository.findById(Long.valueOf(userId));
        User u = null;
        if(userOpt.isPresent()){

            u = userOpt.get();

        }

        boolean isOmnichannelUser = userRoles.contains("OMNI_CHANNEL")
                && !u.getNotificationLevel().contains("FIRST");

        boolean isAdminOrIT = userRoles.contains("ADMIN") || userRoles.contains("IT") || isOmnichannelUser;
        List<String> allowedTIDs = new ArrayList<>();

        // For non-admin or non-IT users, fetch allowed TIDs
        if (!isAdminOrIT) {
            allowedTIDs = atmAccessRepository.findByUserId(userId).stream()
                    .filter(access -> "CHECKED".equalsIgnoreCase(access.getStatus()))
                    .map(ATMAccesses::getTID)
                    .toList();
            logger.debug("Allowed TIDs for user {}: {}", userId, allowedTIDs);

            // If no allowed TIDs are found, return an empty page or some fallback response
            if (allowedTIDs.isEmpty()) {
                logger.warn("No allowed TIDs found for user: {}", userId);
                return DashboardResponse.builder()
                        .totalAtms("0")
                        .atmsOnline("0")
                        .atmsWithIssues("0")
                        .activeAlerts("0")
                        .build();
            }
        }

        // Fetching total ATMs count filtered by allowed TIDs (if user is not admin or IT)
        String totalAtms = isAdminOrIT
                ? String.valueOf(atmsRepository.count())  // Admin/IT can see all ATMs
                : String.valueOf(atmsRepository.countByAtmIdIn(allowedTIDs));  // Non-admins are filtered by allowed TIDs

        // Fetching ATMs online (status = "No Issue") filtered by allowed TIDs
        // Fetching ATMs online (status equals "1") filtered by allowed TIDs if applicable
        String atmsOnline = isAdminOrIT
                ? String.valueOf(atmsRepository.findAllStatus("1").size())
                : String.valueOf(atmsRepository.findAllByTerminalIdInAndStatus("1", allowedTIDs).size());

        String atmsWithIssues = isAdminOrIT
                ? String.valueOf(atmsRepository.findAllStatusNotEqual("1").size())
                : String.valueOf(atmsRepository.findAllByTerminalIdInAndStatusNotEqual("1", allowedTIDs).size());

        // Fetching active alerts (status doesn't contain "Online All") filtered by allowed TIDs
        String activeAlerts = "0";
        // Build and return the DashboardResponse DTO
        return DashboardResponse.builder()
                .totalAtms(totalAtms)
                .atmsOnline(atmsOnline)
                .atmsWithIssues(atmsWithIssues)
                .activeAlerts(activeAlerts)
                .build();
    }






}
