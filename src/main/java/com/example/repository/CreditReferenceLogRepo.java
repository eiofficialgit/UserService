package com.example.repository;

import org.springframework.data.domain.Page; 
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.entity.CreditReferenceLog;

public interface CreditReferenceLogRepo extends MongoRepository<CreditReferenceLog, String> {

	Page<CreditReferenceLog> findByuserid(String userid, Pageable pageable);

}

