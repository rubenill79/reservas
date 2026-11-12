package com.rubenlazaro.reservas.application.usecase;

import com.rubenlazaro.reservas.application.port.BookingRepository;
import com.rubenlazaro.reservas.application.port.ResourceRepository;
import com.rubenlazaro.reservas.domain.Booking;
import com.rubenlazaro.reservas.domain.BookingOverlapException;
import com.rubenlazaro.reservas.domain.ResourceNotFoundException;
import com.rubenlazaro.reservas.domain.TimeRange;

public class CreateBooking {

    private final ResourceRepository resources;
    private final BookingRepository bookings;

    public CreateBooking(ResourceRepository resources, BookingRepository bookings) {
        this.resources = resources;
        this.bookings = bookings;
    }

    /**
     * Gives a clear error for the common case. Under concurrent requests two callers can
     * both pass the conflict check, so the database exclusion constraint stays the final guarantee.
     */
    public Booking execute(Long resourceId, Long userId, TimeRange range) {
        if (!resources.existsById(resourceId)) {
            throw new ResourceNotFoundException(resourceId);
        }
        Booking candidate = Booking.create(resourceId, userId, range);
        if (!bookings.findConflicting(candidate).isEmpty()) {
            throw new BookingOverlapException(resourceId, range);
        }
        return bookings.save(candidate);
    }
}
