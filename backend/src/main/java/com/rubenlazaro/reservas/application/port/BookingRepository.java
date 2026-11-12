package com.rubenlazaro.reservas.application.port;

import com.rubenlazaro.reservas.domain.Booking;

import java.util.List;
import java.util.Optional;

/** Outbound port: how the application stores and queries bookings. */
public interface BookingRepository {

    /** Persists the booking and returns it with its generated id. */
    Booking save(Booking booking);

    Optional<Booking> findById(Long id);

    List<Booking> findByResourceId(Long resourceId);

    /** Existing bookings of the same resource whose time range overlaps the given booking's. */
    List<Booking> findConflicting(Booking booking);

    /** Deletes the booking; returns false if there was none with that id. */
    boolean deleteById(Long id);
}
