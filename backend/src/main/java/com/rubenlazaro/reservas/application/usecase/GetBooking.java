package com.rubenlazaro.reservas.application.usecase;

import com.rubenlazaro.reservas.application.port.BookingRepository;
import com.rubenlazaro.reservas.domain.Booking;
import com.rubenlazaro.reservas.domain.BookingNotFoundException;

public class GetBooking {

    private final BookingRepository bookings;

    public GetBooking(BookingRepository bookings) {
        this.bookings = bookings;
    }

    public Booking execute(Long id) {
        return bookings.findById(id).orElseThrow(() -> new BookingNotFoundException(id));
    }
}
