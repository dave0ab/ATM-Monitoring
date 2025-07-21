package com.zemenbank.atmMonitoring.repositories.atmMonitor;


import com.zemenbank.atmMonitoring.model.atmMonitor.AtmLocation;
import com.zemenbank.atmMonitoring.model.atmMonitor.PingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AtmLocationRepository extends JpaRepository<AtmLocation, Long> {

    @Query("SELECT a FROM AtmLocation a WHERE a.TID = :tId")
    List<AtmLocation> findByCustomTId(String tId);

    @Query("SELECT a FROM AtmLocation a WHERE a.TID = :tId")
    AtmLocation findByTId(String tId);
    void deleteAllByTIDIn(List<String> tIds);
}