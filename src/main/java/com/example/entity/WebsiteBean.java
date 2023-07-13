package com.example.entity;

import org.springframework.data.mongodb.core.index.Indexed;

public class WebsiteBean {
	
	@Indexed
	String id;
	
	@Indexed 
	String name;
	
	@Indexed 
	Boolean isUsed;
	
	String usedBy;
	
	Integer type;
	
	String parentWebId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getIsUsed() {
		return isUsed;
	}

	public void setIsUsed(Boolean isUsed) {
		this.isUsed = isUsed;
	}

	public String getUsedBy() {
		return usedBy;
	}

	public void setUsedBy(String usedBy) {
		this.usedBy = usedBy;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getParentWebId() {
		return parentWebId;
	}

	public void setParentWebId(String parentWebId) {
		this.parentWebId = parentWebId;
	}
	
	
	

}
