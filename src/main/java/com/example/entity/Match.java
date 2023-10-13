package com.example.entity;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
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
	private Odds odds;
	private int __v;
	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
	}
	public String getAddType() {
		return addType;
	}
	public void setAddType(String addType) {
		this.addType = addType;
	}
	public String getBbbFancy() {
		return bbbFancy;
	}
	public void setBbbFancy(String bbbFancy) {
		this.bbbFancy = bbbFancy;
	}
	public String getBmProvider() {
		return bmProvider;
	}
	public void setBmProvider(String bmProvider) {
		this.bmProvider = bmProvider;
	}
	public String getChannelNo() {
		return channelNo;
	}
	public void setChannelNo(String channelNo) {
		this.channelNo = channelNo;
	}
	public String getCompetitionId() {
		return competitionId;
	}
	public void setCompetitionId(String competitionId) {
		this.competitionId = competitionId;
	}
	public String getCompetitionName() {
		return competitionName;
	}
	public void setCompetitionName(String competitionName) {
		this.competitionName = competitionName;
	}
	public String getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
	public String getEventId() {
		return eventId;
	}
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	public String getFancyAType() {
		return fancyAType;
	}
	public void setFancyAType(String fancyAType) {
		this.fancyAType = fancyAType;
	}
	public String getFancyProvider() {
		return fancyProvider;
	}
	public void setFancyProvider(String fancyProvider) {
		this.fancyProvider = fancyProvider;
	}
	public boolean isActive() {
		return isActive;
	}
	public void setisActive(boolean isActive) {
		this.isActive = isActive;
	}
	public boolean isResult() {
		return isResult;
	}
	public void setisResult(boolean isResult) {
		this.isResult = isResult;
	}
	public String getmEventId() {
		return mEventId;
	}
	public void setmEventId(String mEventId) {
		this.mEventId = mEventId;
	}
	public String getmType() {
		return mType;
	}
	public void setmType(String mType) {
		this.mType = mType;
	}
	public String getMarketId() {
		return marketId;
	}
	public void setMarketId(String marketId) {
		this.marketId = marketId;
	}
	public List<Object> getMarketIds() {
		return marketIds;
	}
	public void setMarketIds(List<Object> marketIds) {
		this.marketIds = marketIds;
	}
	public String getMarketName() {
		return marketName;
	}
	public void setMarketName(String marketName) {
		this.marketName = marketName;
	}
	public List<Markets> getMarkets() {
		return markets;
	}
	public void setMarkets(List<Markets> markets) {
		this.markets = markets;
	}
	public List<Runners> getMatchRunners() {
		return matchRunners;
	}
	public void setMatchRunners(List<Runners> matchRunners) {
		this.matchRunners = matchRunners;
	}
	public String getOddsProvider() {
		return oddsProvider;
	}
	public void setOddsProvider(String oddsProvider) {
		this.oddsProvider = oddsProvider;
	}
	public String getOpenDate() {
		return openDate;
	}
	public void setOpenDate(String openDate) {
		this.openDate = openDate;
	}
	public String getSportId() {
		return sportId;
	}
	public void setSportId(String sportId) {
		this.sportId = sportId;
	}
	public String getSportName() {
		return sportName;
	}
	public void setSportName(String sportName) {
		this.sportName = sportName;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getUnixDate() {
		return unixDate;
	}
	public void setUnixDate(int unixDate) {
		this.unixDate = unixDate;
	}
	public String getUpdatedAt() {
		return updatedAt;
	}
	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}
	

	
	
	
}
