package com.rubenlazaro.reservas.domain;

import java.util.Objects;

/**
 * A reservation of a resource by a user for a time range. It refers to the
 * resource and the user by id only, so it does not depend on how those
 * concepts evolve. The id is null until the booking has been persisted.
 */
public record Booking(Long id, Long resourceId, Long userId, TimeRange range) {

    public Booking {
        Objects.requireNonNull(resourceId, "resourceId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(range, "range must not be null");
    }

    public static Booking create(Long resourceId, Long userId, TimeRange range) {
        return new Booking(null, resourceId, userId, range);
    }

    /** Two bookings conflict when they are for the same resource and their time ranges overlap. */
    public boolean conflictsWith(Booking other) {
        return resourceId.equals(other.resourceId) && range.overlaps(other.range);
    }
}
