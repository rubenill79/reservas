package com.rubenlazaro.reservas;

import com.rubenlazaro.reservas.application.usecase.CancelBooking;
import com.rubenlazaro.reservas.application.usecase.CreateBooking;
import com.rubenlazaro.reservas.application.usecase.CreateResource;
import com.rubenlazaro.reservas.application.usecase.GetBooking;
import com.rubenlazaro.reservas.application.usecase.ListBookingsByResource;
import com.rubenlazaro.reservas.application.usecase.ListResources;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReservasApplicationTests extends IntegrationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
    }

    @Test
    void everyUseCaseIsWiredAsABean() {
        List<Class<?>> useCases = List.of(
                CreateResource.class, ListResources.class,
                CreateBooking.class, GetBooking.class,
                ListBookingsByResource.class, CancelBooking.class);

        for (Class<?> useCase : useCases) {
            assertThat(context.getBeanNamesForType(useCase))
                    .as("beans of type %s", useCase.getSimpleName())
                    .hasSize(1);
        }
    }
}
