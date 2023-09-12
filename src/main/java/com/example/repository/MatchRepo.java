package com.example.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.entity.Match;

public interface MatchRepo extends MongoRepository<Match, String> {

	Match findByeventId(String eventId);

	List<Match> findByisActive(boolean isActive);

}
