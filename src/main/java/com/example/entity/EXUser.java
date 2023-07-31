package com.example.entity;

import java.util.Date; 

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Document(collection = "exuser")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class EXUser {
	
	@Id
	private String id;
	
	private String websitename;
	 
	private String email;
	 
	private String firstName;
   
	private String lastName;
		 
	private String userid;
 
	private String password;
	 
	private  Integer usertype;

	private  Boolean accountLock;
	
	private Boolean betLock;
	 
	Boolean isActive;
		 
	Date createdOn;
	
	 
	Date updatedOn;
	 
	private  String parentId;
	
	private  String parentName;
	
	private  String parentUserId;
	
	 
	private String adminId;
	
	 
	private String subadminId;
	
	 
	private String miniadminId;
	
	 
	private String supersuperId;
	
	 
	private String supermasterId;
	
	 
	private String masterId;
	
	private String adminName;
	
	private String subadminName;
	
	private String miniadminName;
	
	private String supersuperName;
	
	private String supermasterName;
	
	private String masterName;
	
	private String adminUserId;
	
	private String subadminUserId;
	
	private String miniadminUserId;
	
	private String supersuperUserId;
	
	private String supermasterUserId;
	
	private String masterUserId;
	
	private Double myBalance;
	
	private Double fixLimit;
	
	private Double exposureLimit;
	
	private  UserStake stake;
	
	private OneClickBetting betting;
	
	private Double myallPl;
	
	 
	private String websiteId;
	
	private String websiteName;
	
	private String subChild;
		
	private String mobileNumber;
	
	private Boolean ispasswordChanged;
	
	Partnership partnership;
	
	private String accType;
	
	private Double childLiab;
	
	private Double pts;
	
	private Double clientPl;
	
	private Double clientPlTotal;
	
	private Double mysportPl;
	
	private Double mycasinoPl;

	private String betLockReason;
	
	private String betLockMatch;
	
	Integer rateDifference;
	
	private String timeZone;
	
	private Boolean isOneClickBet;
	
	private Double defaultStake;
	
	private Boolean highlightOdds;
	
	private Boolean acceptAnyFancyOdds;
	
	private Boolean acceptAnySportsBookOdds;
	
	private Boolean acceptAnyBinaryOdds;

	
}
