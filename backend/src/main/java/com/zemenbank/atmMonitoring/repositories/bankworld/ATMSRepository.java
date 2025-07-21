package com.zemenbank.atmMonitoring.repositories.bankworld;

import com.zemenbank.atmMonitoring.model.atmMonitor.AtmMonitorStatus;
import com.zemenbank.atmMonitoring.model.bankworld.ATMS;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ATMSRepository extends JpaRepository<ATMS, Long> {


    @Query("SELECT a FROM ATMS a WHERE " +
            "(:search IS NULL OR " +
            "LOWER(CAST(a.atmId AS string)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.atmName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.modelDesc) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.deviceName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(CAST(a.region AS string)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.regionName) LIKE LOWER(CONCAT('%', :search, '%'))) ")
    Page<ATMS> findAllAtms(
            @Param("search") String search,

            Pageable pageable
    );


    @Query("SELECT a FROM ATMS a WHERE " +
            "(:deviceName IS NULL OR LOWER(a.deviceName) = LOWER(:deviceName)) AND " +
            "(:search IS NULL OR " +
            "LOWER(CAST(a.atmId AS string)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.atmName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.modelDesc) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(CAST(a.region AS string)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.regionName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:status IS NULL OR a.status = :status)")
    Page<ATMS> findAllByDeviceName(
            @Param("deviceName") String deviceName,
            @Param("search") String search,
            @Param("status") String status,
            Pageable pageable
    );

    @Query("SELECT a FROM ATMS a WHERE " +
            "(:problem IS NULL OR LOWER(a.deviceName) = LOWER(:problem))")
    Page<ATMS> findAllByDeviceName(
            @Param("problem") String problem,
            Pageable pageable
    );

    @Query("""
    SELECT a FROM ATMS a 
    WHERE (:status IS NULL OR LOWER(a.actualOpStateDesc) = LOWER(:status) OR 
           LOWER(a.commStateDesc) = LOWER(:status))
""")
    Page<ATMS> findAllByStatus(
            @Param("status") String status,
            Pageable pageable
    );


    List<ATMS> findByActualOpStateDescIn(List<String> actualOpState);

    Optional<ATMS> findByAtmId(String atmId);


    @Query("SELECT a FROM ATMS a WHERE a.atmId IN :tids")
    Page<ATMS> findAllByTIDs(@Param("tids") List<String> tids, Pageable pageable);

    List<ATMS> findByDeviceNameNotNull();


    long countByAtmIdIn(List<String> allowedTIDs);









    @Query("SELECT s FROM ATMS s WHERE s.commsState = :status")
    List<ATMS> findAllStatus(String status);

    @Query("SELECT s FROM ATMS s WHERE s.commsState != :status")
    List<ATMS> findAllStatusNotEqual(String status);







    @Query("SELECT s FROM ATMS s WHERE s.atmId IN :terminalIds AND s.commsState = :status")
    List<ATMS> findAllByTerminalIdInAndStatus(@Param("status") String status, @Param("terminalIds") List<String> terminalIds);


    @Query("SELECT s FROM ATMS s WHERE s.atmId IN :terminalIds AND s.commsState != :status")
    List<ATMS> findAllByTerminalIdInAndStatusNotEqual(@Param("status") String status, @Param("terminalIds") List<String> terminalIds);


}
