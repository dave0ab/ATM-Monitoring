package com.zemenbank.atmMonitoring.model.bankworld;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "BWS_ATM_NETWORK", schema = "BANKWORLD")
public class ATM_Network {

    @Id
    @Column(name = "ATM_ID", nullable = false, length = 10)
    private String atmId;

    @Column(name = "REQUIRED_OP_STATE", nullable = false)
    private Integer requiredOpState;

    @Column(name = "STOP_POLL", nullable = false)
    private Integer stopPoll;

    @Column(name = "ATM_GROUP_ID", nullable = false)
    private Long atmGroupId;

    @Column(name = "ICON_X", nullable = false)
    private Integer iconX;

    @Column(name = "ICON_Y", nullable = false)
    private Integer iconY;

    @Column(name = "IS_TEMPLATE", nullable = false)
    private Integer isTemplate;

    @Column(name = "HAS_NOTES", nullable = false)
    private Integer hasNotes;

    @Column(name = "EMV_ENABLED", length = 1)
    private String emvEnabled;

    @Column(name = "BWFEEATMGROUPID")
    private Long bwFeeAtmGroupId;

    @Column(name = "MAC_ENABLED", length = 1)
    private String macEnabled;

    @Column(name = "UPTIMECOLLECTEDDATE")
    private Date uptimeCollectedDate;

    @Column(name = "TXNNOTIFYPERIOD")
    private Integer txnNotifyPeriod;

    @Column(name = "ACTUAL_SYSTEM_ID", length = 8)
    private String actualSystemId;

    @Column(name = "TERMINAL", length = 8)
    private String terminal;

    @Column(name = "LAST_UPDATED")
    private Timestamp lastUpdated;

    @Column(name = "LASTDS_EXT")
    private byte[] lastDsExt;

    @Column(name = "LASTFS_EXT")
    private byte[] lastFsExt;

    @Column(name = "IPM_STORE")
    private Long ipmStore;

    @Column(name = "CASHTXNNOTIFYPERIOD")
    private Integer cashTxnNotifyPeriod;

    @Column(name = "GEOLOCATION", length = 32)
    private String geoLocation;

    @Column(name = "COUNTRY_SUBDIVISION", length = 3)
    private String countrySubdivision;

    @Column(name = "ACQUIRER", nullable = false, length = 6)
    private String acquirer;

    @Column(name = "ID", nullable = false, length = 4)
    private String id;

    @Column(name = "ENC_ZONE", nullable = false, length = 10)
    private String encZone;

    @Column(name = "NEW_KEY_TIME", nullable = false, length = 4)
    private String newKeyTime;

    @Column(name = "BRANCH", nullable = false, length = 10)
    private String branch;

    @Column(name = "POLL_SEL", nullable = false, length = 8)
    private String pollSel;

    @Column(name = "TDATE", length = 8)
    private String tDate;

    @Column(name = "TTIME", length = 6)
    private String tTime;

    @Column(name = "ACTUAL_OP_STATE")
    private Integer actualOpState;

    @Column(name = "DEVICE")
    private Integer device;

    @Column(name = "FAILURE")
    private Integer failure;

    @Column(name = "DTYPE", nullable = false, length = 1)
    private String dtype;

    @Column(name = "MODEL_DESC", length = 20)
    private String modelDesc;

    @Column(name = "DEVICE2")
    private Integer device2;

    @Column(name = "FAILURE2")
    private Integer failure2;

    @Column(name = "LASTFS")
    private byte[] lastFs;

    @Column(name = "LASTDS")
    private byte[] lastDs;

    @Column(name = "PORT", nullable = false)
    private Integer port;

    @Column(name = "BDAY", nullable = false)
    private Integer bday;

    @Column(name = "MODEL")
    private Integer model;

    @Column(name = "APP")
    private Integer app;

    @Column(name = "DTIM_MODEL_RCVD", length = 12)
    private String dtimModelRcvd;

    @Column(name = "APP_DESC", length = 40)
    private String appDesc;

    @Column(name = "CURRENCY", nullable = false)
    private Integer currency;

    @Column(name = "COMMS_METHOD", nullable = false, length = 1)
    private String commsMethod;

    @Column(name = "PROTOCOL", length = 31)
    private String protocol;

    @Column(name = "BAUD", length = 6)
    private String baud;

    @Column(name = "DEVICE_NAME", length = 200)
    private String deviceName;

    @Column(name = "POLL_INT")
    private Integer pollInt;

    @Column(name = "ACK_TIME")
    private Integer ackTime;

    @Column(name = "ENC", length = 5)
    private String enc;

    @Column(name = "DCD", length = 1)
    private String dcd;

    @Column(name = "OVERHEAD1")
    private Integer overhead1;

    @Column(name = "OVERHEAD2")
    private Integer overhead2;

    @Column(name = "OVERHEAD3")
    private Integer overhead3;

    @Column(name = "ADDR", length = 64)
    private String addr;

    @Column(name = "SOCKET", length = 10)
    private String socket;

    @Column(name = "TCPIP_HDR", length = 1)
    private String tcpipHdr;

    @Column(name = "COM")
    private Integer com;

    @Column(name = "SRC", length = 1)
    private String src;

    @Column(name = "RECONNECT")
    private Integer reconnect;

    @Column(name = "LANG3", length = 2)
    private String lang3;

    @Column(name = "LANG4", length = 2)
    private String lang4;

    @Column(name = "LANG2", length = 2)
    private String lang2;

    @Column(name = "COMMS_STATE")
    private Integer commsState;

    @Column(name = "DWNL_STATE")
    private Integer dwnlState;

    @Column(name = "EXIT_STATE")
    private Integer exitState;

    @Column(name = "CHIP_APP", length = 8)
    private String chipApp;

    @Column(name = "CHIP_IFD", length = 8)
    private String chipIfd;

    @Column(name = "MSG_VER", length = 1)
    private String msgVer;

    @Column(name = "ATM_NAME", length = 30)
    private String atmName;

    @Column(name = "STATUS", nullable = false)
    private Integer status;

    @Column(name = "REGION", nullable = false)
    private Integer region;

    @Column(name = "AT_BRANCH", nullable = false, length = 2)
    private String atBranch;

    @Column(name = "ADDRESS1", length = 20)
    private String address1;

    @Column(name = "ADDRESS2", length = 20)
    private String address2;

    @Column(name = "ADDRESS3", length = 20)
    private String address3;

    @Column(name = "ADDRESS4", length = 20)
    private String address4;

    @Column(name = "CITYSTATE", length = 15)
    private String cityState;

    @Column(name = "POSTCODE", length = 10)
    private String postcode;

    @Column(name = "USE_SET", length = 1)
    private String useSet;

    @Column(name = "SERVICE_SET")
    private Integer serviceSet;
}
