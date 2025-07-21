package com.zemenbank.atmMonitoring.services.impl.cache;

import com.zemenbank.atmMonitoring.model.bankworld.ATMS;
import com.zemenbank.atmMonitoring.repositories.bankworld.ATMSRepository;
import com.zemenbank.atmMonitoring.services.ATMCacheService;
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
public class ATMCacheServiceImpl implements ATMCacheService {

    public static final Map<Long, ATMS> atmCache = new ConcurrentHashMap<>();
    private final ATMSRepository atmsRepository;

    // Constructor injection for the repository
    public ATMCacheServiceImpl(ATMSRepository atmsRepository) {
        this.atmsRepository = atmsRepository;
        preloadCache();  // Initial cache load
    }

    @Override
    public Optional<ATMS> findById(Long atmId) {
        return Optional.ofNullable(atmCache.get(atmId));
    }

    @Override
    public Page<ATMS> getAllAtms(Pageable pageable) {
        List<ATMS> atmsList = new ArrayList<>(atmCache.values());

        if (pageable.isUnpaged()) {
            return new PageImpl<>(atmsList);
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), atmsList.size());

        List<ATMS> paginatedAtms = atmsList.subList(start, end);
        return new PageImpl<>(paginatedAtms, pageable, atmsList.size());
    }



    @Override
    public Page<ATMS> getAtmsByStatus(String status, Pageable pageable) {
        return toPage(
                atmCache.values().stream()
                        .filter(atm -> status.equalsIgnoreCase(atm.getActualOpStateDesc())
                                || status.equalsIgnoreCase(atm.getCommStateDesc()))
                        .toList(),
                pageable
        );
    }


    @Override
    public Page<ATMS> getAtmsByDeviceName(String deviceName, Pageable pageable) {
        return toPage(
                atmCache.values().stream()
                        .filter(atm -> atm.getDeviceName().contains(deviceName))
                        .toList(),
                pageable
        );
    }

    @Override
    public Page<ATMS> searchAtms(String search, Pageable pageable) {
        String lowerCaseSearch = search.toLowerCase();

        return toPage(
                atmCache.values().stream()
                .filter(atm ->
                (atm.getDeviceName() != null && atm.getDeviceName().toLowerCase().contains(lowerCaseSearch))
                || (atm.getAtmName() != null && atm.getAtmName().toLowerCase().contains(lowerCaseSearch))
                || (atm.getAtmId() != null && atm.getAtmId().toLowerCase().contains(lowerCaseSearch))
                || (atm.getSuccessTxnLastUpdate() != null && atm.getSuccessTxnLastUpdate().toLowerCase().contains(lowerCaseSearch))
            )
                        .toList(),
                pageable
        );
    }

    @Override
    public Page<ATMS> getAtmsByTIDs(List<String> tids, Pageable pageable) {
        return toPage(
                atmCache.values().stream()
                        .filter(atm -> tids.contains(atm.getAtmId()))
                        .toList(),
                pageable
        );
    }

    // Scheduled cache refresh every 5 seconds
    @Scheduled(fixedRate = 5000)
    public void refreshCache() {

        atmCache.clear();  // Clear the existing cache
        preloadCache();    // Reload from the repository
    }

    // Preload cache from the repository
    private void preloadCache() {
        List<ATMS> atmsList = atmsRepository.findAll();
        atmsList.forEach(atm -> atmCache.put(Long.valueOf(atm.getAtmId()), atm));

    }

    private Page<ATMS> toPage(List<ATMS> atms, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), atms.size());
        return new PageImpl<>(atms.subList(start, end), pageable, atms.size());
    }


}
