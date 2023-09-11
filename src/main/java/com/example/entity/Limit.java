package com.example.entity;

import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Limit {
	
	@Id
	private String id;
	private boolean baseCurrency;
	private int delay;
	private String name;
	private int maxPL;
	private int maxStake;
	private int minStake;
	private int oddsLimit;
	private int preMaxPL;
	private int preMaxStake;
	private int preMinStake;
	
}
