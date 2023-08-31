package com.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HyperMessage {
	
	private String websiteName;
	private String title;
	private String date;
	private String isLock;

}
