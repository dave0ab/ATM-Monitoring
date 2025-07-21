package com.zemenbank.atmMonitoring.model.bankworld;


import com.zemenbank.atmMonitoring.services.ATMSListener;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "MON_BWATMS", schema = "BANKWORLD")
@EntityListeners(ATMSListener.class)
public class ATMS {

	@Id
	@Column(name = "ATM_ID", length = 10, nullable = false)
	private String atmId;

	@Column(name = "ATM_NAME", length = 30)
	private String atmName;

	@Column(name = "MODEL_DESC", length = 20)
	private String modelDesc;

	@Column(name = "ADDRESS1", length = 20)
	private String address1;

	@Column(name = "REGION", nullable = false)
	private Integer region;

	@Column(name = "REGIONNAME", length = 30)
	private String regionName;

	@Column(name = "REQUIRED_OP_STATE", nullable = false)
	private Integer requiredOpState;

	@Column(name = "ACTUAL_OP_STATE")
	private Integer actualOpState;

	@Column(name = "ACTUAL_OP_STATE_DESC", length = 30)
	private String actualOpStateDesc;

	@Column(name = "COMMS_STATE")
	private Integer commsState;

	@Column(name = "COMM_STATE_DESC", length = 30)
	private String commStateDesc;

	@Column(name = "STATUS", nullable = false)
	private Integer status;

	@Column(name = "STATE_NAME", length = 30)
	private String stateName;

	@Column(name = "DEVICE")
	private Integer device;

	@Column(name = "DEVICE_NAME", length = 30)
	private String deviceName;

	@Column(name = "FAILURE")
	private Integer failure;

	@Column(name = "FAILURE_NAME", length = 30)
	private String failureName;

	@Column(name = "DEVICE2")
	private Integer device2;

	@Column(name = "DEVICE2_NAME", length = 30)
	private String device2Name;

	@Column(name = "FAILURE2")
	private Integer failure2;

	@Column(name = "FAILURE2_NAME", length = 30)
	private String failure2Name;

	@Column(name = "SUCCESSTXNLASTUPDATE", length = 15)
	private String successTxnLastUpdate;

	@Column(name = "LASTFS")
	private byte[] lastFs;

	@Column(name = "LASTDS")

	private byte[] lastDs;

	@Column(name = "ATM_ID_NUM", length = 16)
	private String atmIdNum;

	@Column(name = "ATM_GROUP_ID", nullable = false)
	private Integer atmGroupId;

	@Column(name = "CASHTXNLASTUPDATE", length = 15)
	private String cashTxnLastUpdate;

	@Column(name = "BRANCH_NAME", length = 15)
	private String branchName;

	@Column(name = "LASTUPDATED", length = 15)
	private String lastUpdated;

	@Column(name = "LASTUPDATED_EPOCH")
	private Long lastUpdatedEpoch;



}
