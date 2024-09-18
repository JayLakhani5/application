package com.usermanagement.usermanagement.rest;

import com.usermanagement.usermanagement.service.FileProcessorService;
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
public class FileProcessController {

    private final FileProcessorService fileProcessorService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadCsvFile(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authorizationHeader) {
        String result = fileProcessorService.processCsvFile(file, authorizationHeader);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
