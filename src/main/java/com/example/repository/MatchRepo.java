package com.example.repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.entity.Match;

public interface MatchRepo extends MongoRepository<Match, String> {

	List<Match> findByisActive(boolean isActive);

	List<Match> findByOpenDateAfterOrderByOpenDateAsc(String todayDate, Sort sort);

	Match findByMarketId(String marketId);

	List<Match> findBySportId(String sportid);
	
	List<Match> findBySportIdAndIsActive(String sportid, boolean isActive);

	List<Match> findBycompetitionId(String competitionId);

	List<Match> findByeventId(String eventId);

	List<Match> findByOpenDateGreaterThanEqualOrderByOpenDate(String todayDate);

	boolean existsByEventId(String eventId);

	List<Match> findByCompetitionName(String competitionname);

	List<Match> findByOpenDateGreaterThanEqual(String todayDate);

	List<Match> findByOpenDateGreaterThanEqual(String todayDate, Sort sort);

	List<Match> findByOpenDateGreaterThanEqualOrderByOpenDate(String todayDate, Sort sort);

	

	

}
