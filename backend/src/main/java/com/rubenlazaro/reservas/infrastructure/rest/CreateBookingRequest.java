package com.rubenlazaro.reservas.infrastructure.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

record CreateBookingRequest(
        @Schema(description = "PROVISIONAL. Until authentication is added (phase 3) the client sends "
                + "the user making the booking; afterwards it will be taken from the access token "
                + "and this field will be removed.")
        @NotNull Long userId,

        @Schema(description = "Start of the booking, inclusive (ISO-8601 instant).",
                example = "2026-01-01T10:00:00Z")
        @NotNull Instant start,

        @Schema(description = "End of the booking, exclusive: a booking may start exactly when "
                + "another one ends (ISO-8601 instant).",
                example = "2026-01-01T11:00:00Z")
        @NotNull Instant end) {
}
