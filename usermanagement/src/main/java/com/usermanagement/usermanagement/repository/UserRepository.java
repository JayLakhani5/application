package com.usermanagement.usermanagement.repository;

import com.usermanagement.usermanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("SELECT u FROM User u WHERE (u.email = :value OR u.contactNumber = :value) AND u.password = :password")
    User findByEmailOrContactNumberAndPassword(@Param("value") String value, @Param("password") String password);

    @Query("SELECT u FROM User u WHERE u.email = :value OR u.contactNumber = :value")
    User findByEmailOrContactNumber(@Param("value") String value);

    Optional<User> findByEmail(String email);

    Optional<User> findByContactNumber(String contactNumber);

}

