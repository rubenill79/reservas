package com.rubenlazaro.reservas.infrastructure.rest;

import com.rubenlazaro.reservas.IntegrationTest;
import com.rubenlazaro.reservas.application.usecase.CreateBooking;
import com.rubenlazaro.reservas.application.usecase.CreateResource;
import com.rubenlazaro.reservas.domain.Booking;
import com.rubenlazaro.reservas.domain.TimeRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BookingControllerIT extends IntegrationTest {

    private static final Long USER_ID = 7L;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CreateResource createResource;

    @Autowired
    private CreateBooking createBooking;

    private Long courtId;

    @BeforeEach
    void createCourt() {
        courtId = createResource.execute("Court 1").id();
    }

    private static Instant at(String time) {
        return Instant.parse("2026-01-01T" + time + ":00Z");
    }

    private static String bookingJson(String start, String end) {
        return """
                {"userId": %d, "start": "2026-01-01T%s:00Z", "end": "2026-01-01T%s:00Z"}
                """.formatted(USER_ID, start, end);
    }

    private ResultActions postBooking(Long resourceId, String json) throws Exception {
        return mvc.perform(post("/api/resources/{resourceId}/bookings", resourceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private Booking existingBooking(String start, String end) {
        return createBooking.execute(courtId, USER_ID, new TimeRange(at(start), at(end)));
    }

    // --- create ---

    @Test
    void createsABooking() throws Exception {
        postBooking(courtId, bookingJson("10:00", "11:00"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/api/bookings/\\d+")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.resourceId").value(courtId))
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.start").value("2026-01-01T10:00:00Z"))
                .andExpect(jsonPath("$.end").value("2026-01-01T11:00:00Z"));
    }

    @Test
    void createsABookingThatStartsWhenAnotherEnds() throws Exception {
        existingBooking("10:00", "11:00");

        postBooking(courtId, bookingJson("11:00", "12:00")).andExpect(status().isCreated());
    }

    @Test
    void rejectsAnOverlappingBookingWithConflict() throws Exception {
        existingBooking("10:00", "12:00");

        postBooking(courtId, bookingJson("11:00", "13:00"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void rejectsABookingForAnUnknownResource() throws Exception {
        postBooking(999L, bookingJson("10:00", "11:00"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void rejectsARangeWhoseEndIsBeforeItsStart() throws Exception {
        postBooking(courtId, bookingJson("11:00", "10:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void rejectsAnEmptyBodyListingEveryMissingField() throws Exception {
        postBooking(courtId, "{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(3))
                .andExpect(jsonPath("$.errors[*].field").value(
                        org.hamcrest.Matchers.containsInAnyOrder("userId", "start", "end")));
    }

    @Test
    void validationMessagesAreInEnglishWhateverTheClientOrServerLanguage() throws Exception {
        // Without a fixed locale the text depends on the JVM default or on Accept-Language, so
        // the same request would get a different body on a Spanish laptop and on an English server.
        mvc.perform(post("/api/resources/{resourceId}/bookings", courtId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .header("Accept-Language", "es-ES"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field=='userId')].message").value("must not be null"));
    }

    @Test
    void rejectsAnUnparseableDate() throws Exception {
        postBooking(courtId, """
                {"userId": 7, "start": "tomorrow", "end": "2026-01-01T11:00:00Z"}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    // --- read ---

    @Test
    void listsTheBookingsOfAResourceOrderedByStart() throws Exception {
        existingBooking("12:00", "13:00");
        existingBooking("10:00", "11:00");

        mvc.perform(get("/api/resources/{resourceId}/bookings", courtId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].start").value("2026-01-01T10:00:00Z"))
                .andExpect(jsonPath("$[1].start").value("2026-01-01T12:00:00Z"));
    }

    @Test
    void listingTheBookingsOfAnUnknownResourceIsNotFound() throws Exception {
        mvc.perform(get("/api/resources/{resourceId}/bookings", 999L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void getsABooking() throws Exception {
        Booking booking = existingBooking("10:00", "11:00");

        mvc.perform(get("/api/bookings/{id}", booking.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking.id()))
                .andExpect(jsonPath("$.resourceId").value(courtId));
    }

    @Test
    void gettingAnUnknownBookingIsNotFound() throws Exception {
        mvc.perform(get("/api/bookings/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void aNonNumericIdIsABadRequest() throws Exception {
        mvc.perform(get("/api/bookings/{id}", "abc"))
                .andExpect(status().isBadRequest());
    }

    // --- cancel ---

    @Test
    void cancelsABooking() throws Exception {
        Booking booking = existingBooking("10:00", "11:00");

        mvc.perform(delete("/api/bookings/{id}", booking.id()))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/bookings/{id}", booking.id()))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancellingAnUnknownBookingIsNotFound() throws Exception {
        mvc.perform(delete("/api/bookings/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void aCancelledSlotCanBeBookedAgain() throws Exception {
        Booking booking = existingBooking("10:00", "11:00");
        mvc.perform(delete("/api/bookings/{id}", booking.id())).andExpect(status().isNoContent());

        postBooking(courtId, bookingJson("10:00", "11:00")).andExpect(status().isCreated());
    }
}
