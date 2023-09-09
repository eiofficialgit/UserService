package com.example.repository;

import org.springframework.data.mongodb.repository.MongoRepository; 

import com.example.entity.UserSession;

public interface UserSessionRepository extends MongoRepository<UserSession, String> {
    UserSession findByUserId(String userId);
}