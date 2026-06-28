package com.eliascanalesnieto.foodhelper.presentation;

import com.eliascanalesnieto.foodhelper.domain.MoneyBoxType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(name = "MoneyBoxResponse", description = "User-owned or manually created money box")
public record MoneyBoxResponse(
        @Schema(description = "Money box identifier", example = "3") Long id,
        @Schema(description = "Money box origin", example = "MANUAL") MoneyBoxType type,
        @Schema(description = "Display name", example = "Household cash") String name,
        @Schema(description = "Owning user identifier; null for manual money boxes", example = "1") Long userId,
        @Schema(description = "Owning username; null for manual money boxes", example = "elias") String username,
        @Schema(description = "Current balance calculated from all signed movements", example = "87.50") BigDecimal balance,
        @Schema(description = "Movements ordered from newest to oldest") List<MoneyBoxMovementResponse> movements
) {
}
