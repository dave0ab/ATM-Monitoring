package com.zemenbank.atmMonitoring.services.impl.bankworld;

import com.zemenbank.atmMonitoring.dto.response.AtmsResponse;
import com.zemenbank.atmMonitoring.exception.ResourceNotFoundException;
import com.zemenbank.atmMonitoring.model.atmMonitor.*;
import com.zemenbank.atmMonitoring.model.bankworld.ATMS;
import com.zemenbank.atmMonitoring.model.bankworld.ATM_CASH;
import com.zemenbank.atmMonitoring.model.bankworld.ATM_Network;
import com.zemenbank.atmMonitoring.repositories.atmMonitor.*;
import com.zemenbank.atmMonitoring.repositories.bankworld.ATMCashRepository;
import com.zemenbank.atmMonitoring.repositories.bankworld.ATM_NetworkRepository;
import com.zemenbank.atmMonitoring.security.AuthService;
import com.zemenbank.atmMonitoring.services.ATMCacheService;
import com.zemenbank.atmMonitoring.services.BaseServices;
import com.zemenbank.atmMonitoring.repositories.bankworld.ATMSRepository;
import com.zemenbank.atmMonitoring.services.AtmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.zemenbank.atmMonitoring.dto.response.AtmsResponse.mapAtmsToResponse;
import static com.zemenbank.atmMonitoring.services.impl.cache.ATMCacheServiceImpl.atmCache;

@Transactional
@Service
public class ATMSServiceImpl extends BaseServices implements AtmsService {

    private final ATMSRepository atmsRepository;

    private final ATM_NetworkRepository atm_NetworkRepository;

    private final ATMCashRepository aTMCashRepository;
    private final MonitorStatusRepository monitorStatusRepository;


    private PingStatusRepository  pingStatusRepository;

    private ATMAccesses atmAccesses;

    private final AuthService authService;
    private ATMAccessRepository atmAccessRepository;

    private final SimpMessagingTemplate messagingTemplate;
    private final   AtmLocationRepository atmLocationRepository;
    private final AtmStatusReportRepository atmStatusReportRepository;
    private final ATMCacheService cachingService;

    private final UserRepository userRepository;

    public ATMSServiceImpl(
            UserRepository userRepository,
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
            MonitorStatusRepository monitorStatusRepository
            // Removed ATMAccesses here
    ) {
        this.userRepository =userRepository;
        this.atmsRepository = atmsRepository;
        this.authService = authService;
        this.messagingTemplate = messagingTemplate;
        this.atm_NetworkRepository = atm_NetworkRepository;
        this.pingStatusRepository = pingStatusRepository;
        this.atmLocationRepository = atmLocationRepository;
        this.atmStatusReportRepository = atmStatusReportRepository;
        this.aTMCashRepository = aTMCashRepository;
        this.atmAccessRepository = atmAccessRepository;
        this.cachingService =atmCacheService;
        this.monitorStatusRepository =monitorStatusRepository;
    }

    // Temporary in-memory storage for tracking last device sorted for each user
    private final Map<String, String> userDeviceSortTracker = new ConcurrentHashMap<>();



    // A map to track the last sort direction for each user and sort field
    private Map<String, Map<String, Sort.Direction>> userSortDirections = new HashMap<>();


    @Override
    public Page<AtmsResponse> getAllAtms(int pageNumber, int pageSize, String status, String search, String sort, String sortBy, String problem) {
        Logger logger = LoggerFactory.getLogger(ATMSServiceImpl.class);
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "atmname";  // Replace with a meaningful default field
        }


        String username = authService.getLoggedInUserId();
        logger.debug("Fetching ATMs for user: {}", username);

        String userRoles = authService.getUserRoles();
        Optional<User> userOpt  = userRepository.findById(Long.valueOf(username));
        User u = null;
        if(userOpt.isPresent()){

            u = userOpt.get();

        }
        boolean isOmnichannelUser = userRoles.contains("OMNI_CHANNEL")
                && !u.getNotificationLevel().contains("FIRST");



        boolean isAdminOrIT = userRoles.contains("ADMIN") || userRoles.contains("IT") || isOmnichannelUser;

        logger.debug("User roles: {}, isAdminOrIT: {}", userRoles, isAdminOrIT);

        List<String> allowedTIDs = new ArrayList<>();
        if (!isAdminOrIT) {
            allowedTIDs = atmAccessRepository.findByUserId(username).stream()
                    .filter(access -> "CHECKED".equalsIgnoreCase(access.getStatus()))
                    .map(ATMAccesses::getTID)
                    .toList();
            logger.debug("Allowed TIDs for user {}: {}", username, allowedTIDs);

            if (allowedTIDs.isEmpty()) {
                logger.warn("No allowed TIDs found for user: {}", username);
                return Page.empty();
            }
        }

