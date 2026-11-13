package com.rubenlazaro.reservas.infrastructure.persistence;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConstraintViolationsTest {

    private static final String CONSTRAINT = "booking_no_overlap";

    @Test
    void recognisesTheConstraintNamedInTheMessage() {
        Throwable error = new RuntimeException(
                "conflicting key value violates exclusion constraint \"booking_no_overlap\"");

        assertThat(ConstraintViolations.isViolationOf(CONSTRAINT, error)).isTrue();
    }

    @Test
    void findsTheConstraintDeepInTheCauseChain() {
        // The driver's message sits at the end of the chain, under Spring's and Hibernate's wrappers.
        Throwable error = new RuntimeException("could not execute statement",
                new IllegalStateException("persistence failure",
                        new RuntimeException("violates exclusion constraint \"booking_no_overlap\"")));

        assertThat(ConstraintViolations.isViolationOf(CONSTRAINT, error)).isTrue();
    }

    @Test
    void rejectsAViolationOfAnotherConstraint() {
        Throwable error = new RuntimeException(
                "insert or update violates foreign key constraint \"booking_resource_id_fkey\"");

        assertThat(ConstraintViolations.isViolationOf(CONSTRAINT, error)).isFalse();
    }

    @Test
    void requiresTheWholeQuotedNameNotJustAPrefix() {
        // "booking_no_overlap_v2" is a different constraint: only the exact quoted name counts.
        Throwable error = new RuntimeException("violates exclusion constraint \"booking_no_overlap_v2\"");

        assertThat(ConstraintViolations.isViolationOf(CONSTRAINT, error)).isFalse();
    }

    @Test
    void skipsCausesWithoutAMessageAndKeepsLooking() {
        Throwable error = new RuntimeException((String) null,
                new RuntimeException("violates exclusion constraint \"booking_no_overlap\""));

        assertThat(ConstraintViolations.isViolationOf(CONSTRAINT, error)).isTrue();
    }

    @Test
    void isFalseWhenNoCauseHasAMessage() {
        Throwable error = new RuntimeException((String) null, new RuntimeException((String) null));

        assertThat(ConstraintViolations.isViolationOf(CONSTRAINT, error)).isFalse();
    }

    @Test
    void isFalseWhenThereIsNoError() {
        assertThat(ConstraintViolations.isViolationOf(CONSTRAINT, null)).isFalse();
    }
}
