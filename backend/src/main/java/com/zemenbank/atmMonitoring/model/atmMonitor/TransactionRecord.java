package com.zemenbank.atmMonitoring.model.atmMonitor;

import com.zemenbank.atmMonitoring.exception.TransactionKey;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "transaction_report")
@Getter
@Setter
public class TransactionRecord {

    @EmbeddedId
    private TransactionKey key;

    private String merchant;

    private String transactionId;

    private String merchantName;
    private String cardNumber;
    private String cardExpiryMonth;
    private String cardExpiryYear;
    private BigDecimal amount;
    private String currency;
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
    private String gatewayRecommendation;

    private String responseMessage;


    @Transient
    public OffsetDateTime getTimeOfRecord() {
        return key != null ? key.getTimeOfRecord() : null;
    }


}
