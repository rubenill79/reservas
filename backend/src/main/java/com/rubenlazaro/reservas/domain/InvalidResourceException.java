package com.rubenlazaro.reservas.domain;

public class InvalidResourceException extends RuntimeException {

    public InvalidResourceException(String message) {
        super(message);
    }
}
