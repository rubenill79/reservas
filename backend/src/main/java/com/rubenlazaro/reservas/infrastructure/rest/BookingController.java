package com.rubenlazaro.reservas.infrastructure.rest;

import com.rubenlazaro.reservas.application.usecase.CancelBooking;
import com.rubenlazaro.reservas.application.usecase.CreateBooking;
import com.rubenlazaro.reservas.application.usecase.GetBooking;
import com.rubenlazaro.reservas.application.usecase.ListBookingsByResource;
import com.rubenlazaro.reservas.domain.Booking;
import com.rubenlazaro.reservas.domain.TimeRange;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/** Inbound adapter: translates HTTP into calls to the booking use cases. */
@RestController
@RequestMapping("/api")
@Tag(name = "Bookings", description = "Reservations of a resource for a time range")
class BookingController {

    private final CreateBooking createBooking;
    private final GetBooking getBooking;
    private final ListBookingsByResource listBookingsByResource;
    private final CancelBooking cancelBooking;

    BookingController(CreateBooking createBooking, GetBooking getBooking,
                      ListBookingsByResource listBookingsByResource, CancelBooking cancelBooking) {
        this.createBooking = createBooking;
        this.getBooking = getBooking;
        this.listBookingsByResource = listBookingsByResource;
        this.cancelBooking = cancelBooking;
    }

    @PostMapping("/resources/{resourceId}/bookings")
    @Operation(summary = "Book a resource for a time range")
    @ApiResponse(responseCode = "400", description = "Missing fields, malformed dates, or end not after start")
    @ApiResponse(responseCode = "404", description = "The resource does not exist")
    @ApiResponse(responseCode = "409", description = "The resource is already booked in part of that range")
    ResponseEntity<BookingResponse> create(@PathVariable Long resourceId,
                                           @Valid @RequestBody CreateBookingRequest request) {
        Booking booking = createBooking.execute(resourceId, request.userId(),
                new TimeRange(request.start(), request.end()));
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/bookings/{id}")
                .buildAndExpand(booking.id())
                .toUri();
        return ResponseEntity.created(location).body(BookingResponse.from(booking));
    }

    @GetMapping("/resources/{resourceId}/bookings")
    @Operation(summary = "List the bookings of a resource, earliest first")
    @ApiResponse(responseCode = "404", description = "The resource does not exist")
    List<BookingResponse> listByResource(@PathVariable Long resourceId) {
        return listBookingsByResource.execute(resourceId).stream().map(BookingResponse::from).toList();
    }

    @GetMapping("/bookings/{id}")
    @Operation(summary = "Get a booking")
    @ApiResponse(responseCode = "404", description = "The booking does not exist")
    BookingResponse get(@PathVariable Long id) {
        return BookingResponse.from(getBooking.execute(id));
    }

    @DeleteMapping("/bookings/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancel a booking", description = "Cancelling deletes the booking, so the slot is free again.")
    @ApiResponse(responseCode = "404", description = "The booking does not exist")
    void cancel(@PathVariable Long id) {
        cancelBooking.execute(id);
    }
}
