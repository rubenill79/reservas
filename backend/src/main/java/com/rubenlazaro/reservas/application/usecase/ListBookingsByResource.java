package com.rubenlazaro.reservas.application.usecase;

import com.rubenlazaro.reservas.application.port.BookingRepository;
import com.rubenlazaro.reservas.application.port.ResourceRepository;
import com.rubenlazaro.reservas.domain.Booking;
import com.rubenlazaro.reservas.domain.ResourceNotFoundException;

import java.util.List;

public class ListBookingsByResource {

    private final ResourceRepository resources;
    private final BookingRepository bookings;

    public ListBookingsByResource(ResourceRepository resources, BookingRepository bookings) {
        this.resources = resources;
        this.bookings = bookings;
    }

    public List<Booking> execute(Long resourceId) {
        if (!resources.existsById(resourceId)) {
            throw new ResourceNotFoundException(resourceId);
        }
        return bookings.findByResourceId(resourceId);
    }
}
