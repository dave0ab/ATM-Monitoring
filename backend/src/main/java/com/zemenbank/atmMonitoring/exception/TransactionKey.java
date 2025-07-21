package com.zemenbank.atmMonitoring.exception;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Embeddable
@Data
@AllArgsConstructor
public class TransactionKey implements Serializable {


    private String orderId;
    private String orderDescription;
    private String transactionType;
    private OffsetDateTime timeOfRecord;

    public TransactionKey() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionKey that)) return false;

        return orderId.equals(that.orderId) &&
                orderDescription.equals(that.orderDescription) &&
                transactionType.equals(that.transactionType) &&
                timeOfRecord.equals(that.timeOfRecord);
    }

    @Override
    public int hashCode() {
        int result = orderId.hashCode();
        result = 31 * result + orderDescription.hashCode();
        result = 31 * result + transactionType.hashCode();
        result = 31 * result + timeOfRecord.hashCode();
        return result;
    }
}
