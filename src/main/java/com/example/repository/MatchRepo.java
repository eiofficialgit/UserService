package com.example.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.entity.Match;

public interface MatchRepo extends MongoRepository<Match, String> {

	Match findByeventId(String eventId);

}
