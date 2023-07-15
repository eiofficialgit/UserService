package com.example.repository;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.EXUser;
import com.example.entity.LoginRequest;

@Repository
public interface EXUserRepository extends MongoRepository<EXUser, String> {

	EXUser findByUserid(String lowerCase);

	Object findByUseridAndPasswordAndIsActive(String userid, String password, boolean b);



	
	
	

}
