package com.example.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.entity.ImportantMessage;

public interface ImportantMessageRepo extends MongoRepository<ImportantMessage, String> {

}
