package com.rubenlazaro.reservas.infrastructure.rest;

import jakarta.validation.constraints.NotBlank;

/**
 * Bean Validation only checks the shape of the input. The length limit and the other business
 * rules belong to the domain (Resource), which rejects them with InvalidResourceException.
 */
record CreateResourceRequest(@NotBlank String name) {
}
