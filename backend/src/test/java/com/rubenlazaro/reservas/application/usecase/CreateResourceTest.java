package com.rubenlazaro.reservas.application.usecase;

import com.rubenlazaro.reservas.application.port.ResourceRepository;
import com.rubenlazaro.reservas.domain.InvalidResourceException;
import com.rubenlazaro.reservas.domain.Resource;
import com.rubenlazaro.reservas.domain.ResourceAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateResourceTest {

    @Mock
    private ResourceRepository resources;

    private CreateResource createResource;

    @BeforeEach
    void setUp() {
        createResource = new CreateResource(resources);
    }

    @Test
    void savesAndReturnsTheNewResource() {
        Resource saved = new Resource(1L, "Court 1");
        when(resources.existsByName("Court 1")).thenReturn(false);
        when(resources.save(Resource.create("Court 1"))).thenReturn(saved);

        Resource result = createResource.execute("Court 1");

        assertThat(result).isEqualTo(saved);
    }

    @Test
    void rejectsADuplicateName() {
        when(resources.existsByName("Court 1")).thenReturn(true);

        assertThatThrownBy(() -> createResource.execute("Court 1"))
                .isInstanceOf(ResourceAlreadyExistsException.class);
        verify(resources, never()).save(any());
    }

    @Test
    void rejectsAnInvalidNameWithoutTouchingTheRepository() {
        assertThatThrownBy(() -> createResource.execute("  "))
                .isInstanceOf(InvalidResourceException.class);
        verifyNoInteractions(resources);
    }
}
