package com.example.repository;


import java.util.List; 

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.entity.EXUser;
import com.example.entity.EXUserResponse;



public interface EXUserRepository extends MongoRepository<EXUser, String> {

	EXUser findByUserid(String lowerCase);

	Object findByUseridAndPasswordAndIsActive(String userid, String password, boolean b);

	List<EXUser> findByUsertype(int usertype);
	
	Page<EXUser> findByParentIdAndUsertype(String parentId, Integer usertype, Pageable pageable);

	Page<EXUser> findByUsertype(Integer usertype, Pageable pageable);

	Page<EXUser> findByUsertype(String parentId, Integer usertype, Pageable pageable);

	List<EXUser> findByParentIdAndUsertype(String parentId, Integer usertype);

	List<EXUser> findByuseridContaining(String keywords);

}
