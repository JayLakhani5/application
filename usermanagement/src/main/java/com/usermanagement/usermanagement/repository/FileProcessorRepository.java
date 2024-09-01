package com.usermanagement.usermanagement.repository;

import com.usermanagement.usermanagement.entity.FileProcessor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileProcessorRepository extends JpaRepository<FileProcessor, Integer> {
}