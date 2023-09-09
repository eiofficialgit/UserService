package com.example.entity;

import org.springframework.data.annotation.Id; 

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {
    
	@Id
	private String newId;
    private String userId;
    private String sessionId;
    private Boolean loggedIn;
   
}