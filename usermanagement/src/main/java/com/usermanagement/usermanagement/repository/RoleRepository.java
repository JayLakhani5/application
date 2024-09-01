package com.usermanagement.usermanagement.repository;

import com.usermanagement.usermanagement.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {
}