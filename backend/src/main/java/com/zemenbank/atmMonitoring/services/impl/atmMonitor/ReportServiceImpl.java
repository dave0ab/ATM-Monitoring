package com.zemenbank.atmMonitoring.services.impl.atmMonitor;

import com.zemenbank.atmMonitoring.dto.response.AtmsReportResponseDTO;
import com.zemenbank.atmMonitoring.model.atmMonitor.ATMAccesses;
import com.zemenbank.atmMonitoring.model.atmMonitor.ATMCashBalance;
import com.zemenbank.atmMonitoring.model.atmMonitor.AtmStatusReport;
import com.zemenbank.atmMonitoring.model.atmMonitor.PingStatus;
import com.zemenbank.atmMonitoring.model.bankworld.ATMS;
import com.zemenbank.atmMonitoring.repositories.atmMonitor.ATMAccessRepository;
import com.zemenbank.atmMonitoring.repositories.atmMonitor.ATMCashBalanceRepository;
import com.zemenbank.atmMonitoring.repositories.atmMonitor.AtmStatusReportRepository;
import com.zemenbank.atmMonitoring.repositories.atmMonitor.PingStatusRepository;
import com.zemenbank.atmMonitoring.repositories.bankworld.ATMSRepository;
import com.zemenbank.atmMonitoring.security.AuthService;
import com.zemenbank.atmMonitoring.services.BaseServices;
import com.zemenbank.atmMonitoring.services.ReportService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl extends BaseServices implements ReportService {

    private final ATMSRepository atmsRepository;
    private final PingStatusRepository pingStatusRepository;
    private final AuthService authService;
    private final ATMCashBalanceRepository atmsCashBalanceRepository;
    private final AtmStatusReportRepository atmStatusReportRepository;

    private ATMAccessRepository atmAccessRepository;

    public ReportServiceImpl(
            ATMCashBalanceRepository atmCashBalanceRepository,
            ATMSRepository atmsRepository,
            PingStatusRepository pingStatusRepository,
            AuthService authService,
            AtmStatusReportRepository atmStatusReportRepository,
            ATMAccessRepository atmAccessRepository) {
        this.atmsRepository = atmsRepository;
        this.pingStatusRepository = pingStatusRepository;
        this.authService = authService;
        this.atmStatusReportRepository = atmStatusReportRepository;
        this.atmsCashBalanceRepository = atmCashBalanceRepository;
        this.atmAccessRepository = atmAccessRepository;
    }

    private List<String> getAccessibleTIDs() {
        String username = authService.getLoggedInUserId();



        String userRoles = authService.getUserRoles();
        boolean isAdminOrIT = userRoles.contains("ADMIN") || userRoles.contains("IT");

        if (isAdminOrIT) {
            return Collections.emptyList(); // Admin and IT users can access all TIDs
        }

        List<String> allowedTIDs = atmAccessRepository.findByUserId(username).stream()
                .filter(access -> "CHECKED".equalsIgnoreCase(access.getStatus()))
                .map(ATMAccesses::getTID)
                .toList();

        if (allowedTIDs.isEmpty()) {
        } else {

        }
        return allowedTIDs;
    }

    private boolean isAccessible(String atmId, List<String> allowedTIDs) {
        return allowedTIDs.isEmpty() || allowedTIDs.contains(atmId); // Allow all if empty (Admin/IT user)
    }

    @Override
    public List<AtmsReportResponseDTO> generateAllAtmsReport() {
        List<String> allowedTIDs = getAccessibleTIDs();
        List<AtmsReportResponseDTO> report = new ArrayList<>();
        Set<String> processedAtmIds = new HashSet<>();

        List<PingStatus> lineOrAdslStatuses = pingStatusRepository.findBySTATUSIn(List.of("Line/Power Issue", "Only ADSL" , "WTTX (Power Issue)"));
        for (PingStatus pingStatus : lineOrAdslStatuses) {
            String atmId = pingStatus.getTerminalId();
            if (!processedAtmIds.contains(atmId) && isAccessible(atmId, allowedTIDs)) {
                Optional<ATMS> atm = atmsRepository.findByAtmId(atmId);
                if (atm.isPresent()) {
                    Optional<ATMCashBalance> cashBalance = atmsCashBalanceRepository.findByAtmId(atmId);
                    report.add(createReportEntry(atm, pingStatus.getSTATUS(), cashBalance));
                    processedAtmIds.add(atmId);
                }
            }
        }

        List<PingStatus> bothReplyStatuses = pingStatusRepository.findBySTATUS("Both Reply");
        for (PingStatus pingStatus : bothReplyStatuses) {
            String atmId = pingStatus.getTerminalId();
            if (!processedAtmIds.contains(atmId) && isAccessible(atmId, allowedTIDs)) {
                Optional<ATMS> atmOpt = atmsRepository.findByAtmId(atmId);
                atmOpt.ifPresent(atm -> {
                    Optional<ATMCashBalance> cashBalance = atmsCashBalanceRepository.findByAtmId(atmId);
                    report.add(createReportEntry(atmOpt, atm.getActualOpStateDesc(), cashBalance));
                    processedAtmIds.add(atmId);
                });
            }
        }

        List<ATMS> opStateIssueAtms = atmsRepository.findByActualOpStateDescIn(List.of("Downloading", "Closed"));
        for (ATMS atm : opStateIssueAtms) {
            if (!processedAtmIds.contains(atm.getAtmId()) && isAccessible(atm.getAtmId(), allowedTIDs)) {
                Optional<ATMCashBalance> cashBalance = atmsCashBalanceRepository.findByAtmId(atm.getAtmId());
                report.add(createReportEntry(Optional.of(atm), atm.getActualOpStateDesc(), cashBalance));
                processedAtmIds.add(atm.getAtmId());
            }
        }

        List<ATMS> allAtms = atmsRepository.findAll();
        for (ATMS atm : allAtms) {
            if (!processedAtmIds.contains(atm.getAtmId()) && isAccessible(atm.getAtmId(), allowedTIDs)) {
                Optional<ATMCashBalance> cashBalance = atmsCashBalanceRepository.findByAtmId(atm.getAtmId());
                report.add(createReportEntry(Optional.of(atm), "No Issue", cashBalance));
            }
        }

        report.sort(Comparator.comparing(AtmsReportResponseDTO::getAtmName, Comparator.nullsLast(String::compareToIgnoreCase)));
        return report;
    }



    private AtmsReportResponseDTO createReportEntry(Optional<ATMS> atmOpt, String status, Optional<ATMCashBalance> cashBalance) {
        ATMS atm = atmOpt.orElseThrow();
        return AtmsReportResponseDTO.builder()
                .terminalId(atm.getAtmId())
                .atmName(atm.getAtmName())
                .status(status)
                .problem(atm.getDeviceName() != null ? atm.getDeviceName() : "No Issue")
                .cashBalance(cashBalance.map(balance -> balance.getBalance() + " ETB").orElse("N/A"))
                .build();
    }

    public String exportAtmsReportToTxt(String type) {


        List<AtmsReportResponseDTO> reportData = generateReportByType("all_atm");


        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        String fileName = "exportTXT" + timestamp + ".txt";
        String filePath = "exports/" + fileName;

        // Ensure the directory exists
        File directory = new File("exports");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        try {
            // Write the data to the .txt file
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent()); // Ensure the exports directory exists
            StringBuilder txtContent = new StringBuilder();

            // Group by Status and Problem
            Map<String, List<AtmsReportResponseDTO>> statusGrouped = reportData.stream()
                    .filter(dto -> dto.getStatus() != null)
                    .collect(Collectors.groupingBy(AtmsReportResponseDTO::getStatus));

            Map<String, List<AtmsReportResponseDTO>> problemGrouped = reportData.stream()
                    .filter(dto -> dto.getProblem() != null)
                    .collect(Collectors.groupingBy(AtmsReportResponseDTO::getProblem));



            for (String status : Arrays.asList("Only ADSL", "Line/Power Issue", "WTTX (Power Issue)")) {


                List<AtmsReportResponseDTO> atmsInStatus = statusGrouped.getOrDefault(status, Collections.emptyList());
                if (atmsInStatus.isEmpty()) {

                } else {
                    txtContent.append("-------------------\n");
                    txtContent.append("Status: ").append(status).append("\n");
                    for (AtmsReportResponseDTO atm : atmsInStatus) {
                        txtContent.append(atm.getAtmName()).append("\n");
                    }
                }
                txtContent.append("\n");
            }


            for (String problem : Arrays.asList("Cash Dispenser", "Encryptor", "Magnetic Card Reader")) {


                List<AtmsReportResponseDTO> atmsWithProblem = problemGrouped.getOrDefault(problem, Collections.emptyList());
                if (atmsWithProblem.isEmpty()) {

                } else {
                    txtContent.append("-------------------\n");
                    txtContent.append("Problem: ").append(problem).append("\n");
                    for (AtmsReportResponseDTO atm : atmsWithProblem) {
                        txtContent.append(atm.getAtmName()).append("\n");
                    }
                }
                txtContent.append("\n");
            }




            // Write Both Reply but Offline Section
            txtContent.append("Status: Both Reply but Offline\n");
            txtContent.append("-------------------\n");

// Fetch ATMs with "Both Reply" status
            List<PingStatus> bothReplyStatuses = pingStatusRepository.findBySTATUS("Both Reply");


// Fetch all ATMs from the database
            List<ATMS> allAtms = atmsRepository.findAll();

// Filter out ATMs that are already processed


            boolean hasBothReplyOffline = false; // Flag to check if any ATM matches the criteria

            for (PingStatus pingStatus : bothReplyStatuses) {
                String atmId = pingStatus.getTerminalId().trim(); // Trim to avoid whitespace issues

                Optional<ATMS> atmOpt = atmsRepository.findByAtmId(atmId);
                    if (atmOpt.isPresent()) {
                        ATMS atm = atmOpt.get();


                        // Check if the ATM is offline
                        if ("Offline".equalsIgnoreCase(atm.getCommStateDesc().trim())) { // Trim to avoid whitespace issues
                            txtContent.append(atm.getAtmName()).append("\n");
                             // Mark as processed
                            hasBothReplyOffline = true;
                        }
                    } else {

                    }

            }

            txtContent.append("\n");

            txtContent.append("\n");
            txtContent.append("======================================================================================\n");








            for (String status : Arrays.asList("Only ADSL", "Line/Power Issue", "WTTX (Power Issue)")) {


                List<AtmsReportResponseDTO> atmsInStatus = statusGrouped.getOrDefault(status, Collections.emptyList());
                if (atmsInStatus.isEmpty()) {

                } else {
                    txtContent.append("-------------------\n");
                    txtContent.append("Status: ").append(status).append("\n");
                    StringBuilder atmNames = new StringBuilder();
                    for (AtmsReportResponseDTO atm : atmsInStatus) {
                        if (atmNames.length() > 0) {
                            atmNames.append(", "); // Add a comma separator between ATM names
                        }
                        atmNames.append(atm.getAtmName());
                    }
                    txtContent.append(atmNames).append("\n"); // Append the comma-separated ATM names
                }
                txtContent.append("\n");
            }


            for (String problem : Arrays.asList("Cash Dispenser", "Encryptor", "Magnetic Card Reader")) {


                List<AtmsReportResponseDTO> atmsWithProblem = problemGrouped.getOrDefault(problem, Collections.emptyList());
                if (atmsWithProblem.isEmpty()) {

                } else {
                    txtContent.append("-------------------\n");
                    txtContent.append("Problem: ").append(problem).append("\n");
                    StringBuilder atmNames = new StringBuilder();
                    for (AtmsReportResponseDTO atm : atmsWithProblem) {
                        if (atmNames.length() > 0) {
                            atmNames.append(", "); // Add a comma separator between ATM names
                        }
                        atmNames.append(atm.getAtmName());
                    }
                    txtContent.append(atmNames).append("\n"); // Append the comma-separated ATM names
                }
                txtContent.append("\n");
            }

// Write Both Reply but Offline Section



            boolean hasBothReplyOffline1 = false; // Flag to check if any ATM matches the criteria
            StringBuilder atmNames = new StringBuilder(); // To store ATM names as a comma-separated list

            for (PingStatus pingStatus : bothReplyStatuses) {
                String atmId = pingStatus.getTerminalId().trim(); // Trim to avoid whitespace issues


                Optional<ATMS> atmOpt = atmsRepository.findByAtmId(atmId);
                if (atmOpt.isPresent()) {
                    ATMS atm = atmOpt.get();


                    // Check if the ATM is offline
                    if ("Offline".equalsIgnoreCase(atm.getCommStateDesc().trim())) { // Trim to avoid whitespace issues
                        if (atmNames.length() > 0) {
                            atmNames.append(", "); // Add a comma separator before appending the next ATM name
                        }
                        atmNames.append(atm.getAtmName()); // Append the ATM name
                        hasBothReplyOffline1 = true;
                    }
                } else {

                }
            }

// Append the comma-separated ATM names to the txtContent
            if (hasBothReplyOffline1) {
                txtContent.append("Status: Both Reply but Offline\n");
                txtContent.append("-------------------\n");
                txtContent.append(atmNames).append("\n");
            }
            txtContent.append("\n");





            Files.write(path, txtContent.toString().getBytes());

            return "https://aps3.zemenbank.com/ATMNTRAPI/v1/atms/document/downloadFile/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Error while exporting report to TXT", e);
        }
    }




    public String exportAtmsReportToExcel(String type) {



        List<AtmsReportResponseDTO> reportData = generateReportByType(type);


        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        String fileName = type+ "_" + timestamp + ".xlsx";
        String filePath = "exports/" + fileName;

        File directory = new File("exports");
        if (!directory.exists()) {
            directory.mkdirs(); // Creates the directory and any necessary parent directories
        }


            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("ATM Status Report");

                // Define headers dynamically based on report type
                String[] headers;
                switch (type.toLowerCase()) {
                    case "all_atm":
                        headers = new String[]{"Terminal ID", "ATM Name", "Status", "Problem", "Cash Balance"};
                        break;
                    case "problem":
                        headers = new String[]{"Terminal ID", "ATM Name", "Problem"};
                        break;
                    case "cash_balance":
                        headers = new String[]{"Terminal ID", "ATM Name", "Cash Balance"};
                        break;
                    case "operational_status":
                        headers = new String[]{"Terminal ID", "ATM Name", "Operational Status"};
                        break;

                    default:

                        throw new IllegalArgumentException("Invalid report type: " + type);
                }


                // Create header row
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    headerRow.createCell(i).setCellValue(headers[i]);
                }

                // Populate data rows dynamically based on report type
                int rowIdx = 1;
                for (AtmsReportResponseDTO report : reportData) {
                    Row row = sheet.createRow(rowIdx++);
                    int colIdx = 0;

                    row.createCell(colIdx++).setCellValue(report.getTerminalId());
                    row.createCell(colIdx++).setCellValue(report.getAtmName());

                    switch (type.toLowerCase()) {
                        case "all_atm":
                            row.createCell(colIdx++).setCellValue(report.getStatus());
                            row.createCell(colIdx++).setCellValue(report.getProblem());
                            row.createCell(colIdx++).setCellValue(report.getCashBalance());
                            break;
                        case "problem":
                            row.createCell(colIdx++).setCellValue(report.getProblem());
                            break;
                        case "cash_balance":
                            row.createCell(colIdx++).setCellValue(report.getCashBalance());
                            break;
                        case "operational_status":
                            row.createCell(colIdx++).setCellValue(report.getStatus());
                            break;
                    }
                }


                // Autosize columns
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }


                // Write the file to disk
                Path path = Paths.get(filePath);
                Files.createDirectories(path.getParent()); // Ensure the exports directory exists


                try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                    workbook.write(fileOut);

                }

                return "https://aps3.zemenbank.com/ATMNTRAPI/v1/atms/document/downloadFile/" + fileName;
            } catch (IOException e) {

                throw new RuntimeException("Error while exporting report to Excel", e);
            }


    }




