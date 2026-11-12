package com.rubenlazaro.reservas.infrastructure.rest;

import com.rubenlazaro.reservas.domain.Resource;

record ResourceResponse(Long id, String name) {

    static ResourceResponse from(Resource resource) {
        return new ResourceResponse(resource.id(), resource.name());
    }
}
