
package com.zemenbank.atmMonitoring.services.impl.atmMonitor;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.zemenbank.atmMonitoring.dto.response.TransactionRecordResponse;
import com.zemenbank.atmMonitoring.exception.TransactionKey;
import com.zemenbank.atmMonitoring.model.atmMonitor.TransactionRecord;
import com.zemenbank.atmMonitoring.repositories.atmMonitor.TransactionRepository;
import com.zemenbank.atmMonitoring.services.BaseServices;
import com.zemenbank.atmMonitoring.services.TransactionRecordService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static com.zemenbank.atmMonitoring.utils.TransactionRecordSpecifications.withDynamicFilters;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl extends BaseServices implements TransactionRecordService {

    private final RestTemplate restTemplate;
    @Autowired
    private final TransactionRepository transactionRepository;



    private final ObjectMapper objectMapper;

    // Corrected URL: Removed space after 'T00'
    private static final String GATEWAY_URL =
            "https://test-gateway.mastercard.com/history/version/1/mso/ZEMEN_MMID/transaction"
                    + "?timeOfRecord.start=2025-04-16T00:00:00Z"
                    + "&timeOfRecord.end=2025-05-18T00:00:00Z"
                    + "&columns=merchant,timeOfRecord,order.id,transaction.id,"
                    + "sourceOfFunds.provided.card.number,sourceOfFunds.provided.card.expiry.month,"
                    + "sourceOfFunds.provided.card.expiry.year,transaction.amount,transaction.currency,"
                    + "transaction.type,transaction.acquirer.id,response.acquirerCode,response.gatewayCode,"
                    + "sourceOfFunds.provided.card.nameOnCard,sourceOfFunds.provided.card.issuer,"
                    + "sourceOfFunds.provided.card.brand,sourceOfFunds.provided.card.accountType,"
                    + "sourceOfFunds.provided.card.fundingMethod,transaction.authenticationStatus,"
                    + "order.status,authentication.version,device.ipAddress,customer.firstName,"
                    + "customer.lastName,customer.email,order.description,response.gatewayRecommendation,response.acquirerMessage";

    private String buildGatewayUrl(String start, String end) {
        return "https://ap-gateway.mastercard.com/history/version/1/mso/ZEMEN_MMID/transaction" +
                "?timeOfRecord.start=" + start +
                "&timeOfRecord.end=" + end +
                "&columns=merchant,timeOfRecord,order.id,transaction.id," +
                "sourceOfFunds.provided.card.number,sourceOfFunds.provided.card.expiry.month," +
                "sourceOfFunds.provided.card.expiry.year,transaction.amount,transaction.currency," +
                "transaction.type,transaction.acquirer.id,response.acquirerCode,response.gatewayCode," +
                "sourceOfFunds.provided.card.nameOnCard,sourceOfFunds.provided.card.issuer," +
                "sourceOfFunds.provided.card.brand,sourceOfFunds.provided.card.accountType," +
                "sourceOfFunds.provided.card.fundingMethod,transaction.authenticationStatus," +
                "order.status,authentication.version,device.ipAddress,customer.firstName," +
                "customer.lastName,customer.email,order.description,response.gatewayRecommendation,authentication.3ds2.requestorName,response.acquirerMessage,risk.response.gatewayCode";
    }

    @Value("${mso.username}")
    private String username;

    @Value("${mso.password}")
    private String password;

    public void fetchAndStoreTransactions() {
        try {
            TransactionRecord latestRecord = transactionRepository.findTopByOrderByKey_TimeOfRecordDesc();
            OffsetDateTime lastSaved = latestRecord != null ? latestRecord.getKey().getTimeOfRecord() : null;

            if (lastSaved == null) {
                lastSaved = OffsetDateTime.of(2024, 6, 1, 0, 0, 0, 0, ZoneOffset.UTC);
                System.out.println("Starting from: " + lastSaved);
            }

           OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(2);
            String startTime = lastSaved.toString();
            String endTime = now.toString();

            String dynamicUrl = buildGatewayUrl(startTime, endTime);
            System.out.println(dynamicUrl);


            String auth = username + ":" + password;
            String encodedAuth = Base64Utils.encodeToString(auth.getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + encodedAuth);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    dynamicUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                String csvBody = response.getBody();
                List<TransactionRecord> records = parseCsvToRecords(csvBody);

                // Filter out already saved transactions
                List<TransactionRecord> newRecords = records.stream()
                        .filter(record -> !existsInDatabase(record))
                        .collect(Collectors.toList());

                // Enrich PAYMENT records with merchant name from AUTHENTICATION
                for (TransactionRecord payment : newRecords) {
                    boolean b = payment.getMerchantName() == null || payment.getMerchantName().isBlank();
                    if ("PAYMENT".equalsIgnoreCase(payment.getKey().getTransactionType())
                            && (payment.getMerchantName() == null || payment.getMerchantName().isBlank())) {



                        newRecords.stream()
                                .filter(authh -> "AUTHENTICATION".equalsIgnoreCase(authh.getKey().getTransactionType())
                                        && authh.getKey().getOrderId().equals(payment.getKey().getOrderId())
                                        && authh.getAmount().compareTo(payment.getAmount()) == 0
                                        && authh.getCardNumber().equals(payment.getCardNumber())
                                        && authh.getMerchantName() != null && !authh.getMerchantName().isBlank())
                                .findFirst()
                                .ifPresent(authh -> {
                                    payment.setMerchantName(authh.getMerchantName());
                                });
                    }
                }

                if (!newRecords.isEmpty()) {

                    transactionRepository.saveAll(newRecords);
                    fixMissingMerchantNames();
                } else {
                    System.out.println("No new transactions found.");
                }
            } else {
                System.err.println("Failed to fetch data from gateway. Status code: "
                        + response.getStatusCodeValue());
            }

        } catch (Exception e) {
            System.err.println("Error fetching or saving transactions: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private boolean existsInDatabase(TransactionRecord record) {
        TransactionKey key = new TransactionKey(
                record.getKey().getOrderId(),
                record.getKey().getOrderDescription(),
                record.getKey().getTransactionType(),
                record.getKey().getTimeOfRecord()
        );

        return transactionRepository.existsById(key); // Not existsByKey
    }


    private List<TransactionRecord> parseCsvToRecords(String csv) throws IOException {
        Reader reader = new StringReader(csv);
        CSVParser parser = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withIgnoreHeaderCase()
                .withTrim()
                .parse(reader);

        List<TransactionRecord> records = new ArrayList<>();

        for (CSVRecord row : parser) {
            TransactionRecord record = new TransactionRecord();

            // ✅ Create and populate TransactionKey
            TransactionKey key = new TransactionKey();
            key.setOrderId(row.get("order.id"));
            key.setOrderDescription(row.get("order.description"));
            key.setTransactionType(row.get("transaction.type"));
            key.setTimeOfRecord(OffsetDateTime.parse(row.get("timeOfRecord")));

            // ✅ Set the key into the record
            record.setKey(key);

            // ✅ Populate the rest of the record
            record.setTransactionId(row.get("transaction.id"));
            record.setMerchant(row.get("merchant"));
            record.setCardNumber(row.get("sourceOfFunds.provided.card.number"));
            record.setCardExpiryMonth(row.get("sourceOfFunds.provided.card.expiry.month"));
            record.setCardExpiryYear(row.get("sourceOfFunds.provided.card.expiry.year"));
            record.setAcquirer(row.get("transaction.acquirer.id"));
            record.setAmount(new BigDecimal(row.get("transaction.amount")));
            record.setCurrency(row.get("transaction.currency"));
            record.setAcquirerCode(row.get("response.acquirerCode"));
            record.setCode(row.get("response.gatewayCode"));
            record.setCardholderName(row.get("sourceOfFunds.provided.card.nameOnCard"));
            record.setIssuer(row.get("sourceOfFunds.provided.card.issuer"));
            record.setBrand(row.get("sourceOfFunds.provided.card.brand"));
            record.setAccountType(row.get("sourceOfFunds.provided.card.accountType"));
            record.setFundingMethod(row.get("sourceOfFunds.provided.card.fundingMethod"));
            record.setAuthenticationStatus(row.get("transaction.authenticationStatus"));
            record.setOrderStatus(row.get("order.status"));
            record.setThreeDsVersion(row.get("authentication.version"));
            record.setIpAddress(row.get("device.ipAddress"));
            record.setFirstName(row.get("customer.firstName"));
            record.setLastName(row.get("customer.lastName"));
            record.setEmail(row.get("customer.email"));
            record.setGatewayRecommendation(row.get("response.gatewayRecommendation"));
            record.setMerchantName(row.get("authentication.3ds2.requestorName"));
            String acquirerMessage = row.get("response.acquirerMessage");
            String riskMessage= row.get("risk.response.gatewayCode");

            if (!acquirerMessage.isEmpty()) {
                record.setResponseMessage(acquirerMessage);
            } else if (!riskMessage.isEmpty()) {
                record.setResponseMessage("IP Risk "+riskMessage.toLowerCase());
            }
            else{

                record.setResponseMessage(null);

            }







            records.add(record);
        }

        return records;
    }






    @Override
    public Page<TransactionRecordResponse> getAllTransactions(
            int pageNumber,
            int pageSize,
            String orderStatus,
            String search,
            String brand,
            String transactionType,
            String currency,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

        int zeroBasedPage = pageNumber; // ✅ page 0 is valid

        List<TransactionRecord> filteredRecords = transactionRepository.findAll(
                withDynamicFilters(orderStatus, brand, transactionType, currency, search, startDate, endDate)
        );

        Map<String, List<TransactionRecord>> groupedByOrderId = filteredRecords.stream()
                .collect(Collectors.groupingBy(tr ->
                        Optional.ofNullable(tr.getKey().getOrderId()).orElse("NO_ORDER_ID")));

        List<TransactionRecord> preferredRecords = groupedByOrderId.values().stream()
                .map(records -> records.stream()
                        .max(Comparator.comparing(
                                (TransactionRecord tr) -> tr.getKey().getTimeOfRecord(),
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .orElse(records.get(0)))
                .sorted(Comparator.comparing(
                        (TransactionRecord tr) -> tr.getKey().getTimeOfRecord(),
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .collect(Collectors.toList());

        int start = zeroBasedPage * pageSize;
        int end = Math.min(start + pageSize, preferredRecords.size());

        List<TransactionRecord> pageContent = (start >= preferredRecords.size())
                ? Collections.emptyList()
                : preferredRecords.subList(start, end);

        List<TransactionRecordResponse> responseList = pageContent.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());

        Page<TransactionRecordResponse> responsePage = new PageImpl<>(
                responseList,
                PageRequest.of(zeroBasedPage, pageSize),
                preferredRecords.size()
        );

        logger.info("Fetched {} transactions on page {}", responseList.size(), pageNumber);
        return responsePage;
    }





    // Helper mapping method
    private TransactionRecordResponse mapToTransactionResponse(TransactionRecord record) {
        TransactionRecordResponse response = new TransactionRecordResponse();

        response.setOrderId(record.getKey().getOrderId());
        response.setTransactionType(record.getKey().getTransactionType());
        response.setOrderDescription(record.getKey().getOrderDescription());
        response.setTimeOfRecord(record.getKey().getTimeOfRecord());

        // Fields
        response.setTransactionId(record.getTransactionId());
        response.setCardNumber(record.getCardNumber());
        response.setCardExpiryMonth(record.getCardExpiryMonth());
        response.setCardExpiryYear(record.getCardExpiryYear());
        response.setAmount(record.getAmount());
        response.setCurrency(record.getCurrency());

        response.setAcquirer(record.getAcquirer());
        response.setAcquirerCode(record.getAcquirerCode());
        response.setGatewayCode(record.getCode());
        response.setGatewayRecommendation(record.getGatewayRecommendation());

        response.setCardholderName(record.getCardholderName());
        response.setIssuer(record.getIssuer());
        response.setBrand(record.getBrand());
        response.setAccountType(record.getAccountType());
        response.setFundingMethod(record.getFundingMethod());

        response.setAuthenticationStatus(record.getAuthenticationStatus());
        response.setThreeDsVersion(record.getThreeDsVersion());

        response.setFirstName(record.getFirstName());
        response.setLastName(record.getLastName());
        response.setEmail(record.getEmail());

        response.setOrderStatus(record.getOrderStatus());
        response.setIpAddress(record.getIpAddress());
        response.setMerchant(record.getMerchant());
        response.setMerchantName(record.getMerchantName());
        response.setResponseMessage(record.getResponseMessage());


        return response;
    }




    @Transactional
    public void fixMissingMerchantNames() {
        System.out.println("Starting fixMissingMerchantNames...");

        // 1. Get all records with merchantId present but merchantName missing
        List<TransactionRecord> recordsMissingMerchantName = transactionRepository.findRecordsWithMerchantIdButMissingName();

        if (recordsMissingMerchantName.isEmpty()) {
            System.out.println("No records with missing merchant names to fix.");
            return;
        }

        // 2. Get unique merchantIds
        Set<String> merchantIds = recordsMissingMerchantName.stream()
                .map(TransactionRecord::getMerchant)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 3. Get sample records that have merchantName filled for those merchantIds
        List<TransactionRecord> referenceRecords = transactionRepository.findRecordsWithMerchantNameByMerchantIds(merchantIds);

        // 4. Map merchantId -> merchantName from reference records
        Map<String, String> merchantIdToName = referenceRecords.stream()
                .filter(r -> r.getMerchant() != null && r.getMerchantName() != null)
                .collect(Collectors.toMap(
                        TransactionRecord::getMerchant,
                        TransactionRecord::getMerchantName,
                        (existing, replacement) -> existing  // keep existing in case of duplicates
                ));

        // 5. Update missing merchant names using the map
        List<TransactionRecord> toUpdate = new ArrayList<>();
        for (TransactionRecord record : recordsMissingMerchantName) {
            String merchantId = record.getMerchant();
            if (merchantId != null && merchantIdToName.containsKey(merchantId)) {
                record.setMerchantName(merchantIdToName.get(merchantId));
                toUpdate.add(record);
            }
        }

        // 6. Save updated records
        if (!toUpdate.isEmpty()) {
            transactionRepository.saveAll(toUpdate);
            System.out.println("Updated merchant name for " + toUpdate.size() + " records.");
        } else {
            System.out.println("No matching merchant names found to update.");
        }
    }




    @Override
    public List<TransactionRecordResponse> getTransactionsByOrderId(String orderId) {
        List<TransactionRecord> records = transactionRepository.findByKey_OrderIdOrderByKey_TimeOfRecordAsc(orderId);
        return records.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }














}
