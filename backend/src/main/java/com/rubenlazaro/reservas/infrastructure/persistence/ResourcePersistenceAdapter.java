package com.rubenlazaro.reservas.infrastructure.persistence;

import com.rubenlazaro.reservas.application.port.ResourceRepository;
import com.rubenlazaro.reservas.domain.Resource;
import com.rubenlazaro.reservas.domain.ResourceAlreadyExistsException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;

/** Outbound adapter: implements the ResourceRepository port with Spring Data JPA. */
@Component
class ResourcePersistenceAdapter implements ResourceRepository {

    /** Name PostgreSQL gives to the UNIQUE constraint on resource.name in migration V1. */
    private static final String UNIQUE_NAME_CONSTRAINT = "resource_name_key";

    private final SpringDataResourceRepository repository;

    ResourcePersistenceAdapter(SpringDataResourceRepository repository) {
        this.repository = repository;
    }

    @Override
    public Resource save(Resource resource) {
        try {
            return toDomain(repository.saveAndFlush(toEntity(resource)));
        } catch (DataIntegrityViolationException e) {
            // CreateResource checks the name first, but two requests can both pass that check.
            if (ConstraintViolations.isViolationOf(UNIQUE_NAME_CONSTRAINT, e)) {
                throw new ResourceAlreadyExistsException(resource.name());
            }
            throw e;
        }
    }

    @Override
    public List<Resource> findAll() {
        return repository.findAllByOrderByIdAsc().stream().map(ResourcePersistenceAdapter::toDomain).toList();
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return repository.existsByName(name);
    }

    private static ResourceJpaEntity toEntity(Resource resource) {
        return new ResourceJpaEntity(resource.id(), resource.name());
    }

    private static Resource toDomain(ResourceJpaEntity entity) {
        return new Resource(entity.getId(), entity.getName());
    }
}
