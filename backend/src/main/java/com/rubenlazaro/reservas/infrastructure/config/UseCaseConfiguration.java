package com.rubenlazaro.reservas.infrastructure.config;

import com.rubenlazaro.reservas.application.port.BookingRepository;
import com.rubenlazaro.reservas.application.port.ResourceRepository;
import com.rubenlazaro.reservas.application.usecase.CancelBooking;
import com.rubenlazaro.reservas.application.usecase.CreateBooking;
import com.rubenlazaro.reservas.application.usecase.CreateResource;
import com.rubenlazaro.reservas.application.usecase.GetBooking;
import com.rubenlazaro.reservas.application.usecase.ListBookingsByResource;
import com.rubenlazaro.reservas.application.usecase.ListResources;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the use cases as Spring beans. They are plain Java classes with no Spring
 * annotations, so the wiring lives here, in infrastructure, instead of inside the core.
 */
@Configuration(proxyBeanMethods = false)
class UseCaseConfiguration {

    @Bean
    CreateResource createResource(ResourceRepository resources) {
        return new CreateResource(resources);
    }

    @Bean
    ListResources listResources(ResourceRepository resources) {
        return new ListResources(resources);
    }

    @Bean
    CreateBooking createBooking(ResourceRepository resources, BookingRepository bookings) {
        return new CreateBooking(resources, bookings);
    }

    @Bean
    GetBooking getBooking(BookingRepository bookings) {
        return new GetBooking(bookings);
    }

    @Bean
    ListBookingsByResource listBookingsByResource(ResourceRepository resources, BookingRepository bookings) {
        return new ListBookingsByResource(resources, bookings);
    }

    @Bean
    CancelBooking cancelBooking(BookingRepository bookings) {
        return new CancelBooking(bookings);
    }
}
