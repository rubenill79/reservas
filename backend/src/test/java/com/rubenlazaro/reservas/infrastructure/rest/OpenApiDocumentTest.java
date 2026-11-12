package com.rubenlazaro.reservas.infrastructure.rest;

import com.rubenlazaro.reservas.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The OpenAPI document is the contract the React client will be generated from, so it is
 * worth a test that the endpoints are in it.
 */
class OpenApiDocumentTest extends IntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void servesAnOpenApi3DocumentDescribingEveryEndpoint() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").value(startsWith("3.")))
                .andExpect(jsonPath("$.paths['/api/resources'].get").exists())
                .andExpect(jsonPath("$.paths['/api/resources'].post").exists())
                .andExpect(jsonPath("$.paths['/api/resources/{resourceId}/bookings'].get").exists())
                .andExpect(jsonPath("$.paths['/api/resources/{resourceId}/bookings'].post").exists())
                .andExpect(jsonPath("$.paths['/api/bookings/{id}'].get").exists())
                .andExpect(jsonPath("$.paths['/api/bookings/{id}'].delete").exists());
    }

    @Test
    void documentsTheBookingRequestFields() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.schemas.CreateBookingRequest.properties.userId").exists())
                .andExpect(jsonPath("$.components.schemas.CreateBookingRequest.required").isArray());
    }
}
