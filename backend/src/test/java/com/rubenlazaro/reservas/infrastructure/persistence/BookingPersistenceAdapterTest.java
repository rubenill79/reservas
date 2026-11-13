package com.rubenlazaro.reservas.infrastructure.persistence;

import com.rubenlazaro.reservas.domain.Booking;
import com.rubenlazaro.reservas.domain.BookingOverlapException;
import com.rubenlazaro.reservas.domain.TimeRange;
import org.junit.jupiter.api.Test;
import org.springframework.dao.CannotAcquireLockException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Translation of failures that cannot be triggered on demand against a real database. The
 * integration tests cover the ones that can (the exclusion constraint, a missing resource).
 */
class BookingPersistenceAdapterTest {

    private final SpringDataBookingRepository repository = mock(SpringDataBookingRepository.class);
    private final BookingPersistenceAdapter adapter = new BookingPersistenceAdapter(repository);

    private static Booking aBooking() {
        Instant start = Instant.parse("2026-01-01T10:00:00Z");
        return Booking.create(1L, 7L, new TimeRange(start, start.plusSeconds(3600)));
    }

    @Test
    void reportsALockFailureAsTheSlotBeingTaken() {
        // When two overlapping inserts race, PostgreSQL may abort the loser with "deadlock detected",
        // which Spring reports as a CannotAcquireLockException. Whether the concurrency test hits that
        // outcome depends on timing, so the translation is pinned here deterministically.
        when(repository.saveAndFlush(any())).thenThrow(new CannotAcquireLockException("deadlock detected"));

        assertThatThrownBy(() -> adapter.save(aBooking()))
                .isInstanceOf(BookingOverlapException.class);
    }
}
