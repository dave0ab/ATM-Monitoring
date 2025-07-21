package com.zemenbank.atmMonitoring.repositories.bankworld;

import com.zemenbank.atmMonitoring.model.bankworld.ATM_Network;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ATM_NetworkRepository extends JpaRepository<ATM_Network, Long> {



    ATM_Network findByAtmId(String atmId);


}
