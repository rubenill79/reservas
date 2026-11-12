package com.rubenlazaro.reservas.application.usecase;

import com.rubenlazaro.reservas.application.port.BookingRepository;
import com.rubenlazaro.reservas.domain.BookingNotFoundException;

public class CancelBooking {

    private final BookingRepository bookings;

    public CancelBooking(BookingRepository bookings) {
        this.bookings = bookings;
    }

    /** Cancelling a booking deletes it. */
    public void execute(Long id) {
        if (!bookings.deleteById(id)) {
            throw new BookingNotFoundException(id);
        }
    }
}
