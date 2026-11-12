package com.rubenlazaro.reservas.infrastructure.persistence;

import com.rubenlazaro.reservas.application.port.BookingRepository;
import com.rubenlazaro.reservas.domain.Booking;
import com.rubenlazaro.reservas.domain.BookingOverlapException;
import com.rubenlazaro.reservas.domain.TimeRange;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Outbound adapter: implements the BookingRepository port with Spring Data JPA. */
@Component
class BookingPersistenceAdapter implements BookingRepository {

    /** Exclusion constraint on booking, created in migration V2. */
    private static final String OVERLAP_CONSTRAINT = "booking_no_overlap";

    private final SpringDataBookingRepository repository;

    BookingPersistenceAdapter(SpringDataBookingRepository repository) {
        this.repository = repository;
    }

    @Override
    public Booking save(Booking booking) {
        try {
            // Flush now so a constraint violation surfaces here, where it can be translated.
            return toDomain(repository.saveAndFlush(toEntity(booking)));
        } catch (DataIntegrityViolationException e) {
            // The conflict check in CreateBooking is a read followed by a write. When two
            // requests race, the database constraint is what rejects the loser.
            if (ConstraintViolations.isViolationOf(OVERLAP_CONSTRAINT, e)) {
                throw new BookingOverlapException(booking.resourceId(), booking.range());
            }
            throw e;
        } catch (CannotAcquireLockException | DeadlockLoserDataAccessException e) {
            // When overlapping rows are inserted at the same instant, each exclusion check waits
            // for the other's uncommitted row, and PostgreSQL breaks the cycle by aborting one
            // transaction with "deadlock detected" instead of an exclusion violation. This insert
            // only waits on bookings that conflict with it, and the surviving one takes the slot,
            // so for the loser the outcome is the same: the slot is already taken.
            throw new BookingOverlapException(booking.resourceId(), booking.range());
        }
    }

    @Override
    public Optional<Booking> findById(Long id) {
        return repository.findById(id).map(BookingPersistenceAdapter::toDomain);
    }

    @Override
    public List<Booking> findByResourceId(Long resourceId) {
        return repository.findByResourceIdOrderByStartTimeAsc(resourceId).stream()
                .map(BookingPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<Booking> findConflicting(Booking booking) {
        return repository.findConflicting(
                        booking.resourceId(), booking.range().start(), booking.range().end()).stream()
                .map(BookingPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public boolean deleteById(Long id) {
        return repository.deleteBookingById(id) > 0;
    }

    private static BookingJpaEntity toEntity(Booking booking) {
        return new BookingJpaEntity(booking.id(), booking.resourceId(), booking.userId(),
                booking.range().start(), booking.range().end());
    }

    private static Booking toDomain(BookingJpaEntity entity) {
        return new Booking(entity.getId(), entity.getResourceId(), entity.getUserId(),
                new TimeRange(entity.getStartTime(), entity.getEndTime()));
    }
}
