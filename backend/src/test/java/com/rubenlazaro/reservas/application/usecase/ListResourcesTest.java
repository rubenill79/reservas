package com.rubenlazaro.reservas.application.usecase;

import com.rubenlazaro.reservas.application.port.ResourceRepository;
import com.rubenlazaro.reservas.domain.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListResourcesTest {

    @Mock
    private ResourceRepository resources;

    private ListResources listResources;

    @BeforeEach
    void setUp() {
        listResources = new ListResources(resources);
    }

    @Test
    void returnsAllResources() {
        List<Resource> all = List.of(new Resource(1L, "Court 1"), new Resource(2L, "Court 2"));
        when(resources.findAll()).thenReturn(all);

        assertThat(listResources.execute()).containsExactlyElementsOf(all);
    }

    @Test
    void returnsAnEmptyListWhenThereAreNoResources() {
        when(resources.findAll()).thenReturn(List.of());

        assertThat(listResources.execute()).isEmpty();
    }
}
