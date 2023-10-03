package com.example.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.entity.EXUserData;

public interface EXUserDataRepo extends MongoRepository<EXUserData, String> {

	EXUserData findByUserid(String userid);

}
