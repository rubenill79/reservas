package com.rubenlazaro.reservas.application.usecase;

import com.rubenlazaro.reservas.application.port.BookingRepository;
import com.rubenlazaro.reservas.domain.Booking;
import com.rubenlazaro.reservas.domain.BookingNotFoundException;
import com.rubenlazaro.reservas.domain.TimeRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetBookingTest {

    @Mock
    private BookingRepository bookings;

    private GetBooking getBooking;

    @BeforeEach
    void setUp() {
        getBooking = new GetBooking(bookings);
    }

    @Test
    void returnsTheBookingWhenItExists() {
        Booking booking = new Booking(10L, 1L, 7L,
                new TimeRange(Instant.parse("2026-01-01T10:00:00Z"), Instant.parse("2026-01-01T11:00:00Z")));
        when(bookings.findById(10L)).thenReturn(Optional.of(booking));

        assertThat(getBooking.execute(10L)).isEqualTo(booking);
    }

    @Test
    void throwsBookingNotFoundWhenItDoesNotExist() {
        when(bookings.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getBooking.execute(10L))
                .isInstanceOf(BookingNotFoundException.class);
    }
}
