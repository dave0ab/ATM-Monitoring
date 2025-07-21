package com.zemenbank.atmMonitoring.services;

import com.zemenbank.atmMonitoring.enums.Category;
import com.zemenbank.atmMonitoring.model.atmMonitor.Tips;

import java.util.List;

public interface TipsService {
    Tips createTips(Tips tips);
    Tips updateTips(Long id, Tips tips);
    void deleteTips(Long id);
    Tips getTipsById(Long id);
    List<Tips> getAllTips();
    List<Tips> getTipsByCategory(Category category); // New method
}
