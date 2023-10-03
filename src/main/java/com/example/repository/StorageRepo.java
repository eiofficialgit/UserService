package com.example.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.entity.ImageData;

public interface StorageRepo extends MongoRepository<ImageData, String> {
	
	Optional<ImageData> findByName(String fileName);

}
