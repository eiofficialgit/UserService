package com.example.entity;

import java.util.List;

import org.springframework.data.mongodb.core.index.Indexed;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebsiteBean {
	
	@Indexed
	String id;
	
	@Indexed 
	String name;
	
	@Indexed 
	Boolean isUsed;
	
	List<String> usedBy;
	
	Integer type;
	
	String parentWebId;

	
	

}
