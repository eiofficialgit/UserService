package com.example.service;

import java.util.List; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.entity.EXUser;
import com.example.entity.UserSession;
import com.example.repository.UserSessionRepository;

@Service
public class SessionManagementService {

    private final UserSessionRepository userSessionRepository;

    @Autowired
    public SessionManagementService(UserSessionRepository userSessionRepository) {
        this.userSessionRepository = userSessionRepository;
    }

    public void invalidatePreviousSessions(EXUser user) {
       UserSession session = userSessionRepository.findByUserId(user.getUserid());
            session.setLoggedIn(false); // Mark previous sessions as not logged in
            userSessionRepository.save(session);
    }
}