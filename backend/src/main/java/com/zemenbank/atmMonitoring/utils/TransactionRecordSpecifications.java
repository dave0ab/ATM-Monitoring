package com.zemenbank.atmMonitoring.utils;


import com.zemenbank.atmMonitoring.model.atmMonitor.TransactionRecord;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionRecordSpecifications {

    public static Specification<TransactionRecord> withDynamicFilters(
            String orderStatus,
            String brand,
            String transactionType,
            String currency,
            String search,
            LocalDate  startDate,  // 👈 new
            LocalDate endDate
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (orderStatus != null && !orderStatus.isBlank()) {
                predicates.add(cb.equal(root.get("orderStatus"), orderStatus));
            }

            if (brand != null && !brand.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("brand")), "%" + brand.toLowerCase() + "%"));
            }

            if (transactionType != null && !transactionType.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("key").get("transactionType")), "%" + transactionType.toLowerCase() + "%"));
            }

            if (currency != null && !currency.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("currency")), "%" + currency.toLowerCase() + "%"));
            }

            if (search != null && !search.isBlank()) {
                String lower = "%" + search.trim().toLowerCase() + "%";

                Predicate orderId = cb.like(cb.lower(cb.trim(root.get("key").get("orderId"))), lower);
                Predicate cardNumber = cb.like(cb.lower(cb.trim(root.get("cardNumber"))), lower);
                Predicate merchantName = cb.like(cb.lower(root.get("merchantName")), lower);
                Predicate cardholderName = cb.like(cb.lower(root.get("cardholderName")), lower);
                Predicate issuer = cb.like(cb.lower(root.get("issuer")), lower);
                Predicate firstName = cb.like(cb.lower(root.get("firstName")), lower);
                Predicate email = cb.like(cb.lower(root.get("email")), lower);
                Predicate orderDescription = cb.like(cb.lower(root.get("key").get("orderDescription")), lower);
                Predicate merchantId = cb.like(cb.lower(cb.trim(root.get("merchant"))), lower);
                Predicate cardName = cb.like(cb.lower(root.get("cardholderName")), lower);

                predicates.add(cb.or(
                        orderId, cardNumber, merchantName,
                        cardholderName, issuer, firstName,
                        email, orderDescription,merchantId,cardName
                ));
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("key").get("timeOfRecord"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("key").get("timeOfRecord"), endDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
