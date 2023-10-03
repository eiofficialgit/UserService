package com.example.repository;
 
import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.entity.HyperMessage;

public interface HyperMessageRepo extends MongoRepository<HyperMessage, String> {

}
