package com.usermanagement.usermanagement.exception;

import java.io.Serial;

public class CsvFileException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 2819291647274833778L;

    public CsvFileException(String message) {
        super(message);
    }

    public CsvFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
