package com.example.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.entity.TransactionHistory;

public interface TransactionHistoryRepo extends MongoRepository<TransactionHistory, String> {

}
