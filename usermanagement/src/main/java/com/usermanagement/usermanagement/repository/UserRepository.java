package com.usermanagement.usermanagement.repository;

import com.usermanagement.usermanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("SELECT u FROM User u WHERE (u.email = :value OR CAST(u.contactNumber AS string) = :value) AND u.password = :password")
    User findByEmailOrContactNumberAndPassword(@Param("value") String value, @Param("password") String password);


}

