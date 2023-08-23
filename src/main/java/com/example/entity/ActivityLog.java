package com.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityLog {
	
	private String date_time;
	private String loginStatus;
	private String ipAddress;
	private String isp;
	private String city_state_country;
	private String user_agent_type;

}
