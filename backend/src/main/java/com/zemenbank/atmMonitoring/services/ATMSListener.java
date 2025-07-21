package com.zemenbank.atmMonitoring.services;


import com.zemenbank.atmMonitoring.model.bankworld.ATMS;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;

import java.util.Objects;

public class ATMSListener {

    @PrePersist
    public void beforeInsert(ATMS atms) {
        System.out.println("Preparing to insert a new ATMS record: " + atms);
    }

    @PreUpdate
    public void beforeUpdate(ATMS atms) {
        System.out.println("Preparing to update an ATMS record with ID: " + atms.getAtmId());
        System.out.println("New state: " + atms);
    }

    @PreRemove
    public void beforeDelete(ATMS atms) {
        System.out.println("Preparing to delete an ATMS record: " + atms);
    }

    /**
     * Compare old and new values (if needed, fetch old values using a custom repository or Hibernate mechanisms).
     * This requires enabling entity tracking for comparison (out of scope for this example).
     */
    public void logChanges(ATMS oldAtms, ATMS newAtms) {
        if (!Objects.equals(oldAtms.getAtmName(), newAtms.getAtmName())) {
            System.out.println("ATM Name changed from " + oldAtms.getAtmName() + " to " + newAtms.getAtmName());
        }
        if (!Objects.equals(oldAtms.getRegion(), newAtms.getRegion())) {
            System.out.println("Region changed from " + oldAtms.getRegion() + " to " + newAtms.getRegion());
        }
        // Repeat for other fields as necessary...
    }
}
