package com.zemenbank.atmMonitoring.services.impl.scheduled;

import com.zemenbank.atmMonitoring.dto.response.GatewayInfoResponse;
import com.zemenbank.atmMonitoring.enums.EscalationLevels;
import com.zemenbank.atmMonitoring.enums.Roles;
import com.zemenbank.atmMonitoring.model.atmMonitor.*;
import com.zemenbank.atmMonitoring.repositories.atmMonitor.*;
import com.zemenbank.atmMonitoring.services.BaseServices;
import com.zemenbank.atmMonitoring.services.MailService;
import com.zemenbank.atmMonitoring.services.SmsService;
import com.zemenbank.atmMonitoring.services.impl.atmMonitor.MonitoringService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MailNotifierScheduledService extends BaseServices {

    private final MailService mailService;
    private final SmsService  smsService;
    private final MonitoringService monitoringService;
    private final MonitorStatusRepository monitorStatusRepository;
    private final ATMCashBalanceRepository atmsCashBalanceRepository;
    private final GatewayStatusReportRepository gatewayStatusReportRepository;
    private final UserRepository userRepository;
    private final ATMAccessRepository atmAccessRepository;

    public MailNotifierScheduledService(SmsService  smsService,ATMAccessRepository atmAccessRepository ,GatewayStatusReportRepository gatewayStatusReportRepository, ATMCashBalanceRepository atmCashBalanceRepository,
                                        MailService mailService,
                                        MonitoringService monitoringService,
                                        MonitorStatusRepository monitorStatusRepository, UserRepository userRepository) {
        this.mailService = mailService;
        this.monitoringService = monitoringService;
        this.monitorStatusRepository = monitorStatusRepository;
        this.atmsCashBalanceRepository = atmCashBalanceRepository;
        this.userRepository = userRepository;
        this.gatewayStatusReportRepository = gatewayStatusReportRepository;
        this.atmAccessRepository =atmAccessRepository;
        this.smsService =smsService;

    }


    private String generateEscalationEmailContentCashLevel(List<ATMCashBalance> atms, String template) {
        StringBuilder tableContent = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a");
        LocalDateTime now = LocalDateTime.now();

        // Report title
        String reportTitle = "Zemen Bank ATM Cash Level Report - " + now.format(formatter);

        // Add styling
        tableContent.append("<style>")
                .append("table { width: 100%; border-collapse: collapse; font-family: Arial, sans-serif; }")
                .append("thead { background-color: #f43f5e; color: white; }") // rose-600 for headers
                .append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }")
                .append("tbody tr:nth-child(odd) { background-color: #ffe4e6; }") // rose-200 for odd rows
                .append("tbody tr:nth-child(even) { background-color: #fff; }") // white for even rows
                .append("h1, h2 { font-family: Arial, sans-serif; color: #f43f5e; text-align: center; }") // rose-600 for headings
                .append("</style>");

        // Add title and table
        tableContent.append("<div class='header'>")
                .append("<img class='logo' src='https://www.zemenbank.com/images/assets/logo.png' alt='Zemen Bank Logo' >") // Inline image
                .append("<div>")
                .append("<h1>Zemen Bank</h1>")
                .append("<h2>ATM Cash Level Status Notification</h2>")
                .append("<h3>").append(now.format(formatter)).append("</h3>")
                .append("</div>")
                .append("</div>")
                .append("<table>")
                .append("<thead>")
                .append("<tr>")
                .append("<th>ID</th>")
                .append("<th>ATM Name</th>")
                .append("<th>Balance</th>")
                .append("<th>Registered Date</th>")
                .append("<th>Last Transaction</th>")
                .append("<th>Escalation Level</th>")
                .append("<th>Total Time with Low Cash</th>")
                .append("<th>Remark</th>")
                .append("</tr>")
                .append("</thead>")
                .append("<tbody>");


        atms.forEach(atm -> {
            Duration duration = Duration.between(atm.getRegisteredAt(), atm.getSinceDateTime());
            long days = duration.toDays();
            long hours = duration.toHours() % 24;

            Optional<AtmMonitorStatus> amOpt =  monitorStatusRepository.findByTerminalId(String.valueOf(atm.getAtmId()));
            AtmMonitorStatus am = null;
            if(amOpt.isPresent()){

                am = amOpt.get();
            }
            tableContent.append("<tr>")
                    .append("<td>").append(atm.getId()).append("</td>")
                    .append("<td>").append(atm.getName()).append("</td>")
                    .append("<td>").append(String.format("ETB %.2f", atm.getBalance())).append("</td>")
                    .append("<td>").append(atm.getRegisteredAt().format(formatter)).append("</td>")
                    .append("<td>").append(atm.getLastTransaction().format(formatter)).append("</td>")
                    .append("<td>").append(atm.getLevel()).append("</td>")
                    .append("<td>")
                    .append(days).append(" day").append(days != 1 ? "s" : "") // Singular/plural for days
                    .append(" and ")
                    .append(hours).append(" hour").append(hours != 1 ? "s" : "") // Singular/plural for hours
                    .append("</td>")
                    .append("<td>")
                    .append(Optional.ofNullable(am.getRemark()).orElse(""))
                    .append("</td>")
                    .append("</tr>");
        });

        // Close table
        tableContent.append("</tbody></table>");

        // Replace placeholders in the template
        template = template.replace("{{reportTitle}}", reportTitle); // Replace the title placeholder
        template = template.replace("{{reportContent}}", tableContent.toString()); // Replace the content placeholder

        return template;
    }


    public void checkAndNotifyATMCashEscalationLevels() throws IOException {

        try {
            // Fetch all ATM balances
            List<ATMCashBalance> atmBalances = atmsCashBalanceRepository.findAll();

            // Exclude ATMs with WAIT level and group by level
            Map<EscalationLevels, List<ATMCashBalance>> groupedByLevel = atmBalances.stream()
                    .filter(atm -> atm.getLevel() != EscalationLevels.WAIT)
                    .collect(Collectors.groupingBy(ATMCashBalance::getLevel));

            // Prepare email content for each recipient based on escalation level
            Map<String, StringBuilder> emailContentMap = new HashMap<>();

            // Loop through each escalation level and prepare email content
            for (EscalationLevels level : EscalationLevels.values()) {
                if (level == EscalationLevels.WAIT) continue;

                // Include all ATMs relevant for this level and above
                List<ATMCashBalance> relevantATMs = groupedByLevel.entrySet().stream()
                        .filter(entry -> entry.getKey().ordinal() >= level.ordinal()) // Include levels >= current level
                        .flatMap(entry -> entry.getValue().stream())
                        .sorted(Comparator.comparingDouble(atm -> Objects.requireNonNullElse(atm.getBalance(), 0.0)))
                        .collect(Collectors.toList());

                if (!relevantATMs.isEmpty()) {
                    String templatePath = "templates/report.html";
                    String htmlTemplate = Files.readString(Paths.get(templatePath));

                    // Generate email content for this level


                    // Fetch users for this level and filter based on roles and allowed TIDs
                    List<User> users = userRepository.findByNotificationLevel(level.name())
                            .stream()
                            .filter(user -> "ACTIVE".equalsIgnoreCase(user.getStatus()))
                            .collect(Collectors.toList());

                    // Loop through users to send the correct content based on their roles and allowed TIDs
                    for (User user : users) {
                        // Check user roles for filtering ATMs
                        boolean isAdminOrITOrBranch = user.getRole().getValue().contains("ADMIN") || user.getRole().getValue().contains("IT");
                        logger.debug("User roles: {}, isAdminOrITOrBranch: {}", user.getRole().getValue(), isAdminOrITOrBranch);

                        List<String> allowedTIDs;
                        if (!isAdminOrITOrBranch) {
                            allowedTIDs = atmAccessRepository.findByUserId(String.valueOf(user.getUser_id())).stream()
                                    .filter(access -> "CHECKED".equalsIgnoreCase(access.getStatus()))
                                    .map(ATMAccesses::getTID)
                                    .toList();
                            System.out.println(allowedTIDs);

                            if (allowedTIDs.isEmpty()) {
                                logger.warn("No allowed TIDs found for user: {}", user.getDomainId());
                                continue; // Skip the user if they have no allowed TIDs
                            }

                            // Filter relevant ATMs based on allowed TIDs for non-admin/IT/BRANCH users
                            relevantATMs = relevantATMs.stream()
                                    .filter(atm -> allowedTIDs.contains(atm.getAtmId()))  // Filtering ATMs by allowed TID
                                    .collect(Collectors.toList());
                        } else {
                            allowedTIDs = new ArrayList<>();
                        }

                        // Generate and send email content only if there are relevant ATMs
                        if (!relevantATMs.isEmpty()) {

                            String emailContent = generateEscalationEmailContentCashLevel(relevantATMs, htmlTemplate);

                            emailContentMap.computeIfAbsent(user.getDomainId(), k -> new StringBuilder())
                                    .append(emailContent).append("<hr>");
                        }
                    }
                }
            }

            // Send combined emails to each recipient
            for (Map.Entry<String, StringBuilder> entry : emailContentMap.entrySet()) {
                String recipient = entry.getKey();
                String combinedContent = entry.getValue().toString();

                try {

                    Optional<User> userOpt = userRepository.findByDomainId(recipient);
                    User user = userOpt.get();
                    if(user.getEmailSubscribe()) {


                        mailService.sendMail("ATM Cash Escalation Notifications", combinedContent, recipient);

                        smsService.sendSms(combinedContent, user.getPhoneNumber());
                    }
                } catch (Exception e) {
                    System.err.println("Failed to send email to " + recipient + ": " + e.getMessage());
                    // Log and continue processing
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Scheduled(fixedRate = 5000)
    public void saveBalances() {
        monitoringService.saveATMSCashBalance();
        monitoringService.saveGatewayStatus();

    }


    //---------------------------------------------------------------------------------------------



    public void checkAndNotifyATMStatus() {
        try {
            // Fetch monitor statuses and exclude WAIT level
            List<AtmMonitorStatus> monitorStatuses = monitorStatusRepository.findAll();
            Map<EscalationLevels, List<AtmMonitorStatus>> groupedByLevel = monitorStatuses.stream()
                    .filter(status -> status.getLevel() != EscalationLevels.WAIT)
                    .collect(Collectors.groupingBy(AtmMonitorStatus::getLevel));

            // Prepare email content for each recipient based on escalation level
            Map<String, StringBuilder> emailContentMap = new HashMap<>();

            // Loop through each escalation level and prepare email content
            for (EscalationLevels level : EscalationLevels.values()) {
                if (level == EscalationLevels.WAIT) continue;

                // Include all cases relevant for this level and above
                List<AtmMonitorStatus> relevantStatuses = groupedByLevel.entrySet().stream()
                        .filter(entry -> entry.getKey().ordinal() >= level.ordinal()) // Include levels >= current level
                        .flatMap(entry -> entry.getValue().stream())
                        .sorted((s1, s2) -> s2.getSinceDateTime().compareTo(s1.getSinceDateTime())) // Sort by `sinceDateTime`
                        .collect(Collectors.toList());

                if (!relevantStatuses.isEmpty()) {
                    // Load the HTML template
                    String templatePath = "templates/report.html";
                    String htmlTemplate = Files.readString(Paths.get(templatePath));

                    // Generate categorized report content


                    // Fetch users for this level and filter based on roles and allowed TIDs
                    List<User> users = userRepository.findByNotificationLevel(level.name())
                            .stream()
                            .filter(user -> "ACTIVE".equalsIgnoreCase(user.getStatus()))
                            .collect(Collectors.toList());

                    // Loop through users to send the correct content based on their roles and allowed TIDs
                    for (User user : users) {
                        // Check user roles for filtering ATMs
                        boolean isAdminOrITOrBranch =user.getRole().getValue().contains("ADMIN") || user.getRole().getValue().contains("IT");
                        logger.debug("User roles: {}, isAdminOrITOrBranch: {}", user.getRole().getValue(), isAdminOrITOrBranch);

                        List<String> allowedTIDs;
                        if (!isAdminOrITOrBranch) {
                            allowedTIDs = atmAccessRepository.findByUserId(String.valueOf(user.getUser_id())).stream()
                                    .filter(access -> "CHECKED".equalsIgnoreCase(access.getStatus()))
                                    .map(ATMAccesses::getTID)
                                    .toList();
                            logger.debug("Allowed TIDs for user {}: {}", user.getDomainId(), allowedTIDs);

                            if (allowedTIDs.isEmpty()) {
                                logger.warn("No allowed TIDs found for user: {}", user.getDomainId());
                                continue; // Skip the user if they have no allowed TIDs
                            }

                            // Filter relevant ATMs based on allowed TIDs for non-admin/IT/BRANCH users
                            relevantStatuses = relevantStatuses.stream()
                                    .filter(status -> allowedTIDs.contains(status.getTerminalId()))  // Filtering ATMs by allowed TID
                                    .collect(Collectors.toList());
                        } else {
                            allowedTIDs = new ArrayList<>();
                        }

                        // Generate and send email content only if there are relevant ATMs
                        if (!relevantStatuses.isEmpty()) {

                            String emailContent = generateCategorizedStatusReport(relevantStatuses, htmlTemplate);

                            emailContentMap.computeIfAbsent(user.getDomainId(), k -> new StringBuilder())
                                    .append(emailContent).append("<hr>");
                        }
                    }
                }
            }

            // Send combined emails to each recipient
            for (Map.Entry<String, StringBuilder> entry : emailContentMap.entrySet()) {
                String recipient = entry.getKey();
                String combinedContent = entry.getValue().toString();

                try {

                    Optional<User> userOpt = userRepository.findByDomainId(recipient);
                    User user = userOpt.get();

                    if(user.getEmailSubscribe()) {
                        mailService.sendMail("ATM Status Notifications", combinedContent, recipient);

                        smsService.sendSms(combinedContent, user.getPhoneNumber());

                    }
                } catch (Exception e) {
                    System.err.println("Failed to send email to " + recipient + ": " + e.getMessage());
                    // Log and continue processing
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private String generateCategorizedStatusReport(List<AtmMonitorStatus> statuses, String template) {
        StringBuilder categorizedContent = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a");

        LocalDateTime now = LocalDateTime.now();
        String reportTitle = "Zemen Bank ATM Report - " + now.format(formatter);

        // Add styling


        // Add report title
        categorizedContent.append("<div class='header'>")
                .append("<img class='logo' src='https://www.zemenbank.com/images/assets/logo.png' alt='Zemen Bank Logo' >") // Inline image
                .append("<div>")
                .append("<h1>Zemen Bank</h1>")
                .append("<h2>ATM Status Notification</h2>")
                .append("<h3>").append(now.format(formatter)).append("</h3>")
                .append("<br>")
                .append("<br>")
                .append("</div>")
                .append("</div>");

        // Group statuses by `status` field
        Map<String, List<AtmMonitorStatus>> groupedByStatus = statuses.stream()
                .collect(Collectors.groupingBy(AtmMonitorStatus::getStatus));

        groupedByStatus.forEach((status, statusList) -> {

            categorizedContent.append("<style>")
                    .append("table { width: 100%; border-collapse: collapse; font-family: Arial, sans-serif; }")
                    .append("thead { background-color: #f43f5e; color: white; }")
                    .append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }")
                    .append("tbody tr:nth-child(odd) { background-color: #ffe4e6; }") // rose-200 for odd rows
                    .append("tbody tr:nth-child(even) { background-color: #fff; }") // white for even rows
                    .append("h1, h2 { font-family: Arial, sans-serif; color: #f43f5e; text-align: center; }") // rose-600 for headings
                    .append("</style>");


            categorizedContent.append("<h3>Status: ").append(status).append("</h3>")
                    .append("<table>")
                    .append("<thead>")
                    .append("<tr>")
                    .append("<th>ID</th>")
                    .append("<th>ATM Name</th>")
                    .append("<th>Registered At</th>")
                    .append("<th>Last Transaction</th>")
                    .append("<th>Total Down Time</th>")
                    .append("<th>Escalation Level</th>")
                    .append("<th>Remark</th>")
                    .append("</tr>")
                    .append("</thead>")
                    .append("<tbody>");

            // Populate table rows
            statusList.forEach(statusObj -> {
                Duration duration = Duration.between(statusObj.getRegisteredAt(), statusObj.getSinceDateTime());
                long days = duration.toDays();
                long hours = duration.toHours() % 24;

                categorizedContent.append("<tr>")
                        .append("<td>").append(statusObj.getId()).append("</td>")
                        .append("<td>").append(statusObj.getAtmName()).append("</td>")
                        .append("<td>").append(statusObj.getSinceDateTime().format(formatter)).append("</td>")
                        .append("<td>").append(statusObj.getLastTransaction().format(formatter)).append("</td>")
                        .append("<td>")
                        .append(days).append(" day").append(days != 1 ? "s" : "") // Handle plural for days
                        .append(" and ")
                        .append(hours).append(" hour").append(hours != 1 ? "s" : "") // Handle plural for hours
                        .append("</td>")
                        .append("<td>").append(statusObj.getLevel()).append("</td>")
                        .append("<td>").append(statusObj.getRemark()).append("</td>")
                        .append("</tr>");
            });

            categorizedContent.append("</tbody></table>");
        });

        // Replace placeholders in the template
        template = template.replace("{{reportTitle}}", reportTitle); // Replace the title placeholder
        template = template.replace("{{reportContent}}", categorizedContent.toString()); // Replace the content placeholder

        return template;
    }


    //---------------------------------------------------------------------------------------------


    @Scheduled(fixedRate = 300000)
    public void escalateAndNotifyGatewayStatus() {
        try {
            // Fetch all monitor statuses
            List<GatewayMonitorStatus> monitorStatuses = gatewayStatusReportRepository.findAll();
            if (monitorStatuses.isEmpty()) {
                System.out.println("No gateway monitor statuses to process.");
                return;
            }

            Map<EscalationLevels, List<GatewayMonitorStatus>> groupedByLevel = new HashMap<>();

            // Iterate through all statuses and update escalation levels
            for (GatewayMonitorStatus status : monitorStatuses) {
                // Exclude statuses with "Online All"
                if (status.getCombinedStatus().contains("Online All")) {
                    continue;
                }

                Duration downtime = Duration.between(status.getSinceDateTime(), LocalDateTime.now());
                long minutes = downtime.toMinutes();

                // Determine escalation level dynamically

                status.setLevel(status.getLevel());

                // Group by escalation level for notification
                groupedByLevel.computeIfAbsent(status.getLevel(), k -> new ArrayList<>()).add(status);

                // Save updated status back to the repository
                gatewayStatusReportRepository.save(status);
            }

            // Notify users for each escalation level
            for (Map.Entry<EscalationLevels, List<GatewayMonitorStatus>> entry : groupedByLevel.entrySet()) {
                EscalationLevels currentLevel = entry.getKey();
                List<GatewayMonitorStatus> statuses = entry.getValue();

                // Include users for current level and all lower levels
                List<User> users = userRepository.findAll().stream()
                        .filter(user -> user.getRole() == Roles.ADMIN || user.getRole() == Roles.IT)
                        .filter(user -> EscalationLevels.valueOf(user.getNotificationLevel()).ordinal() <= currentLevel.ordinal())
                        .filter(user -> "ACTIVE".equalsIgnoreCase(user.getStatus()))
                        .collect(Collectors.toList());

                for (User user : users) {

                        String emailContent = generateEmailContent(statuses);
                        try {
                            mailService.sendMail("Gateway Escalation Alert: " + currentLevel.name(), emailContent, user.getDomainId());
                            System.out.println("Notification sent to " + user.getDomainId());

                            smsService.sendSms(emailContent, user.getPhoneNumber());
                        } catch (Exception e) {
                            System.err.println("Failed to send email to " + user.getDomainId() + ": " + e.getMessage());
                        }


                }
            }
        } catch (Exception e) {
            System.err.println("Error during escalateAndNotifyGatewayStatus execution: " + e.getMessage());
        }
    }

    // Determine escalation level based on downtime


    // Generate email content
    private String generateEmailContent(List<GatewayMonitorStatus> statuses) {
        StringBuilder emailContent = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a");
        LocalDateTime now = LocalDateTime.now();

        // Define network translations
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

        // HTML styling
        emailContent.append("<style>")
                .append("table { width: 100%; border-collapse: collapse; font-family: Arial, sans-serif; }")
                .append("thead { background-color: #f43f5e; color: white; }") // rose-600 for headers
                .append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }")
                .append("tbody tr:nth-child(odd) { background-color: #ffe4e6; }") // rose-200 for odd rows
                .append("tbody tr:nth-child(even) { background-color: #fff; }") // white for even rows
                .append("h1, h2 { font-family: Arial, sans-serif; color: #f43f5e; text-align: center; }") // rose-600 for headings
                .append("</style>");

        // Title and subtitle
        emailContent.append("<div class='header'>")
                .append("<img class='logo' src='https://www.zemenbank.com/images/assets/logo.png' alt='Zemen Bank Logo' width='60' height='60'>") // Inline image
                .append("<div>")
                .append("<h1>Zemen Bank</h1>")
                .append("<h2>Gateway Status Notification</h2>")
                .append("<h3>").append(now.format(formatter)).append("</h3>")
                .append("</div>")
                .append("</div>")
                .append("<table>")
                .append("<thead><tr>")
                .append("<th>Network</th>")
                .append("<th>Translated Name</th>")
                .append("<th>Problem</th>")
                .append("<th>Downtime</th>")
                .append("<th>Escalation Level</th>")
                .append("</tr></thead><tbody>");

        // Populate table rows with network translations
        for (GatewayMonitorStatus status : statuses) {
            String translatedName = networkTranslations.getOrDefault(status.getNetwork(), "Unknown Network");

            Duration downtime = Duration.between(status.getRegisteredAt(), LocalDateTime.now());
            long hours = downtime.toHours();
            long minutes = downtime.toMinutes() % 60;

            emailContent.append("<tr>")
                    .append("<td>").append(status.getNetwork()).append("</td>")
                    .append("<td>").append(translatedName).append("</td>")
                    .append("<td>").append(status.getCombinedStatus().contains(":") ? status.getCombinedStatus().split(":", 2)[1].trim() : status.getCombinedStatus()).append("</td>")
                    .append("<td>").append(hours).append(" hours, ").append(minutes).append(" minutes").append("</td>")
                    .append("<td>").append(status.getLevel().name()).append("</td>")
                    .append("</tr>");
        }

        // Close table
        emailContent.append("</tbody></table>");
        return emailContent.toString();
    }






    ///////////////////////   Schedule Part




    @Scheduled(cron = "0 0 8 * * ?", zone = "Africa/Addis_Ababa")
    public void triggerAt8() throws IOException {
        checkAndNotifyATMStatus();
        checkAndNotifyATMCashEscalationLevels();
    }

    @Scheduled(cron = "0 30 11 * * ?", zone = "Africa/Addis_Ababa")
    public void triggerAt1130() throws IOException {
        checkAndNotifyATMStatus();
        checkAndNotifyATMCashEscalationLevels();
    }

    @Scheduled(cron = "0 0 13 * * ?", zone = "Africa/Addis_Ababa")
    public void triggerAt3() throws IOException {
        checkAndNotifyATMStatus();
        checkAndNotifyATMCashEscalationLevels();
    }

    @Scheduled(cron = "0 0 15 * * ?", zone = "Africa/Addis_Ababa")
    public void triggerAt1() throws IOException {
        checkAndNotifyATMStatus();
        checkAndNotifyATMCashEscalationLevels();
    }

    @Scheduled(cron = "0 0 17 * * ?", zone = "Africa/Addis_Ababa")
    public void triggerAt5() throws IOException {
        checkAndNotifyATMStatus();
        checkAndNotifyATMCashEscalationLevels();
    }


    @Scheduled(cron = "0 0 9-17 * * ?", zone = "Africa/Addis_Ababa")
    public void triggerAtOmniChannel() throws IOException {
        checkAndNotifyATMStatusOmniChannel();
        checkAndNotifyATMCashEscalationLevelsOmniChannel();
    }



    // For Omni Channel

    public void checkAndNotifyATMStatusOmniChannel() {
        try {
            // Fetch monitor statuses and exclude WAIT level
            List<AtmMonitorStatus> monitorStatuses = monitorStatusRepository.findAll();
            Map<EscalationLevels, List<AtmMonitorStatus>> groupedByLevel = monitorStatuses.stream()
                    .filter(status -> status.getLevel() != EscalationLevels.WAIT)
                    .collect(Collectors.groupingBy(AtmMonitorStatus::getLevel));

            // Prepare email content for each recipient based on escalation level
            Map<String, StringBuilder> emailContentMap = new HashMap<>();

            // Loop through each escalation level and prepare email content
            for (EscalationLevels level : EscalationLevels.values()) {
                if (level == EscalationLevels.WAIT) continue;

                // Include all cases relevant for this level and above
                List<AtmMonitorStatus> relevantStatuses = groupedByLevel.entrySet().stream()
                        .filter(entry -> entry.getKey().ordinal() >= level.ordinal()) // Include levels >= current level
                        .flatMap(entry -> entry.getValue().stream())
                        .sorted((s1, s2) -> s2.getSinceDateTime().compareTo(s1.getSinceDateTime())) // Sort by `sinceDateTime`
                        .collect(Collectors.toList());

                if (!relevantStatuses.isEmpty()) {
                    // Load the HTML template
                    String templatePath = "templates/report.html";
                    String htmlTemplate = Files.readString(Paths.get(templatePath));

                    // Fetch users for this level and filter based on roles and allowed TIDs
                    List<User> users = userRepository.findByNotificationLevel(level.name())
                            .stream()
                            .filter(user -> "ACTIVE".equalsIgnoreCase(user.getStatus()))
                            .collect(Collectors.toList());

                    // Loop through users to send the correct content based on their roles and allowed TIDs
                    for (User user : users) {
                        boolean isAdminOrITOrBranch = user.getRole().getValue().contains("ADMIN") ||
                                user.getRole().getValue().contains("IT") &&  user.getNotificationLevel().equals("FIRST") ;
                        logger.debug("User roles: {}, isAdminOrITOrBranch: {}", user.getRole().getValue(), isAdminOrITOrBranch);

                        // Skip ADMIN and IT users entirely
                        if (isAdminOrITOrBranch) {
                            logger.debug("Skipping email generation for ADMIN/IT user: {}", user.getDomainId());
                            continue;
                        }

                        // Fetch allowed TIDs for non-ADMIN/IT users
                        List<String> allowedTIDs = atmAccessRepository.findByUserId(String.valueOf(user.getUser_id())).stream()
                                .filter(access -> "CHECKED".equalsIgnoreCase(access.getStatus()))
                                .map(ATMAccesses::getTID)
                                .toList();
                        logger.debug("Allowed TIDs for user {}: {}", user.getDomainId(), allowedTIDs);

                        if (allowedTIDs.isEmpty()) {
                            logger.warn("No allowed TIDs found for user: {}", user.getDomainId());
                            continue; // Skip the user if they have no allowed TIDs
                        }

                        // Filter relevant statuses for this user
                        List<AtmMonitorStatus> userRelevantStatuses = relevantStatuses.stream()
                                .filter(status -> allowedTIDs.contains(status.getTerminalId()))
                                .collect(Collectors.toList());

                        // Generate and send email content only if there are relevant statuses
                        if (!userRelevantStatuses.isEmpty()) {
                            String emailContent = generateCategorizedStatusReport(userRelevantStatuses, htmlTemplate);

                            emailContentMap.computeIfAbsent(user.getDomainId(), k -> new StringBuilder())
                                    .append(emailContent).append("<hr>");
                        }
                    }
                }
            }

            // Send combined emails to each recipient
            for (Map.Entry<String, StringBuilder> entry : emailContentMap.entrySet()) {
                String recipient = entry.getKey();
                String combinedContent = entry.getValue().toString();

                try {
                    Optional<User> userOpt = userRepository.findByDomainId(recipient);
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        boolean isAdminOrITOrBranch = user.getRole().getValue().contains("ADMIN") ||
                                user.getRole().getValue().contains("IT") || !user.getNotificationLevel().equals("FIRST");

                        // Skip ADMIN and IT users during email sending
                        if (isAdminOrITOrBranch) {
                            logger.debug("Skipping email sending for ADMIN/IT user: {}", user.getDomainId());
                            continue;
                        }

                        if(user.getEmailSubscribe()) {
                        // Send email
                        mailService.sendMail("ATM Status Notifications", combinedContent, recipient);


                        // Send SMS if phone number exists
                        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
                            smsService.sendSms(combinedContent, user.getPhoneNumber());
                        } else {
                            logger.warn("Phone number is missing for user: {}", user.getDomainId());
                        }
                    }


                }
                } catch (Exception e) {
                    logger.error("Failed to send email/SMS to {}: {}", recipient, e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            logger.error("Error reading email template file: {}", e.getMessage(), e);
        }
    }


    public void checkAndNotifyATMCashEscalationLevelsOmniChannel() throws IOException {
        try {
            // Fetch all ATM balances
            List<ATMCashBalance> atmBalances = atmsCashBalanceRepository.findAll();

            // Exclude ATMs with WAIT level and group by level
            Map<EscalationLevels, List<ATMCashBalance>> groupedByLevel = atmBalances.stream()
                    .filter(atm -> atm.getLevel() != EscalationLevels.WAIT)
                    .collect(Collectors.groupingBy(ATMCashBalance::getLevel));

            // Prepare email content for each recipient based on escalation level
            Map<String, StringBuilder> emailContentMap = new HashMap<>();

            // Loop through each escalation level and prepare email content
            for (EscalationLevels level : EscalationLevels.values()) {
                if (level == EscalationLevels.WAIT) continue;

                // Include all ATMs relevant for this level and above
                List<ATMCashBalance> relevantATMs = groupedByLevel.entrySet().stream()
                        .filter(entry -> entry.getKey().ordinal() >= level.ordinal()) // Include levels >= current level
                        .flatMap(entry -> entry.getValue().stream())
                        .sorted(Comparator.comparingDouble(atm -> Objects.requireNonNullElse(atm.getBalance(), 0.0)))
                        .collect(Collectors.toList());

                if (!relevantATMs.isEmpty()) {
                    String templatePath = "templates/report.html";
                    String htmlTemplate = Files.readString(Paths.get(templatePath));

                    // Fetch users for this level and filter based on roles and allowed TIDs
                    List<User> users = userRepository.findByNotificationLevel(level.name())
                            .stream()
                            .filter(user -> "ACTIVE".equalsIgnoreCase(user.getStatus()))
                            .collect(Collectors.toList());

                    // Loop through users to send the correct content based on their roles and allowed TIDs
                    for (User user : users) {
                        boolean isAdminOrITOrBranch = user.getRole().getValue().contains("ADMIN") ||
                                user.getRole().getValue().contains("IT") || !user.getNotificationLevel().equals("FIRST") ;

                        // Skip ADMIN and IT users entirely
                        if (isAdminOrITOrBranch) {
                            logger.debug("Skipping email generation for ADMIN/IT user: {}", user.getDomainId());
                            continue;
                        }

                        // Fetch allowed TIDs for non-ADMIN/IT users
                        List<String> allowedTIDs = atmAccessRepository.findByUserId(String.valueOf(user.getUser_id())).stream()
                                .filter(access -> "CHECKED".equalsIgnoreCase(access.getStatus()))
                                .map(ATMAccesses::getTID)
                                .toList();
                        logger.debug("Allowed TIDs for user {}: {}", user.getDomainId(), allowedTIDs);

                        if (allowedTIDs.isEmpty()) {
                            logger.warn("No allowed TIDs found for user: {}", user.getDomainId());
                            continue; // Skip the user if they have no allowed TIDs
                        }

                        // Filter relevant ATMs for this user
                        List<ATMCashBalance> userRelevantATMs = relevantATMs.stream()
                                .filter(atm -> allowedTIDs.contains(atm.getAtmId()))
                                .collect(Collectors.toList());

                        // Generate and send email content only if there are relevant ATMs
                        if (!userRelevantATMs.isEmpty()) {
                            String emailContent = generateEscalationEmailContentCashLevel(userRelevantATMs, htmlTemplate);

                            emailContentMap.computeIfAbsent(user.getDomainId(), k -> new StringBuilder())
                                    .append(emailContent).append("<hr>");
                        }
                    }
                }
            }

            // Send combined emails to each recipient
            for (Map.Entry<String, StringBuilder> entry : emailContentMap.entrySet()) {
                String recipient = entry.getKey();
                String combinedContent = entry.getValue().toString();

                try {
                    Optional<User> userOpt = userRepository.findByDomainId(recipient);
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        boolean isAdminOrITOrBranch = user.getRole().getValue().contains("ADMIN") ||
                                user.getRole().getValue().contains("IT");

                        // Skip ADMIN and IT users during email sending
                        if (isAdminOrITOrBranch) {
                            logger.debug("Skipping email sending for ADMIN/IT user: {}", user.getDomainId());
                            continue;
                        }
                        if(user.getEmailSubscribe()) {
                            // Send email
                            mailService.sendMail("ATM Cash Escalation Notifications", combinedContent, recipient);

                            // Send SMS if phone number exists
                            if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
                                smsService.sendSms(combinedContent, user.getPhoneNumber());
                            } else {
                                logger.warn("Phone number is missing for user: {}", user.getDomainId());
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to send email/SMS to {}: {}", recipient, e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            logger.error("Error reading email template file: {}", e.getMessage(), e);
            throw e;
        }
    }


}