@Override
    public List<AtmStatusReport> getAllAtmStatuses() {
        return null;
    }

    @Override
    public Optional<AtmsReportResponseDTO> getAtmStatusByTerminalId(String terminalId) {
        return Optional.empty();
    }


    public List<AtmsReportResponseDTO> generateReportByType(String type) {
        List<String> allowedTIDs = getAccessibleTIDs();

        return switch (type.toLowerCase()) {
            case "all_atm" -> generateAllAtmsReport();
            case "problem" -> generateProblemReport(allowedTIDs);
            case "operational_status" -> generateOperationalStatusReport(allowedTIDs);
            case "cash_balance" -> generateCashBalanceReport(allowedTIDs);
            default -> throw new IllegalArgumentException("Invalid report type: " + type);
        };
    }

    private List<AtmsReportResponseDTO> generateProblemReport(List<String> allowedTIDs) {
        List<ATMS> problemAtms = atmsRepository.findByDeviceNameNotNull();
        Map<String, List<ATMS>> groupedByDeviceName = problemAtms.stream()
                .filter(atm -> isAccessible(atm.getAtmId(), allowedTIDs))
                .collect(Collectors.groupingBy(ATMS::getDeviceName));

        List<AtmsReportResponseDTO> report = new ArrayList<>();
        groupedByDeviceName.forEach((deviceName, atms) -> {

            for (ATMS atm : atms) {
                Optional<ATMCashBalance> cashBalance = atmsCashBalanceRepository.findByAtmId(atm.getAtmId());
                report.add(createReportEntry(Optional.of(atm), deviceName, cashBalance));
            }
        });

        return report;
    }

    private List<AtmsReportResponseDTO> generateOperationalStatusReport(List<String> allowedTIDs) {
        return atmsRepository.findAll().stream()
                .filter(atm -> isAccessible(atm.getAtmId(), allowedTIDs))
                .map(atm -> {
                    Optional<ATMCashBalance> cashBalance = atmsCashBalanceRepository.findByAtmId(atm.getAtmId());
                    return createReportEntry(Optional.of(atm), atm.getActualOpStateDesc(), cashBalance);
                })
                .sorted(Comparator.comparing(AtmsReportResponseDTO::getAtmName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    private List<AtmsReportResponseDTO> generateCashBalanceReport(List<String> allowedTIDs) {
        return atmsCashBalanceRepository.findAll().stream()
                .filter(balance -> isAccessible(balance.getAtmId(), allowedTIDs))
                .map(balance -> {
                    Optional<ATMS> atm = atmsRepository.findByAtmId(balance.getAtmId());
                    return atm.map(atmEntity -> createReportEntry(Optional.of(atmEntity), "Cash Balance", Optional.of(balance))).orElse(null);
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(b -> {
                    try {
                        return Double.parseDouble(b.getCashBalance().replace(" ETB", ""));
                    } catch (NumberFormatException e) {
                        return Double.MAX_VALUE;
                    }
                }))
                .toList();
    }
}

