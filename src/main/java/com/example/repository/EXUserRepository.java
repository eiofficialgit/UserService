package com.example.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.entity.EXUser;

public interface EXUserRepository extends MongoRepository<EXUser, String> {

	EXUser findByUserid(String lowerCase);

	Object findByUseridAndPasswordAndIsActive(String lowerCase, String password, boolean b);
	
	Optional<EXUser> findById(String userid, String password);

}
