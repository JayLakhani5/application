package com.usermanagement.usermanagement.repository;

import com.usermanagement.usermanagement.entity.FileProcessor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileProcessorRepository extends JpaRepository<FileProcessor, Integer> {
    Optional<FileProcessor> findFirstByUserId(int userId);
}