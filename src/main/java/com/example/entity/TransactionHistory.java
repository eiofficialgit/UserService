package com.example.entity;
 

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionHistory {
	
	private String date_time;
	private Double depositFromUpline;
	private Double depositToDownline;
	private Double withdrawByUpline;
	private Double withdrawFromDownline;
	private Double balance;
	private String remark;
	private String from;
	private String to;
	
}