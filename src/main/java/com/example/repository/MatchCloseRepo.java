package com.example.repository;


import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.entity.MatchClose;

public interface MatchCloseRepo extends MongoRepository<MatchClose, String> {

}
