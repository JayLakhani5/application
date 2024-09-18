package com.usermanagement.usermanagement.repository;

import com.usermanagement.usermanagement.entity.Role;
import com.usermanagement.usermanagement.entity.User;
import com.usermanagement.usermanagement.entity.UserRoleMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRoleMappingRepository extends JpaRepository<UserRoleMapping, Integer> {
    Optional<UserRoleMapping> findByUserIdAndRoleId(User userId, Role roleId);

    List<UserRoleMapping> findByUserId(User user);

}