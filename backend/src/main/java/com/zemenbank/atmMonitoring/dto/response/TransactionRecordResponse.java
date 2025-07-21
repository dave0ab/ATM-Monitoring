package com.zemenbank.atmMonitoring.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class TransactionRecordResponse {

    // Composite key fields
    private String transactionId;
    private String transactionType;
    private String orderDescription;
    private OffsetDateTime timeOfRecord;

    // Transaction details
    private String orderId;
    private String cardNumber;
    private String cardExpiryMonth;
    private String cardExpiryYear;
    private BigDecimal amount;
    private String currency;

    // Acquirer & response
    private String acquirer;
    private String acquirerCode;
    private String gatewayCode;
    private String gatewayRecommendation;

    // Card details
    private String cardholderName;
    private String issuer;
    private String brand;
    private String accountType;
    private String fundingMethod;

    // Authentication
    private String authenticationStatus;
    private String threeDsVersion;

    // Customer
    private String firstName;
    private String lastName;
    private String email;

    // Metadata
    private String orderStatus;
    private String ipAddress;
    private String merchant;

    private String merchantName;

    private  String responseMessage;
}
