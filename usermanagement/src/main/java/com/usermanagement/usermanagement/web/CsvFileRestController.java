package com.usermanagement.usermanagement.web;

import com.usermanagement.usermanagement.exception.CsvFileException;
import com.usermanagement.usermanagement.service.CsvFileService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@RestController
public class CsvFileRestController {
    private final CsvFileService csvFileService;

    @PostMapping("/upload-csv")
    public ResponseEntity<String> uploadCsvFile(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String response = csvFileService.processCsvFile(file, authorizationHeader);
            return ResponseEntity.ok(response);
        } catch (CsvFileException e) {
            throw new CsvFileException(e.getMessage());
        } catch (RuntimeException e) {
            throw new CsvFileException("Unauthorized access: " + e.getMessage());
        } catch (Exception e) {
            throw new CsvFileException("An unexpected error occurred while processing the CSV file.");
        }
    }
}
