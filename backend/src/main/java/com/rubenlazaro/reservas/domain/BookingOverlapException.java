package com.rubenlazaro.reservas.domain;

public class BookingOverlapException extends RuntimeException {

    public BookingOverlapException(Long resourceId, TimeRange range) {
        super("Resource " + resourceId + " is already booked between "
                + range.start() + " and " + range.end());
    }
}
