package com.rubenlazaro.reservas.application.port;

import com.rubenlazaro.reservas.domain.Resource;

import java.util.List;

/** Outbound port: how the application stores and queries resources. */
public interface ResourceRepository {

    /** Persists the resource and returns it with its generated id. */
    Resource save(Resource resource);

    List<Resource> findAll();

    boolean existsById(Long id);

    boolean existsByName(String name);
}
