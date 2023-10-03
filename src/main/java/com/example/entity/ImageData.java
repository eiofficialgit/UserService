package com.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor 
public class ImageData {
	
	private String id;
	private String name;
	private String type;
	private byte[] imageData;
	
	
}
