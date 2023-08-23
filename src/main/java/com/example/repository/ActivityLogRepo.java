package com.example.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.entity.ActivityLog;

public interface ActivityLogRepo extends MongoRepository<ActivityLog, String> {

}
