package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(name = "UpdateCurrentWeekMenuStockRequest", description = "Payload for replacing the temporary stock of the established week")
public record UpdateCurrentWeekMenuStockRequest(
        @ArraySchema(schema = @Schema(implementation = CurrentWeekMenuStockItemRequest.class))
        @NotNull @Valid List<CurrentWeekMenuStockItemRequest> weekStock
) {
}
