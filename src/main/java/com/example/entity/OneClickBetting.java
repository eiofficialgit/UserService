package com.example.entity;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "oneClickBet")
public class OneClickBetting {

	@Indexed
	String id;
	
	@Indexed
	String userid;
	private Integer bet1;
	private Integer bet2;
	private Integer bet3;
	private Integer bet4;
	
	public Integer getBet1() {
		return bet1;
	}
	public void setBet1(Integer bet1) {
		this.bet1 = bet1;
	}
	public Integer getBet2() {
		return bet2;
	}
	public void setBet2(Integer bet2) {
		this.bet2 = bet2;
	}
	public Integer getBet3() {
		return bet3;
	}
	public void setBet3(Integer bet3) {
		this.bet3 = bet3;
	}
	public Integer getBet4() {
		return bet4;
	}
	public void setBet4(Integer bet4) {
		this.bet4 = bet4;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	
	
}
