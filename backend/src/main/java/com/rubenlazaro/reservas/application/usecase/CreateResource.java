package com.rubenlazaro.reservas.application.usecase;

import com.rubenlazaro.reservas.application.port.ResourceRepository;
import com.rubenlazaro.reservas.domain.Resource;
import com.rubenlazaro.reservas.domain.ResourceAlreadyExistsException;

public class CreateResource {

    private final ResourceRepository resources;

    public CreateResource(ResourceRepository resources) {
        this.resources = resources;
    }

    public Resource execute(String name) {
        // Build the domain object first: it validates the name before any repository call.
        Resource resource = Resource.create(name);
        if (resources.existsByName(name)) {
            throw new ResourceAlreadyExistsException(name);
        }
        return resources.save(resource);
    }
}
