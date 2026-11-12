package com.rubenlazaro.reservas.application.usecase;

import com.rubenlazaro.reservas.application.port.BookingRepository;
import com.rubenlazaro.reservas.domain.BookingNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelBookingTest {

    @Mock
    private BookingRepository bookings;

    private CancelBooking cancelBooking;

    @BeforeEach
    void setUp() {
        cancelBooking = new CancelBooking(bookings);
    }

    @Test
    void deletesTheBooking() {
        when(bookings.deleteById(5L)).thenReturn(true);

        assertThatCode(() -> cancelBooking.execute(5L)).doesNotThrowAnyException();
        verify(bookings).deleteById(5L);
    }

    @Test
    void throwsBookingNotFoundWhenItDoesNotExist() {
        when(bookings.deleteById(5L)).thenReturn(false);

        assertThatThrownBy(() -> cancelBooking.execute(5L))
                .isInstanceOf(BookingNotFoundException.class);
    }
}
