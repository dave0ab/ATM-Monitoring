package com.zemenbank.atmMonitoring.services;

import com.zemenbank.atmMonitoring.model.bankworld.GatewayInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface GatewayCacheService {


    Optional<GatewayInfo> findByNetwork(String network);


    List<GatewayInfo> findAll();

    Page<GatewayInfo> findAll(Pageable pageable);


    Page<GatewayInfo> findAllByStatus(String status, Pageable pageable);


    Page<GatewayInfo> findAllByDeviceName(String deviceName, Pageable pageable);


    Page<GatewayInfo> findAllGatewayInfo(String search, Pageable pageable);


    Page<GatewayInfo> findAllByTIDs(List<String> tids, Pageable pageable);


    Integer getUptime(String network);


    Integer getDowntime(String network);



    GatewayInfo updateGatewayStatus(GatewayInfo gatewayInfo);
}
