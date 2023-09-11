package com.example.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Markets {
	
	private String marketId;
	private String marketName;
	private List<Runners> runners;
	private boolean status;
	private List<Limit> limit;

}
