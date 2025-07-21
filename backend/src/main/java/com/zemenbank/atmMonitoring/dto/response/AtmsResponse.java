package com.zemenbank.atmMonitoring.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.zemenbank.atmMonitoring.model.atmMonitor.AtmLocation;
import com.zemenbank.atmMonitoring.model.bankworld.ATMS;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AtmsResponse {



    private String atmId;
    private String atmName;
    private String modelDesc;
    private String address1;
    private Integer region;
    private String regionName;
    private Integer requiredOpState;
    private Integer actualOpState;
    private String actualOpStateDesc;

    private Integer commsState;
    private String commStateDesc;
    private Integer status;
    private String stateName;
    private Integer device;
    private String deviceName;
    private Integer failure;
    private String failureName;
    private Integer device2;
    private String device2Name;
    private Integer failure2;
    private String failure2Name;
     private Long successTxnLastUpdate;
    private byte[] lastFs;
    private byte[] lastDs;
    private String atmIdNum;
    private Integer atmGroupId;
    private String cashTxnLastUpdate;
    private String branchName;
    private String lastUpdated;
    private Long lastUpdatedEpoch;

    private String pingStatus;

    private String atmIP;

    private String mapLink;

    private String cashAdd;

    private String cashLeft;

    private String remark;

    public Long getAtmIdd() {
        return Long.valueOf(atmId);
    }


    public static AtmsResponse mapAtmsToResponse(ATMS atms, String pingStatus, String atmIP ,String mapLink , String cashAdd ,String cashLeft , String remark) {
        return new AtmsResponse(
                atms.getAtmId(),
                atms.getAtmName(),
                atms.getModelDesc(),
                atms.getAddress1(),
                atms.getRegion(),
                atms.getRegionName(),
                atms.getRequiredOpState(),
                atms.getActualOpState(),
                atms.getActualOpStateDesc(),
                atms.getCommsState(),
                atms.getCommStateDesc(),
                atms.getStatus(),
                atms.getStateName(),
                atms.getDevice(),
                atms.getDeviceName(),
                atms.getFailure(),
                atms.getFailureName(),
                atms.getDevice2(),
                atms.getDevice2Name(),
                atms.getFailure2(),
                atms.getFailure2Name(),
                parseDateString(atms.getSuccessTxnLastUpdate()),
                atms.getLastFs(),
                atms.getLastDs(),
                atms.getAtmIdNum(),
                atms.getAtmGroupId(),
                atms.getCashTxnLastUpdate(),
                atms.getBranchName(),
                atms.getLastUpdated(),
                atms.getLastUpdatedEpoch(),
                pingStatus,
                atmIP,
                mapLink,
                cashAdd,
                cashLeft,
                remark

        );
    }


    private static Long parseDateString(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null; // Handle null or empty date
        }

        try {
            // Parse the input date string to LocalDateTime
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyMMdd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.parse(dateString, inputFormatter);

            // Convert to Unix timestamp (milliseconds since epoch in UTC)
            return dateTime.atZone(ZoneId.of("America/New_York")).toInstant().toEpochMilli();
        } catch (DateTimeParseException e) {
            System.err.println("Failed to parse date: " + dateString + " - " + e.getMessage());
            return null;
        }
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AtmsResponse that = (AtmsResponse) o;
        return Objects.equals(atmId, that.atmId) &&
                Objects.equals(atmName, that.atmName) &&
                Objects.equals(modelDesc, that.modelDesc) &&
                Objects.equals(address1, that.address1) &&
                Objects.equals(region, that.region) &&
                Objects.equals(regionName, that.regionName) &&
                Objects.equals(requiredOpState, that.requiredOpState) &&
                Objects.equals(actualOpState, that.actualOpState) &&
                Objects.equals(actualOpStateDesc, that.actualOpStateDesc) &&
                Objects.equals(commsState, that.commsState) &&
                Objects.equals(commStateDesc, that.commStateDesc) &&
                Objects.equals(status, that.status) &&
                Objects.equals(stateName, that.stateName) &&
                Objects.equals(device, that.device) &&
                Objects.equals(deviceName, that.deviceName) &&
                Objects.equals(failure, that.failure) &&
                Objects.equals(failureName, that.failureName) &&
                Objects.equals(device2, that.device2) &&
                Objects.equals(device2Name, that.device2Name) &&
                Objects.equals(failure2, that.failure2) &&
                Objects.equals(failure2Name, that.failure2Name) &&
                Objects.equals(successTxnLastUpdate, that.successTxnLastUpdate) &&
                Objects.equals(lastFs, that.lastFs) &&
                Objects.equals(lastDs, that.lastDs) &&
                Objects.equals(atmIdNum, that.atmIdNum) &&
                Objects.equals(atmGroupId, that.atmGroupId) &&
                Objects.equals(cashTxnLastUpdate, that.cashTxnLastUpdate) &&
                Objects.equals(branchName, that.branchName) &&
                Objects.equals(lastUpdated, that.lastUpdated) &&
                Objects.equals(lastUpdatedEpoch, that.lastUpdatedEpoch) &&
                Objects.equals(pingStatus, that.pingStatus) &&
                Objects.equals(atmIP, that.atmIP) &&
                Objects.equals(mapLink, that.mapLink) &&
                Objects.equals(cashAdd, that.cashAdd) &&
                Objects.equals(cashLeft, that.cashLeft);
    }

    @Override
    public int hashCode() {
        return Objects.hash(atmId, atmName, modelDesc, address1, region, regionName, requiredOpState, actualOpState, actualOpStateDesc, commsState, commStateDesc, status, stateName, device, deviceName, failure, failureName, device2, device2Name, failure2, failure2Name, successTxnLastUpdate, lastFs, lastDs, atmIdNum, atmGroupId, cashTxnLastUpdate, branchName, lastUpdated, lastUpdatedEpoch, pingStatus, atmIP, mapLink, cashAdd, cashLeft);
    }




}
