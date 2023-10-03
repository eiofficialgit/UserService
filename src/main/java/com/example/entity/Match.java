package com.example.entity;

  
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Match {
	
	@Id
	private String _id;
	private String addType;
	private String bbbFancy;
	private String bmProvider;
	private String channelNo;
	private String competitionId;
	private String competitionName;
	private String createdAt;
	private String eventId;
	private String eventName;
	private String fancyAType;
	private String fancyProvider;
	public boolean isActive;
	public boolean isResult;
	private String mEventId;
	private String mType;
	private String marketId;
	private List<Object> marketIds;
	private String marketName;
	private List<Markets> markets;
	private List<Runners> matchRunners;
	private String oddsProvider;
	private String openDate;
	private String sportId;
	private String sportName;
	private String type;
	private int unixDate;
	private String updatedAt;
	private List<odds> odds;
	
	
	
}
