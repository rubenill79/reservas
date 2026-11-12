package com.rubenlazaro.reservas.application.usecase;

import com.rubenlazaro.reservas.application.port.BookingRepository;
import com.rubenlazaro.reservas.application.port.ResourceRepository;
import com.rubenlazaro.reservas.domain.Booking;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListBookingsByResourceTest {

    private static final Long RESOURCE_ID = 1L;

    @Mock
    private ResourceRepository resources;

    @Mock
    private BookingRepository bookings;

    private ListBookingsByResource listBookingsByResource;

    @BeforeEach
    void setUp() {
        listBookingsByResource = new ListBookingsByResource(resources, bookings);
    }

    @Test
    void returnsTheBookingsOfTheResource() {
        List<Booking> found = List.of(new Booking(10L, RESOURCE_ID, 7L,
                new TimeRange(Instant.parse("2026-01-01T10:00:00Z"), Instant.parse("2026-01-01T11:00:00Z"))));
        when(resources.existsById(RESOURCE_ID)).thenReturn(true);
        when(bookings.findByResourceId(RESOURCE_ID)).thenReturn(found);

        assertThat(listBookingsByResource.execute(RESOURCE_ID)).containsExactlyElementsOf(found);
    }

    @Test
    void throwsResourceNotFoundWhenTheResourceDoesNotExist() {
        when(resources.existsById(RESOURCE_ID)).thenReturn(false);

        assertThatThrownBy(() -> listBookingsByResource.execute(RESOURCE_ID))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(bookings);
    }
}
