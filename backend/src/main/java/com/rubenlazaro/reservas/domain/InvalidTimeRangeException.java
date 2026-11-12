package com.rubenlazaro.reservas.domain;

public class InvalidTimeRangeException extends RuntimeException {

    public InvalidTimeRangeException(String message) {
        super(message);
    }
}
