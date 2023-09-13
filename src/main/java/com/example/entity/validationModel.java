package com.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class validationModel {
	 
	private String id;
	private String userid;
	private Integer usertype;
	private String websiteName;
	private String password;
	private String email;
	private String firstName;
	private String lastName;
	private String mobileNumber;
	private String exposureLimit;
	private String timeZone;
		  

}
