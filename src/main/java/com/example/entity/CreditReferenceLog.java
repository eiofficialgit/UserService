package com.example.entity;

import lombok.AllArgsConstructor; 
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditReferenceLog {
	
	private String date;
	private Double oldValue;
	private Double newValue;
	private String userid;

}