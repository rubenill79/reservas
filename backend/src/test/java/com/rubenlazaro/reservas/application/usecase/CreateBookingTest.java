package com.rubenlazaro.reservas.application.usecase;

import com.rubenlazaro.reservas.application.port.BookingRepository;
import com.rubenlazaro.reservas.application.port.ResourceRepository;
import com.rubenlazaro.reservas.domain.Booking;
import com.rubenlazaro.reservas.domain.BookingOverlapException;
import com.rubenlazaro.reservas.domain.ResourceNotFoundException;
import com.rubenlazaro.reservas.domain.TimeRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateBookingTest {

    private static final Long RESOURCE_ID = 1L;
    private static final Long USER_ID = 7L;
    private static final TimeRange RANGE = new TimeRange(at("10:00"), at("11:00"));

    private static Instant at(String time) {
        return Instant.parse("2026-01-01T" + time + ":00Z");
    }

    @Mock
    private ResourceRepository resources;

    @Mock
    private BookingRepository bookings;

    private CreateBooking createBooking;

    @BeforeEach
    void setUp() {
        createBooking = new CreateBooking(resources, bookings);
    }

    @Test
    void savesTheBookingWhenThereAreNoConflicts() {
        Booking candidate = Booking.create(RESOURCE_ID, USER_ID, RANGE);
        Booking saved = new Booking(10L, RESOURCE_ID, USER_ID, RANGE);
        when(resources.existsById(RESOURCE_ID)).thenReturn(true);
        when(bookings.findConflicting(candidate)).thenReturn(List.of());
        when(bookings.save(candidate)).thenReturn(saved);

        Booking result = createBooking.execute(RESOURCE_ID, USER_ID, RANGE);

        assertThat(result).isEqualTo(saved);
    }

    @Test
    void throwsResourceNotFoundWhenTheResourceDoesNotExist() {
        when(resources.existsById(RESOURCE_ID)).thenReturn(false);

        assertThatThrownBy(() -> createBooking.execute(RESOURCE_ID, USER_ID, RANGE))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(bookings);
    }

    @Test
    void throwsBookingOverlapWhenAConflictExists() {
        Booking candidate = Booking.create(RESOURCE_ID, USER_ID, RANGE);
        Booking existing = new Booking(3L, RESOURCE_ID, 99L, new TimeRange(at("10:30"), at("11:30")));
        when(resources.existsById(RESOURCE_ID)).thenReturn(true);
        when(bookings.findConflicting(candidate)).thenReturn(List.of(existing));

        assertThatThrownBy(() -> createBooking.execute(RESOURCE_ID, USER_ID, RANGE))
                .isInstanceOf(BookingOverlapException.class);
        verify(bookings, never()).save(any());
    }
}
