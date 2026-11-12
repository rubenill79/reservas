package com.rubenlazaro.reservas.domain;

import java.util.Objects;

/**
 * Something that can be booked (a court, a room). The id is null until the
 * resource has been persisted.
 */
public record Resource(Long id, String name) {

    /** Mirrors the VARCHAR(100) column of the resource table. */
    public static final int MAX_NAME_LENGTH = 100;

    public Resource {
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new InvalidResourceException("name must not be blank");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new InvalidResourceException(
                    "name must not exceed " + MAX_NAME_LENGTH + " characters");
        }
    }

    public static Resource create(String name) {
        return new Resource(null, name);
    }
}
