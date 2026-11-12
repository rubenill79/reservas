package com.rubenlazaro.reservas.domain;

import java.time.Instant;
import java.util.Objects;

public record TimeRange(Instant start, Instant end) {

    public TimeRange {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        if (!end.isAfter(start)) {
            throw new InvalidTimeRangeException(
                    "end must be after start (start=" + start + ", end=" + end + ")");
        }
    }

    /**
     * Ranges are half-open, [start, end): the end instant is excluded, so a range
     * that starts exactly when another ends does not overlap it.
     */
    public boolean overlaps(TimeRange other) {
        return start.isBefore(other.end) && other.start.isBefore(end);
    }
}
