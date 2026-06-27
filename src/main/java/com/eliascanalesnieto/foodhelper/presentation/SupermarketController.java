package com.eliascanalesnieto.foodhelper.presentation;

import com.eliascanalesnieto.foodhelper.application.SupermarketService;
import com.eliascanalesnieto.foodhelper.domain.Supermarket;
import com.eliascanalesnieto.foodhelper.presentation.error.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/supermarkets")
@RequiredArgsConstructor
@Tag(name = "Supermarkets", description = "Manage the supermarket catalog used by products and shopping lists")
public class SupermarketController {
    private final SupermarketService service;

    @GetMapping
    @Operation(summary = "List supermarkets", description = "Returns all supermarkets ordered by name.")
    @ApiResponse(responseCode = "200", description = "Supermarkets returned",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = SupermarketResponse.class))))
    public List<SupermarketResponse> findAll() {
        return service.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supermarket")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Supermarket returned", content = @Content(schema = @Schema(implementation = SupermarketResponse.class))),
            @ApiResponse(responseCode = "404", description = "Supermarket not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public SupermarketResponse findById(@PathVariable Long id) {
        return toResponse(service.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create supermarket")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Supermarket created", content = @Content(schema = @Schema(implementation = SupermarketResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Supermarket name already exists", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public SupermarketResponse create(@Valid @RequestBody SupermarketRequest request) {
        return toResponse(service.create(request.name()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update supermarket")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Supermarket updated", content = @Content(schema = @Schema(implementation = SupermarketResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Supermarket not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Supermarket name already exists", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public SupermarketResponse update(@PathVariable Long id, @Valid @RequestBody SupermarketRequest request) {
        return toResponse(service.update(id, request.name()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete supermarket", description = "Deletes an unassigned supermarket. Assigned supermarkets return a conflict response.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Supermarket deleted"),
            @ApiResponse(responseCode = "404", description = "Supermarket not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Supermarket is assigned to products", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    private SupermarketResponse toResponse(Supermarket supermarket) {
        return new SupermarketResponse(supermarket.getId(), supermarket.getName());
    }
}
