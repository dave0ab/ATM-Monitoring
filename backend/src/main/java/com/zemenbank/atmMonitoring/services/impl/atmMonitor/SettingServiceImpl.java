package com.zemenbank.atmMonitoring.services.impl.atmMonitor;

import com.zemenbank.atmMonitoring.model.atmMonitor.AtmLocation;
import com.zemenbank.atmMonitoring.repositories.atmMonitor.AtmLocationRepository;
import com.zemenbank.atmMonitoring.services.BaseServices;
import com.zemenbank.atmMonitoring.services.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SettingServiceImpl extends BaseServices implements SettingService {

    private final AtmLocationRepository atmLocationRepository;

    @Autowired
    public SettingServiceImpl(AtmLocationRepository atmLocationRepository) {
        this.atmLocationRepository = atmLocationRepository;
    }

    @Override
    public AtmLocation addOrUpdateLocation(String TID, String LO, String LA) {
        logger.info("Entering addOrUpdateLocation method with TID: " + TID + ", LO: " + LO + ", LA: " + LA);

        // Check if the location exists by TID
        AtmLocation existingLocation = atmLocationRepository.findByTId(TID);

        if (existingLocation != null) {
            logger.info("Location found for TID: " + TID + ". Proceeding with update.");

            // Only update LO and LA if they are not empty
            if (LO != null && !LO.isEmpty()) {
                existingLocation.setLO(LO);
            }

            if (LA != null && !LA.isEmpty()) {
                existingLocation.setLA(LA);
            }

            // Save the updated location and return it
            AtmLocation updatedLocation = atmLocationRepository.save(existingLocation);
            logger.info("Location updated: " + updatedLocation);
            return updatedLocation;
        } else {
            logger.info("No location found for TID: " + TID + ". Creating a new location.");

            // If location doesn't exist, create a new one
            AtmLocation newLocation = new AtmLocation();
            newLocation.setTID(TID);

            // Only set LO and LA if they are not empty
            if (LO != null && !LO.isEmpty()) {
                newLocation.setLO(LO);
            }

            if (LA != null && !LA.isEmpty()) {
                newLocation.setLA(LA);
            }

            // Save and return the new location
            AtmLocation savedLocation = atmLocationRepository.save(newLocation);
            logger.info("New location created: " + savedLocation);
            return savedLocation;
        }
    }






    @Override
    public AtmLocation getLocationByTID(String TID) {
        //
        return atmLocationRepository.findByTId(TID);
    }
}
