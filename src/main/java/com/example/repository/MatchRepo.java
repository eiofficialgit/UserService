package com.example.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.entity.Match;

public interface MatchRepo extends MongoRepository<Match, String> {

	List<Match> findByisActive(boolean isActive);

	List<Match> findByOpenDateGreaterThanEqual(String date);

	Match findBymarketId(String marketId);

	List<Match> findBySportId(String sportid);

	

}