        Sort.Direction direction = determineSortDirection(username, sortBy, sort);
        String sortField = determineSortField(sortBy);

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortField));
        logger.debug("Page request created: pageNumber = {}, pageSize = {}, sortField = {}, direction = {}", pageNumber, pageSize, sortField, direction);

        Page<ATMS> atmsPage = fetchAtmsFromCache(pageable, isAdminOrIT, allowedTIDs, status, search, problem);

        logger.debug("ATMs fetched: totalElements = {}, totalPages = {}", atmsPage.getTotalElements(), atmsPage.getTotalPages());
        return atmsPage.map(this::mapToResponse);
    }





    private Sort.Direction determineSortDirection(String username, String sortBy, String sort) {
        if ("true".equals(sort)) {
            userSortDirections.putIfAbsent(username, new HashMap<>());
            Map<String, Sort.Direction> userFieldDirections = userSortDirections.get(username);
            Sort.Direction currentDirection = userFieldDirections.getOrDefault(sortBy, Sort.Direction.DESC);
            Sort.Direction newDirection = currentDirection == Sort.Direction.ASC ? Sort.Direction.DESC : Sort.Direction.ASC;
            userFieldDirections.put(sortBy, newDirection);
            return newDirection;
        }
        return Sort.Direction.ASC;
    }

    private String determineSortField(String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "id":
            case "atmid": return "atmId";
            case "atmname": return "atmName";
            case "devicename": return "deviceName";
            case "actualopstate": return "actualOpStateDesc";
            case "commstate":
            case "commsstate": return "commStateDesc";
            case "successtxnlastupdate": return "successTxnLastUpdate";
            case "cashleft": return "cashLeft";
            case "pingstatus": return "pingStatus";

            default: throw new IllegalArgumentException("Invalid sort property: " + sortBy);
        }
    }



    private Page<ATMS> fetchAtmsFromCache(Pageable pageable, boolean isAdminOrIT, List<String> allowedTIDs, String status, String search, String problem) {
        // Retrieve all ATMs from the cache
        List<ATMS> atmsList = new ArrayList<>(atmCache.values());

        // Filter based on 'problem' (deviceName)
        if (problem != null && !problem.trim().isEmpty()) {
            return cachingService.getAtmsByDeviceName(problem, pageable);
        }
        if (status != null && !status.trim().isEmpty()) {
            return cachingService.getAtmsByStatus(status, pageable);
        }
        if (search != null && !search.trim().isEmpty()) {
            return cachingService.searchAtms(search, pageable);
        }

        // Filter based on allowed TIDs for non-admin users
        if (!isAdminOrIT) {
            atmsList = atmsList.stream()
                    .filter(atm -> allowedTIDs.contains(atm.getAtmId()))
                    .toList();
        }

        // Apply grouping and prioritization based on sortBy
        String sortBy = pageable.getSort().stream()
                .findFirst()
                .map(Sort.Order::getProperty)
                .orElse("atmId"); // Default to atmId if no sortBy is provided
        boolean ascending = pageable.getSort().getOrderFor(sortBy).isAscending();
        atmsList = prioritizeByProperty(atmsList, sortBy);


        // Apply sorting based on Pageable
        if (pageable.getSort().isSorted()) {
            Comparator<ATMS> comparator = pageable.getSort().stream()
                    .map(order -> {
                        String property = order.getProperty();

                        // Define property-specific comparator
                        Comparator<ATMS> propertyComparator = switch (property) {
                            case "atmId", "atmName", "deviceName", "actualOpStateDesc", "commStateDesc" ,"pingStatus" ->
                                    Comparator.comparing(atm -> {
                                        return switch (property) {
                                            case "atmId" -> atm.getAtmId();
                                            case "atmName" -> atm.getAtmName();
                                            case "deviceName" -> atm.getDeviceName();
                                            case "actualOpStateDesc" -> atm.getActualOpStateDesc();
                                            case "commStateDesc" -> atm.getCommStateDesc();
                                            case "pingStatus" -> {
                                                PingStatus pingStatusEntity = pingStatusRepository.findByTerminalId(atm.getAtmId());
                                                // Return ping status if found, else default to "UNKNOWN"
                                                yield pingStatusEntity != null ? pingStatusEntity.getSTATUS() : pingStatusEntity.getSTATUS();
                                            }

                                            default -> throw new IllegalArgumentException("Invalid sort property: " + property);
                                        };
                                    }, Comparator.nullsLast(Comparator.naturalOrder()));

                            case "successTxnLastUpdate" ->
                                    Comparator.comparing(atm -> {
                                        String dateStr = atm.getSuccessTxnLastUpdate();
                                        return parseDate(dateStr) != null ? parseDate(dateStr).getTime() : Long.MIN_VALUE;
                                    }, Comparator.nullsLast(Comparator.naturalOrder()));


                            case "cashLeft" ->
                                    Comparator.comparing(atm -> {
                                        ATM_CASH atm_CASH = aTMCashRepository.findFirstByAtmId(atm.getAtmId());
                                        if (atm_CASH != null && atm_CASH.getCleft() != null) {
                                            return atm_CASH.getCleft(); // Return cashLeft if present
                                        } else {
                                            return Double.MIN_VALUE; // Use a very low value for null or missing cashLeft
                                        }
                                    }, Comparator.nullsLast(Comparator.naturalOrder()));





                            default -> throw new IllegalArgumentException("Invalid sort property: " + property);
                        };



                        return ascending ? propertyComparator : propertyComparator.reversed();
                    })
                    .reduce(Comparator::thenComparing)
                    .orElse((a, b) -> 0); // Default comparator that does nothing

            // Apply sorting to the list
            atmsList.sort(comparator);
        }

        // Paginate the sorted list
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), atmsList.size());
        List<ATMS> paginatedAtms = atmsList.subList(start, end);

        return new PageImpl<>(paginatedAtms, pageable, atmsList.size());
    }


    // This will store the rotation index for each user
    private Map<String, Integer> userRotationIndex = new HashMap<>();

    // This will store the distinct Device Names or other properties used for sorting
    private List<String> distinctValues;

    // This will store the grouped ATMs by the chosen property (e.g., DeviceName, actualOpStateDesc, etc.)
    private Map<String, List<ATMS>> groupedATMs;
    private List<ATMS> prioritizeByProperty(List<ATMS> atmsList, String property) {

         String username = authService.getLoggedInUserId();



        groupedATMs = atmsList.stream()
                .collect(Collectors.groupingBy(atm -> {
                    switch (property) {
                        case "deviceName":
                            return Optional.ofNullable(atm.getDeviceName()).orElse("UNKNOWN");
                        case "actualOpStateDesc":
                            return Optional.ofNullable(atm.getActualOpStateDesc()).orElse("UNKNOWN");
                        case "commStateDesc":
                            return Optional.ofNullable(atm.getCommStateDesc()).orElse("UNKNOWN");
                        case "pingStatus":
                            // Assuming you want to group by the status of ping for each ATM
                            PingStatus pingStatusEntity = pingStatusRepository.findByTerminalId(atm.getAtmId());
                            return pingStatusEntity != null ? pingStatusEntity.getSTATUS() : "UNKNOWN";  // Default to "UNKNOWN" if PingStatus is null
                        default:
                            return "UNKNOWN"; // Default case if the property is invalid
                    }
                }));

        // Get the distinct values based on the selected property
        distinctValues = new ArrayList<>(groupedATMs.keySet());

        // Initialize rotation for the user if not already done
        if (!userRotationIndex.containsKey(username)) {
            userRotationIndex.put(username, 0); // Start with the first group
        }

        // Get the next group for the user
        return getNextGroup();
    }

    // Get the next group in rotation for the user, based on the current rotation index
    // This will store the last used sortBy for each user
    private Map<String, String> userSortTracker = new HashMap<>(); // Track last sortBy for each user

    public List<ATMS> getNextGroup() {
        String username = authService.getLoggedInUserId();

        // Get the current rotation index for the user
        int currentIndex = userRotationIndex.getOrDefault(username, 0);

        // Check if distinctValues is empty or has only one element
        if (distinctValues.isEmpty()) {
            return new ArrayList<>();  // Return an empty list if there are no distinct values
        }

        // Ensure that currentIndex is within bounds
        if (currentIndex >= distinctValues.size()) {
            currentIndex = 0;  // Reset to 0 if the current index exceeds the size
        }

        // Get the value that is currently being shown (deviceName, actualOpStateDesc, or commStateDesc)
        String currentValue = distinctValues.get(currentIndex);

        // Get the ATMs grouped by the current value
        List<ATMS> currentGroup = groupedATMs.get(currentValue);

        // Update rotation index for the user
        currentIndex = (currentIndex + 1) % distinctValues.size(); // Safely handle index rotation

        // Update the user's rotation index
        userRotationIndex.put(username, currentIndex);

        // Return the ATMs for the current group
        return currentGroup;
    }



    // Helper method to parse date strings
    private Date parseDate(String dateStr) {
        try {
            if (dateStr == null || dateStr.trim().isEmpty()) {
                return null;  // Handle null or empty date string
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd HH:mm:ss");
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            return null;  // Return null for invalid dates
        }
    }






    // Mapping method for ATMs to response
    private AtmsResponse mapToResponse(ATMS atms) {
        // Map ATMS data to AtmsResponse object
        ATM_Network gatewayServer = atm_NetworkRepository.findByAtmId(atms.getAtmId());
        ATM_CASH atm_CASH = aTMCashRepository.findFirstByAtmId(atms.getAtmId());
        PingStatus pingStatusEntity = pingStatusRepository.findByTerminalId(atms.getAtmId());
        Optional<AtmMonitorStatus> atmMonitorStatusopt= monitorStatusRepository.findByTerminalId(atms.getAtmId());
        AtmMonitorStatus atmMonitorStatus =atmMonitorStatusopt.get();
        String atmIP = gatewayServer != null ? gatewayServer.getAddr() : null;
        String cashAdd = atm_CASH != null ? atm_CASH.getCadd().toString() : null;
        String cashLeft = atm_CASH != null ? atm_CASH.getCleft().toString(): null;

        String pingStatus = pingStatusEntity != null ? pingStatusEntity.getSTATUS() : "UNKNOWN";

        AtmLocation atmLocation = atmLocationRepository.findByTId(atms.getAtmId());

        if (atmLocation == null) {
            atmLocation = AtmLocation.builder()
                    .TID(atms.getAtmId())
                    .LO("0.0")  // Default Longitude
                    .LA("0.0")  // Default Latitude
                    .build();
            atmLocation = atmLocationRepository.save(atmLocation);
        }


        String latitude = atmLocation.getLA();
        String longitude = atmLocation.getLO();
        String mapsLink = String.format("https://www.google.com/maps?q=%s,%s&output=embed", latitude, longitude);

        return mapAtmsToResponse(atms, pingStatus, atmIP, mapsLink, cashAdd ,cashLeft , atmMonitorStatus.getRemark());
    }




    // Method to determine and return sorting preference




    @Override
    public AtmsResponse getAtmById(String atmId) {
        ATMS atms = atmsRepository.findById(Long.valueOf(atmId))
                .orElseThrow(() -> new ResourceNotFoundException("ATM with id " + atmId + " not found"));

        ATM_Network gatewayServer = atm_NetworkRepository.findByAtmId(atms.getAtmId());
        PingStatus pingStatusEntity = pingStatusRepository.findByTerminalId(atms.getAtmId());
        ATM_CASH atm_CASH = aTMCashRepository.findFirstByAtmId(atms.getAtmId());
        String cashAdd = atm_CASH != null ? atm_CASH.getCadd().toString() : null;
        String cashLeft = atm_CASH != null ? atm_CASH.getCleft().toString(): null;
        String atmIP = gatewayServer != null ? gatewayServer.getAddr() : null;
        String pingStatus = pingStatusEntity != null ? pingStatusEntity.getSTATUS() : "UNKNOWN";
        Optional<AtmMonitorStatus> atmMonitorStatusopt= monitorStatusRepository.findByTerminalId(atms.getAtmId());
        AtmMonitorStatus atmMonitorStatus =atmMonitorStatusopt.get();

        AtmLocation atmLocation = (AtmLocation) atmLocationRepository.findByTId(atmId);

        if (atmLocation == null) {
            atmLocation = AtmLocation.builder()
                    .TID(atmId)
                    .LO("0.0")  // Default Longitude
                    .LA("0.0")  // Default Latitude
                    .build();
            atmLocation = atmLocationRepository.save(atmLocation);
        }


        String latitude = atmLocation.getLA(); // Replace with the actual method that returns latitude
        String longitude = atmLocation.getLO(); // Replace with the actual method that returns longitude

        String mapsLink = String.format("https://www.google.com/maps?q=%s,%s", latitude, longitude +"&output=embed");

        return mapAtmsToResponse(atms, pingStatus, atmIP , mapsLink , cashAdd ,cashLeft , atmMonitorStatus.getRemark());
    }



    @Override
    public String addRemark(String atmId, String remark) {
        // Check if the ATM exists
        Optional<AtmMonitorStatus> atmMonitorStatusOpt = monitorStatusRepository.findByTerminalId(atmId);

        if (atmMonitorStatusOpt.isPresent()) {
            // Update remark if ATM exists
            AtmMonitorStatus atmMonitorStatus = atmMonitorStatusOpt.get();
            atmMonitorStatus.setRemark(remark);
            monitorStatusRepository.save(atmMonitorStatus); // Persist changes
            return "Remark updated successfully for ATM ID: " + atmId;
        } else {
            // Throw exception if ATM does not exist
            throw new NoSuchElementException("ATM with ID " + atmId + " not found.");
        }
    }







    @Override
    public Page<AtmsResponse> getAllAtmsSmartBranch(int pageNumber, int pageSize) {
        // Create a Pageable object with the given page number and page size
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        // Fetch the paginated data from the repository
        Page<ATMS> atmsPage = atmsRepository.findAll(pageable);


        // Map the ATMS entities to AtmsResponse DTOs
        return atmsPage.map(this::mapToResponseSmartBranch);
    }





    @Override
    public AtmsResponse getAtmBYIdSmartBranch(String terminalId) {


            ATMS atms = atmsRepository.findByAtmId(terminalId)
                    .orElseThrow(() -> new ResourceNotFoundException("ATM with id " + terminalId + " not found"));

            ATM_Network gatewayServer = atm_NetworkRepository.findByAtmId(atms.getAtmId());
            PingStatus pingStatusEntity = pingStatusRepository.findByTerminalId(atms.getAtmId());
            ATM_CASH atm_CASH = aTMCashRepository.findFirstByAtmId(atms.getAtmId());
            String cashAdd = atm_CASH != null ? atm_CASH.getCadd().toString() : null;
            String cashLeft = atm_CASH != null ? atm_CASH.getCleft().toString(): null;
            String atmIP = "";
            String pingStatus = pingStatusEntity != null ? pingStatusEntity.getSTATUS() : "UNKNOWN";
            Optional<AtmMonitorStatus> atmMonitorStatusopt= monitorStatusRepository.findByTerminalId(atms.getAtmId());
            AtmMonitorStatus atmMonitorStatus =atmMonitorStatusopt.get();

            AtmLocation atmLocation = (AtmLocation) atmLocationRepository.findByTId(terminalId);

            if (atmLocation == null) {
                atmLocation = AtmLocation.builder()
                        .TID(terminalId)
                        .LO("0.0")  // Default Longitude
                        .LA("0.0")  // Default Latitude
                        .build();
                atmLocation = atmLocationRepository.save(atmLocation);
            }


            String latitude = atmLocation.getLA(); // Replace with the actual method that returns latitude
            String longitude = atmLocation.getLO(); // Replace with the actual method that returns longitude

            String mapsLink = String.format("https://www.google.com/maps?q=%s,%s", latitude, longitude +"&output=embed");

            return mapAtmsToResponse(atms, pingStatus, atmIP , mapsLink , cashAdd ,cashLeft , atmMonitorStatus.getRemark());
        }





    private AtmsResponse mapToResponseSmartBranch(ATMS atms) {
        // Map ATMS data to AtmsResponse object
        ATM_Network gatewayServer = atm_NetworkRepository.findByAtmId(atms.getAtmId());
        ATM_CASH atm_CASH = aTMCashRepository.findFirstByAtmId(atms.getAtmId());
        PingStatus pingStatusEntity = pingStatusRepository.findByTerminalId(atms.getAtmId());
        Optional<AtmMonitorStatus> atmMonitorStatusopt= monitorStatusRepository.findByTerminalId(atms.getAtmId());
        AtmMonitorStatus atmMonitorStatus =atmMonitorStatusopt.get();
        String atmIP = "";
        String cashAdd = atm_CASH != null ? atm_CASH.getCadd().toString() : null;
        String cashLeft = atm_CASH != null ? atm_CASH.getCleft().toString(): null;

        String pingStatus = pingStatusEntity != null ? pingStatusEntity.getSTATUS() : "UNKNOWN";

        AtmLocation atmLocation = atmLocationRepository.findByTId(atms.getAtmId());

        if (atmLocation == null) {
            atmLocation = AtmLocation.builder()
                    .TID(atms.getAtmId())
                    .LO("0.0")  // Default Longitude
                    .LA("0.0")  // Default Latitude
                    .build();
            atmLocation = atmLocationRepository.save(atmLocation);
        }


        String latitude = atmLocation.getLA();
        String longitude = atmLocation.getLO();
        String mapsLink = String.format("https://www.google.com/maps?q=%s,%s&output=embed", latitude, longitude);

        return mapAtmsToResponse(atms, pingStatus, atmIP, mapsLink, cashAdd ,cashLeft , atmMonitorStatus.getRemark());
    }



}











