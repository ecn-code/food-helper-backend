package com.eliascanalesnieto.foodhelper.presentation;

import com.eliascanalesnieto.foodhelper.application.PlanningCouponService;
import com.eliascanalesnieto.foodhelper.presentation.error.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
