package com.zemenbank.atmMonitoring.dto.response;

import lombok.Data;

@Data
public class TransactionApiResponse {
    private String merchant;
    private String merchantName;
    private String timeOfRecord; // ISO 8601 timestamp
    private String orderId;
    private String transactionId;
    private String cardNumber;
    private String cardExpiryMonth;
    private String cardExpiryYear;
    private String amount;
    private String currency;
    private String type;
    private String acquirer;
    private String acquirerCode;
    private String code;
    private String cardholderName;
    private String issuer;
    private String brand;
    private String accountType;
    private String fundingMethod;
    private String authenticationStatus;
    private String orderStatus;
    private String threeDsVersion;
    private String ipAddress;
    private String firstName;
    private String lastName;
    private String email;
    private String orderDescription;
    private String gatewayRecommendation;
}