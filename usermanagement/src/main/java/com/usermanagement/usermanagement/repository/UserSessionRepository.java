package com.usermanagement.usermanagement.repository;

import com.usermanagement.usermanagement.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserSessionRepository extends JpaRepository<UserSession, Integer> {
}