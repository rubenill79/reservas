package com.rubenlazaro.reservas.application.usecase;

import com.rubenlazaro.reservas.application.port.ResourceRepository;
import com.rubenlazaro.reservas.domain.Resource;

import java.util.List;

public class ListResources {

    private final ResourceRepository resources;

    public ListResources(ResourceRepository resources) {
        this.resources = resources;
    }

    public List<Resource> execute() {
        return resources.findAll();
    }
}
