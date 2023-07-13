package com.example.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.entity.WebsiteBean;

public interface WebsiteBeanRepository extends MongoRepository<WebsiteBean, String> {

	WebsiteBean findByid(String id);

	Object findByName(String name);

}
