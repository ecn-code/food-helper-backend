package com.eliascanalesnieto.foodhelper.presentation;

import com.eliascanalesnieto.foodhelper.application.PlanningCouponService;
import com.eliascanalesnieto.foodhelper.domain.CouponDefinition;
import com.eliascanalesnieto.foodhelper.presentation.error.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Coupons", description = "Browse the global coupon catalog and its availability for a payer user")
public class CouponController {
    private final PlanningCouponService planningCouponService;

    @GetMapping("/coupons")
    @Operation(
            summary = "List global coupon catalog",
            description = "Returns every configured coupon for the requested payer user. Use onlyAvailable=true to keep only coupons that can be redeemed right now."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Coupons returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CouponResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Payer user not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<CouponResponse> findCoupons(
            @RequestParam Long payerUserId,
            @RequestParam(defaultValue = "false") boolean onlyAvailable
    ) {
        return planningCouponService.findGlobalCoupons(payerUserId, onlyAvailable);
    }

    @PostMapping("/coupons")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create coupon definition", description = "Creates a coupon. Use ruleCode ALWAYS for a coupon without menu validation.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Coupon created", content = @Content(schema = @Schema(implementation = CouponResponse.class))), @ApiResponse(responseCode = "400", description = "Invalid request or rule", content = @Content(schema = @Schema(implementation = ApiError.class))), @ApiResponse(responseCode = "409", description = "Coupon code already exists", content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public CouponResponse create(@Valid @RequestBody CouponDefinitionRequest request) { return toResponse(planningCouponService.createDefinition(toDefinition(request))); }

    @PutMapping("/coupons/{code}")
    @Operation(summary = "Update coupon definition")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Coupon updated", content = @Content(schema = @Schema(implementation = CouponResponse.class))), @ApiResponse(responseCode = "400", description = "Invalid request or rule", content = @Content(schema = @Schema(implementation = ApiError.class))), @ApiResponse(responseCode = "404", description = "Coupon not found", content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public CouponResponse update(@PathVariable String code, @Valid @RequestBody CouponDefinitionRequest request) { return toResponse(planningCouponService.updateDefinition(code, toDefinition(request))); }

    @DeleteMapping("/coupons/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete coupon definition")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Coupon deleted"), @ApiResponse(responseCode = "404", description = "Coupon not found", content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public void delete(@PathVariable String code) { planningCouponService.deleteDefinition(code); }

    private CouponDefinition toDefinition(CouponDefinitionRequest request) { return CouponDefinition.builder().code(request.code()).name(request.name()).conditionDescription(request.conditionDescription()).ruleCode(request.ruleCode()).rewardAmount(request.rewardAmount()).periodDays(request.periodDays()).build(); }
    private CouponResponse toResponse(CouponDefinition c) { return new CouponResponse(c.getId(), c.getCode(), c.getName(), c.getConditionDescription(), c.getRuleCode(), c.getRewardAmount(), c.getPeriodDays(), true, List.of()); }
}
