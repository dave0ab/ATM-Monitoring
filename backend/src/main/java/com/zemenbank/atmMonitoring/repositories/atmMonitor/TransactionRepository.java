package com.zemenbank.atmMonitoring.repositories.atmMonitor;

import com.zemenbank.atmMonitoring.exception.TransactionKey;
import com.zemenbank.atmMonitoring.model.atmMonitor.TransactionRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TransactionRepository extends JpaRepository<TransactionRecord, TransactionKey>,
        JpaSpecificationExecutor<TransactionRecord> {

    TransactionRecord findTopByOrderByKey_TimeOfRecordDesc();

    boolean existsByKey(TransactionKey key);


    @Query("SELECT t FROM TransactionRecord t WHERE t.key.transactionType = :type AND (t.merchantName IS NULL OR t.merchantName = '')")
    List<TransactionRecord> findByTransactionTypeAndMerchantNameIsNullOrBlank(@Param("type") String type);


    @Query("""
    SELECT t FROM TransactionRecord t
    WHERE (:orderStatus IS NULL OR t.orderStatus = :orderStatus)
      AND (:brand IS NULL OR LOWER(t.brand) LIKE LOWER(CONCAT('%', :brand, '%')))
      AND (:transactionType IS NULL OR LOWER(t.key.transactionType) LIKE LOWER(CONCAT('%', :transactionType, '%')))
      AND (:currency IS NULL OR LOWER(t.currency) LIKE LOWER(CONCAT('%', :currency, '%')))
    ORDER BY t.key.timeOfRecord DESC
""")
    List<TransactionRecord> findFilteredTransactionsNoPaging(
            @Param("orderStatus") String orderStatus,
            @Param("brand") String brand,
            @Param("transactionType") String transactionType,
            @Param("currency") String currency
    );



    @Query("SELECT t FROM TransactionRecord t WHERE t.key.transactionType = 'AUTHENTICATION' " +
            "AND t.key.orderId = :orderId " +
            "AND t.amount = :amount " +
            "AND t.cardNumber = :cardNumber " +
            "AND t.merchantName IS NOT NULL AND t.merchantName <> ''")
    Optional<TransactionRecord> findAuthenticationRecordForPayment(@Param("orderId") String orderId,
                                                                   @Param("amount") BigDecimal amount,
                                                                   @Param("cardNumber") String cardNumber);




    @Query("SELECT t FROM TransactionRecord t WHERE t.merchant IS NOT NULL AND (t.merchantName IS NULL OR t.merchantName = '')")
    List<TransactionRecord> findRecordsWithMerchantIdButMissingName();

    @Query("SELECT t FROM TransactionRecord t WHERE t.merchant IN :merchantIds AND t.merchantName IS NOT NULL AND t.merchantName <> ''")
    List<TransactionRecord> findRecordsWithMerchantNameByMerchantIds(@Param("merchantIds") Set<String> merchantIds);


    List<TransactionRecord> findByKey_OrderIdOrderByKey_TimeOfRecordAsc(String orderId);


}
