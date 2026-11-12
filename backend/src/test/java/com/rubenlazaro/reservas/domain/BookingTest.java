package com.rubenlazaro.reservas.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingTest {

    private static final Long RESOURCE_ID = 1L;
    private static final Long USER_ID = 7L;

    private static Instant at(String time) {
        return Instant.parse("2026-01-01T" + time + ":00Z");
    }

    private static TimeRange range(String start, String end) {
        return new TimeRange(at(start), at(end));
    }

    private static Booking booking(Long resourceId, String start, String end) {
        return Booking.create(resourceId, USER_ID, range(start, end));
    }

    // --- construction ---

    @Test
    void createsNewBookingWithoutId() {
        TimeRange range = range("10:00", "11:00");

        Booking booking = Booking.create(RESOURCE_ID, USER_ID, range);

        assertThat(booking.id()).isNull();
        assertThat(booking.resourceId()).isEqualTo(RESOURCE_ID);
        assertThat(booking.userId()).isEqualTo(USER_ID);
        assertThat(booking.range()).isEqualTo(range);
    }

    @Test
    void keepsIdWhenRehydratedFromPersistence() {
        Booking booking = new Booking(42L, RESOURCE_ID, USER_ID, range("10:00", "11:00"));

        assertThat(booking.id()).isEqualTo(42L);
    }

    @Test
    void rejectsNullResourceId() {
        assertThatThrownBy(() -> Booking.create(null, USER_ID, range("10:00", "11:00")))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullUserId() {
        assertThatThrownBy(() -> Booking.create(RESOURCE_ID, null, range("10:00", "11:00")))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullRange() {
        assertThatThrownBy(() -> Booking.create(RESOURCE_ID, USER_ID, null))
                .isInstanceOf(NullPointerException.class);
    }

    // --- conflicts: same resource and overlapping time ---

    @Test
    void conflictsWhenSameResourceAndRangesOverlap() {
        Booking existing = booking(1L, "10:00", "12:00");
        Booking requested = booking(1L, "11:00", "13:00");

        assertThat(requested.conflictsWith(existing)).isTrue();
        assertThat(existing.conflictsWith(requested)).isTrue();
    }

    @Test
    void doesNotConflictWhenResourcesDiffer() {
        Booking court1 = booking(1L, "10:00", "12:00");
        Booking court2 = booking(2L, "10:00", "12:00");

        assertThat(court1.conflictsWith(court2)).isFalse();
        assertThat(court2.conflictsWith(court1)).isFalse();
    }

    @Test
    void doesNotConflictWhenSameResourceAndRangesAreAdjacent() {
        Booking morning = booking(1L, "10:00", "11:00");
        Booking next = booking(1L, "11:00", "12:00");

        assertThat(morning.conflictsWith(next)).isFalse();
        assertThat(next.conflictsWith(morning)).isFalse();
    }
}
