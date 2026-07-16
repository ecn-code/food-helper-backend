package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(name = "CouponDefinitionRequest", description = "Payload for creating or updating a coupon definition")
public record CouponDefinitionRequest(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9_]{1,80}") @Schema(example = "WEEKLY_TREAT") String code,
        @NotBlank @Size(max = 150) @Schema(example = "Weekly treat") String name,
        @NotBlank @Schema(example = "No menu validation required") String conditionDescription,
        @NotBlank @Schema(description = "Coupon eligibility rule. ALWAYS creates a coupon without menu validation.", example = "ALWAYS") String ruleCode,
        @NotNull @DecimalMin("0.00") @Schema(example = "10.00") BigDecimal rewardAmount,
        @Min(0) @Schema(example = "30") int periodDays
) {}
