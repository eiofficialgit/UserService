package com.example.repository;

import org.springframework.data.mongodb.repository.MongoRepository; 


import com.example.entity.EXUser;


public interface Authenticaterepo extends MongoRepository<EXUser, String> {

	EXUser findByUserid(String userid);
	

}
