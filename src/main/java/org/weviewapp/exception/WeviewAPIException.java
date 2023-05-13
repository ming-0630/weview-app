package org.weviewapp.exception;

import org.springframework.http.HttpStatus;

public class WeviewAPIException extends RuntimeException {
    private final HttpStatus status;
    private final String message;

    public WeviewAPIException(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public WeviewAPIException(String message, HttpStatus status, String message1) {
        super(message);
        this.status = status;
        this.message = message1;
    }

    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
