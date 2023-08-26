package com.example.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.entity.TransactionHistory;

public interface TransactionHistoryRepo extends MongoRepository<TransactionHistory, String> {

	Page<TransactionHistory> findByfrom(String userid, Pageable pageable);

}
