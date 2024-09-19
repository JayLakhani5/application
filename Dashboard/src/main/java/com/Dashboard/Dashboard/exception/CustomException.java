package com.Dashboard.Dashboard.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serial;


@Getter
public class CustomException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -2022959396872140238L;
    private final HttpStatus status;

    public CustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

}
