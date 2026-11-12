package com.rubenlazaro.reservas.infrastructure.rest;

import com.rubenlazaro.reservas.domain.Booking;

import java.time.Instant;

record BookingResponse(Long id, Long resourceId, Long userId, Instant start, Instant end) {

    static BookingResponse from(Booking booking) {
        return new BookingResponse(booking.id(), booking.resourceId(), booking.userId(),
                booking.range().start(), booking.range().end());
    }
}
