package com.rubenlazaro.reservas.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface SpringDataResourceRepository extends JpaRepository<ResourceJpaEntity, Long> {

    boolean existsByName(String name);

    List<ResourceJpaEntity> findAllByOrderByIdAsc();
}
