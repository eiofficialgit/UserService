package com.example.entity;

import java.util.List;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
	private boolean isActive;
	private boolean isResult;
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
	private int __v;
	
}
