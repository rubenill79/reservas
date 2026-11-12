-- V2: owner of a booking, timezone-aware timestamps and overlap protection in the database.

-- Needed so a GiST index can combine equality on resource_id with range overlap.
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- Who made the booking. No foreign key yet: the users table arrives with security (phase 3).
-- NOT NULL without a default is safe because the table is still empty.
ALTER TABLE booking
    ADD COLUMN user_id BIGINT NOT NULL;

-- Timestamps are instants (java.time.Instant in the domain), so store them with time zone.
-- Existing values, if any, were written as UTC.
ALTER TABLE booking
    ALTER COLUMN start_time TYPE TIMESTAMPTZ USING start_time AT TIME ZONE 'UTC',
    ALTER COLUMN end_time   TYPE TIMESTAMPTZ USING end_time   AT TIME ZONE 'UTC';

-- Two bookings of the same resource can never overlap, even under concurrent requests.
-- tstzrange(start, end) is half-open, [start, end), the same convention as TimeRange.overlaps().
ALTER TABLE booking
    ADD CONSTRAINT booking_no_overlap
    EXCLUDE USING gist (
        resource_id WITH =,
        tstzrange(start_time, end_time) WITH &&
    );
