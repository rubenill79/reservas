package com.rubenlazaro.reservas.infrastructure.rest;

import com.rubenlazaro.reservas.application.usecase.CreateResource;
import com.rubenlazaro.reservas.application.usecase.ListResources;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Inbound adapter: translates HTTP into calls to the resource use cases. */
@RestController
@RequestMapping("/api/resources")
@Tag(name = "Resources", description = "Things that can be booked, such as courts or rooms")
class ResourceController {

    private final CreateResource createResource;
    private final ListResources listResources;

    ResourceController(CreateResource createResource, ListResources listResources) {
        this.createResource = createResource;
        this.listResources = listResources;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a resource")
    @ApiResponse(responseCode = "400", description = "Blank, missing or too long name")
    @ApiResponse(responseCode = "409", description = "A resource with that name already exists")
    ResourceResponse create(@Valid @RequestBody CreateResourceRequest request) {
        return ResourceResponse.from(createResource.execute(request.name()));
    }

    @GetMapping
    @Operation(summary = "List all resources")
    List<ResourceResponse> list() {
        return listResources.execute().stream().map(ResourceResponse::from).toList();
    }
}
