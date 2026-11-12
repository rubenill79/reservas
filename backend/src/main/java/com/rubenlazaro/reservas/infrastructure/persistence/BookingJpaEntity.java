package com.rubenlazaro.reservas.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Persistence model of a booking. Like the domain model it refers to the resource and the user
 * by id, so there are no JPA associations and nothing is loaded lazily.
 */
@Entity
@Table(name = "booking")
class BookingJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resource_id", nullable = false)
    private Long resourceId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    /** Required by JPA. */
    protected BookingJpaEntity() {
    }

    BookingJpaEntity(Long id, Long resourceId, Long userId, Instant startTime, Instant endTime) {
        this.id = id;
        this.resourceId = resourceId;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    Long getId() {
        return id;
    }

    Long getResourceId() {
        return resourceId;
    }

    Long getUserId() {
        return userId;
    }

    Instant getStartTime() {
        return startTime;
    }

    Instant getEndTime() {
        return endTime;
    }
}
