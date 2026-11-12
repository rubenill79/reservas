package com.rubenlazaro.reservas.domain;

public class ResourceAlreadyExistsException extends RuntimeException {

    public ResourceAlreadyExistsException(String name) {
        super("A resource named '" + name + "' already exists");
    }
}
