package com.example.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchClose {
	
	private String id;
	private String marketid;
	private String marketname;
	private String sportid;
	private String sportname;
	private String matchid;
	private String matchname;
	private String eventDateTime;
	private String selectionname;
	private int selectionid;
	private String resultDate;
	private String resultLong;
	private String resultIP;
	private boolean status;

}
