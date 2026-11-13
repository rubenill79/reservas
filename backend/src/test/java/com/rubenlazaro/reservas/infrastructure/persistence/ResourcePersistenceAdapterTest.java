package com.rubenlazaro.reservas.infrastructure.persistence;

import com.rubenlazaro.reservas.domain.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The domain rejects every value that would break a constraint other than the unique name, so no
 * real input reaches that path. It is checked here with a failing repository.
 */
class ResourcePersistenceAdapterTest {

    private final SpringDataResourceRepository repository = mock(SpringDataResourceRepository.class);
    private final ResourcePersistenceAdapter adapter = new ResourcePersistenceAdapter(repository);

    @Test
    void doesNotDisguiseAnotherIntegrityViolationAsADuplicateName() {
        DataIntegrityViolationException other =
                new DataIntegrityViolationException("value too long for type character varying(100)");
        when(repository.saveAndFlush(any())).thenThrow(other);

        assertThatThrownBy(() -> adapter.save(Resource.create("Court 1"))).isSameAs(other);
    }
}
