package com.zemenbank.atmMonitoring.services;


import com.zemenbank.atmMonitoring.model.atmMonitor.AtmLocation;

public interface SettingService {
    AtmLocation addOrUpdateLocation(String TID, String LO, String LA);

    AtmLocation getLocationByTID(String TID);
}
