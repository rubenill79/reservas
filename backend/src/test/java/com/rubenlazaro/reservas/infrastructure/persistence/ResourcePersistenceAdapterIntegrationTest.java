package com.rubenlazaro.reservas.infrastructure.persistence;

import com.rubenlazaro.reservas.IntegrationTest;
import com.rubenlazaro.reservas.application.port.ResourceRepository;
import com.rubenlazaro.reservas.domain.Resource;
import com.rubenlazaro.reservas.domain.ResourceAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourcePersistenceAdapterIntegrationTest extends IntegrationTest {

    @Autowired
    private ResourceRepository resources;

    @Test
    void savesTheResourceAndAssignsAnId() {
        Resource saved = resources.save(Resource.create("Court 1"));

        assertThat(saved.id()).isNotNull();
        assertThat(saved.name()).isEqualTo("Court 1");
    }

    @Test
    void findAllReturnsEveryResourceInCreationOrder() {
        Resource first = resources.save(Resource.create("Court 2"));
        Resource second = resources.save(Resource.create("Court 1"));

        assertThat(resources.findAll()).containsExactly(first, second);
    }

    @Test
    void findAllReturnsAnEmptyListWhenThereAreNoResources() {
        assertThat(resources.findAll()).isEmpty();
    }

    @Test
    void existsByIdReflectsWhatIsStored() {
        Resource saved = resources.save(Resource.create("Court 1"));

        assertThat(resources.existsById(saved.id())).isTrue();
        assertThat(resources.existsById(saved.id() + 1)).isFalse();
    }

    @Test
    void existsByNameReflectsWhatIsStored() {
        resources.save(Resource.create("Court 1"));

        assertThat(resources.existsByName("Court 1")).isTrue();
        assertThat(resources.existsByName("Court 2")).isFalse();
    }

    @Test
    void savingADuplicateNameThrowsResourceAlreadyExists() {
        resources.save(Resource.create("Court 1"));

        assertThatThrownBy(() -> resources.save(Resource.create("Court 1")))
                .isInstanceOf(ResourceAlreadyExistsException.class);
    }
}
