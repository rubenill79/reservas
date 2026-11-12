package com.rubenlazaro.reservas.infrastructure.persistence;

import com.rubenlazaro.reservas.IntegrationTest;
import com.rubenlazaro.reservas.application.port.BookingRepository;
import com.rubenlazaro.reservas.application.port.ResourceRepository;
import com.rubenlazaro.reservas.domain.Booking;
import com.rubenlazaro.reservas.domain.BookingOverlapException;
import com.rubenlazaro.reservas.domain.Resource;
import com.rubenlazaro.reservas.domain.TimeRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingPersistenceAdapterIT extends IntegrationTest {

    private static final Long USER_ID = 7L;

    @Autowired
    private BookingRepository bookings;

    @Autowired
    private ResourceRepository resources;

    private Long court1;
    private Long court2;

    @BeforeEach
    void createResources() {
        court1 = resources.save(Resource.create("Court 1")).id();
        court2 = resources.save(Resource.create("Court 2")).id();
    }

    private static Instant at(int hour) {
        return Instant.parse(String.format("2026-01-01T%02d:00:00Z", hour));
    }

    private static Booking booking(Long resourceId, int startHour, int endHour) {
        return Booking.create(resourceId, USER_ID, new TimeRange(at(startHour), at(endHour)));
    }

    // --- save / find ---

    @Test
    void savesTheBookingAndAssignsAnId() {
        Booking saved = bookings.save(booking(court1, 10, 11));

        assertThat(saved.id()).isNotNull();
        assertThat(saved.resourceId()).isEqualTo(court1);
        assertThat(saved.userId()).isEqualTo(USER_ID);
        assertThat(saved.range()).isEqualTo(new TimeRange(at(10), at(11)));
    }

    @Test
    void findByIdReturnsWhatWasSaved() {
        Booking saved = bookings.save(booking(court1, 10, 11));

        assertThat(bookings.findById(saved.id())).contains(saved);
    }

    @Test
    void findByIdReturnsEmptyWhenThereIsNoSuchBooking() {
        assertThat(bookings.findById(999L)).isEmpty();
    }

    @Test
    void findByResourceIdReturnsOnlyThatResourcesBookingsOrderedByStart() {
        Booking afternoon = bookings.save(booking(court1, 12, 13));
        Booking morning = bookings.save(booking(court1, 10, 11));
        bookings.save(booking(court2, 10, 11));

        assertThat(bookings.findByResourceId(court1)).containsExactly(morning, afternoon);
    }

    // --- conflicts ---

    static Stream<Arguments> candidateHours() {
        List<Arguments> candidates = new ArrayList<>();
        for (int start = 8; start <= 13; start++) {
            for (int end = start + 1; end <= 14; end++) {
                candidates.add(Arguments.of(start, end));
            }
        }
        return candidates.stream();
    }

    /**
     * The overlap rule exists twice: Booking.conflictsWith in the domain and the query behind
     * findConflicting. This checks they agree on every range from 08:00 to 14:00 against an
     * existing 10:00-12:00 booking: before it, touching it, inside it, around it and after it.
     */
    @ParameterizedTest(name = "candidate {0}:00-{1}:00")
    @MethodSource("candidateHours")
    void findConflictingAgreesWithTheDomainRule(int startHour, int endHour) {
        Booking existing = bookings.save(booking(court1, 10, 12));
        Booking candidate = booking(court1, startHour, endHour);

        boolean conflictsInTheDomain = existing.conflictsWith(candidate);
        boolean conflictsInTheDatabase = !bookings.findConflicting(candidate).isEmpty();

        assertThat(conflictsInTheDatabase).isEqualTo(conflictsInTheDomain);
    }

    @Test
    void findConflictingReturnsTheConflictingBooking() {
        Booking existing = bookings.save(booking(court1, 10, 12));

        assertThat(bookings.findConflicting(booking(court1, 11, 13))).containsExactly(existing);
    }

    @Test
    void findConflictingIgnoresOtherResources() {
        bookings.save(booking(court1, 10, 12));

        assertThat(bookings.findConflicting(booking(court2, 10, 12))).isEmpty();
    }

    // --- the database guarantee ---

    @Test
    void savingAnOverlappingBookingThrowsBookingOverlap() {
        bookings.save(booking(court1, 10, 12));

        assertThatThrownBy(() -> bookings.save(booking(court1, 11, 13)))
                .isInstanceOf(BookingOverlapException.class);
    }

    @Test
    void savingBackToBackBookingsIsAllowed() {
        bookings.save(booking(court1, 10, 11));

        assertThatCode(() -> bookings.save(booking(court1, 11, 12))).doesNotThrowAnyException();
    }

    // --- delete ---

    @Test
    void deleteByIdRemovesTheBookingAndReturnsTrue() {
        Booking saved = bookings.save(booking(court1, 10, 11));

        assertThat(bookings.deleteById(saved.id())).isTrue();
        assertThat(bookings.findById(saved.id())).isEmpty();
    }

    @Test
    void deleteByIdReturnsFalseWhenThereIsNoSuchBooking() {
        assertThat(bookings.deleteById(999L)).isFalse();
    }
}
