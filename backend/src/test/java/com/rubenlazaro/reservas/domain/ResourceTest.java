package com.rubenlazaro.reservas.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceTest {

    // The database column is VARCHAR(100), so the domain enforces the same limit.
    private static final int MAX_NAME_LENGTH = 100;

    @Test
    void createsNewResourceWithoutId() {
        Resource resource = Resource.create("Court 1");

        assertThat(resource.id()).isNull();
        assertThat(resource.name()).isEqualTo("Court 1");
    }

    @Test
    void keepsIdWhenRehydratedFromPersistence() {
        Resource resource = new Resource(5L, "Court 1");

        assertThat(resource.id()).isEqualTo(5L);
    }

    @Test
    void rejectsNullName() {
        assertThatThrownBy(() -> Resource.create(null))
                .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   "})
    void rejectsBlankName(String blank) {
        assertThatThrownBy(() -> Resource.create(blank))
                .isInstanceOf(InvalidResourceException.class);
    }

    @Test
    void acceptsNameOfMaximumLength() {
        Resource resource = Resource.create("a".repeat(MAX_NAME_LENGTH));

        assertThat(resource.name()).hasSize(MAX_NAME_LENGTH);
    }

    @Test
    void rejectsNameLongerThanMaximumLength() {
        assertThatThrownBy(() -> Resource.create("a".repeat(MAX_NAME_LENGTH + 1)))
                .isInstanceOf(InvalidResourceException.class);
    }
}
