package com.rubenlazaro.reservas.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

interface SpringDataBookingRepository extends JpaRepository<BookingJpaEntity, Long> {

    List<BookingJpaEntity> findByResourceIdOrderByStartTimeAsc(Long resourceId);

    /**
     * Ranges are half-open, [start, end): the same rule as TimeRange.overlaps and as the
     * booking_no_overlap constraint, so back-to-back bookings do not conflict.
     */
    @Query("""
            select b from BookingJpaEntity b
            where b.resourceId = :resourceId
              and b.startTime < :end
              and b.endTime > :start
            order by b.startTime
            """)
    List<BookingJpaEntity> findConflicting(@Param("resourceId") Long resourceId,
                                           @Param("start") Instant start,
                                           @Param("end") Instant end);

    /** Returns the number of deleted rows; a modifying query needs its own transaction. */
    @Transactional
    @Modifying
    @Query("delete from BookingJpaEntity b where b.id = :id")
    int deleteBookingById(@Param("id") Long id);
}
