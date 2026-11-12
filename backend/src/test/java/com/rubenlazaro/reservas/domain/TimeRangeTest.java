package com.rubenlazaro.reservas.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimeRangeTest {

    private static Instant at(String time) {
        return Instant.parse("2026-01-01T" + time + ":00Z");
    }

    private static TimeRange range(String start, String end) {
        return new TimeRange(at(start), at(end));
    }

    // --- construction ---

    @Test
    void createsRangeWhenEndIsAfterStart() {
        TimeRange range = new TimeRange(at("10:00"), at("11:00"));

        assertThat(range.start()).isEqualTo(at("10:00"));
        assertThat(range.end()).isEqualTo(at("11:00"));
    }

    @Test
    void rejectsRangeWhenEndEqualsStart() {
        assertThatThrownBy(() -> new TimeRange(at("10:00"), at("10:00")))
                .isInstanceOf(InvalidTimeRangeException.class);
    }

    @Test
    void rejectsRangeWhenEndIsBeforeStart() {
        assertThatThrownBy(() -> new TimeRange(at("11:00"), at("10:00")))
                .isInstanceOf(InvalidTimeRangeException.class);
    }

    @Test
    void rejectsNullStart() {
        assertThatThrownBy(() -> new TimeRange(null, at("11:00")))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullEnd() {
        assertThatThrownBy(() -> new TimeRange(at("10:00"), null))
                .isInstanceOf(NullPointerException.class);
    }

    // --- overlap: ranges are half-open, [start, end) ---

    @Test
    void overlapsWhenRangesIntersect() {
        assertThat(range("10:00", "12:00").overlaps(range("11:00", "13:00"))).isTrue();
    }

    @Test
    void doesNotOverlapWhenRangesAreSeparate() {
        assertThat(range("10:00", "11:00").overlaps(range("12:00", "13:00"))).isFalse();
    }

    @Test
    void doesNotOverlapWhenRangesAreAdjacent() {
        TimeRange first = range("10:00", "11:00");
        TimeRange second = range("11:00", "12:00");

        assertThat(first.overlaps(second)).isFalse();
        assertThat(second.overlaps(first)).isFalse();
    }

    @Test
    void overlapsWhenOneContainsTheOther() {
        TimeRange outer = range("10:00", "14:00");
        TimeRange inner = range("11:00", "12:00");

        assertThat(outer.overlaps(inner)).isTrue();
        assertThat(inner.overlaps(outer)).isTrue();
    }

    @Test
    void overlapsWhenRangesAreIdentical() {
        assertThat(range("10:00", "11:00").overlaps(range("10:00", "11:00"))).isTrue();
    }

    @Test
    void overlapIsSymmetric() {
        TimeRange a = range("10:00", "12:00");
        TimeRange intersecting = range("11:00", "13:00");
        TimeRange adjacent = range("12:00", "13:00");

        assertThat(a.overlaps(intersecting)).isEqualTo(intersecting.overlaps(a));
        assertThat(a.overlaps(adjacent)).isEqualTo(adjacent.overlaps(a));
    }
}
