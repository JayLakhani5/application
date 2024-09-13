package com.usermanagement.usermanagement.rest;

import com.usermanagement.usermanagement.service.CsvFileService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@RestController
public class CsvFileRest {
    private final CsvFileService csvFileService;

    @PostMapping("/upload-csv")
    public ResponseEntity<String> uploadCsvFile(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String response = csvFileService.processCsvFile(file, authorizationHeader);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }
}
