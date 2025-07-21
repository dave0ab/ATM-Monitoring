package com.zemenbank.atmMonitoring.repositories.bankworld;

import com.zemenbank.atmMonitoring.model.bankworld.GatewayInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GatewayInfoRepository extends JpaRepository<GatewayInfo, Long> {



    Optional<GatewayInfo> findByNetwork(String network);



}
