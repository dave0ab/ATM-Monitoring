package com.zemenbank.atmMonitoring.services.impl.cache;

import com.zemenbank.atmMonitoring.model.bankworld.GatewayInfo;
import com.zemenbank.atmMonitoring.repositories.bankworld.GatewayInfoRepository;
import com.zemenbank.atmMonitoring.services.GatewayCacheService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GatewayCacheServiceImpl implements GatewayCacheService {

    private final Map<String, GatewayInfo> gatewayCache = new ConcurrentHashMap<>();
    private final GatewayInfoRepository gatewayInfoRepository;

    // Constructor injection for the repository
    public GatewayCacheServiceImpl(GatewayInfoRepository gatewayInfoRepository) {
        this.gatewayInfoRepository = gatewayInfoRepository;
        preloadCache();  // Initial cache load
    }

    @Scheduled(fixedRate = 5000) // Refresh every minute
    public void preloadCache() {
        List<GatewayInfo> gateways = gatewayInfoRepository.findAll();
        gatewayCache.clear();
        gateways.forEach(gateway -> gatewayCache.put(gateway.getNetwork(), gateway));
    }

    @Override
    public Optional<GatewayInfo> findByNetwork(String network) {
        return Optional.ofNullable(gatewayCache.get(network));
    }

    @Override
    public List<GatewayInfo> findAll() {
        return new ArrayList<>(gatewayCache.values());
    }

    @Override
    public Page<GatewayInfo> findAll(Pageable pageable) {
        List<GatewayInfo> gatewayList = new ArrayList<>(gatewayCache.values());
        return createPage(gatewayList, pageable);
    }

    @Override
    public Page<GatewayInfo> findAllByStatus(String status, Pageable pageable) {
        List<GatewayInfo> filtered = gatewayCache.values().stream()
                .filter(gateway -> String.valueOf(gateway.getActualStat()).equals(status))
                .toList();
        return createPage(filtered, pageable);
    }

    @Override
    public Page<GatewayInfo> findAllByDeviceName(String deviceName, Pageable pageable) {
        List<GatewayInfo> filtered = gatewayCache.values().stream()
                .filter(gateway -> gateway.getSystemId().equalsIgnoreCase(deviceName))
                .toList();
        return createPage(filtered, pageable);
    }

    @Override
    public Page<GatewayInfo> findAllGatewayInfo(String search, Pageable pageable) {
        List<GatewayInfo> filtered = gatewayCache.values().stream()
                .filter(gateway -> gateway.getNetwork().contains(search) ||
                        gateway.getSystemId().contains(search))
                .toList();
        return createPage(filtered, pageable);
    }

    @Override
    public Page<GatewayInfo> findAllByTIDs(List<String> tids, Pageable pageable) {
        List<GatewayInfo> filtered = gatewayCache.values().stream()
                .filter(gateway -> tids.contains(gateway.getNetwork()))
                .toList();
        return createPage(filtered, pageable);
    }

    @Override
    public Integer getUptime(String network) {
        return gatewayCache.getOrDefault(network, new GatewayInfo()).getActualUpTime().intValue();
    }

    @Override
    public Integer getDowntime(String network) {
        return gatewayCache.getOrDefault(network, new GatewayInfo()).getActualDownTime().intValue();
    }

    @Override
    public GatewayInfo updateGatewayStatus(GatewayInfo gatewayInfo) {
        gatewayCache.put(gatewayInfo.getNetwork(), gatewayInfo);
        gatewayInfoRepository.save(gatewayInfo);
        return gatewayInfo;
    }

    private Page<GatewayInfo> createPage(List<GatewayInfo> gateways, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), gateways.size());
        List<GatewayInfo> pagedList = gateways.subList(start, end);
        return new PageImpl<>(pagedList, pageable, gateways.size());
    }

    private int calculateUptime(GatewayInfo gateway) {
        long actualUpTime = gateway.getActualUpTime() != null ? gateway.getActualUpTime() : 0;
        long actualDownTime = gateway.getActualDownTime() != null ? gateway.getActualDownTime() : 0;
        long actualResetTime = gateway.getActualResetTime() != null ? gateway.getActualResetTime() : 0;

        long totalTime = actualUpTime + actualDownTime + actualResetTime;
        return totalTime > 0 ? (int) ((actualUpTime * 100) / totalTime) : 0;
    }
}
