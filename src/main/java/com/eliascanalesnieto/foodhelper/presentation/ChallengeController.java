package com.eliascanalesnieto.foodhelper.presentation;

import com.eliascanalesnieto.foodhelper.application.ChallengeService;
import com.eliascanalesnieto.foodhelper.domain.ChallengeDefinition;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/challenges")
@RequiredArgsConstructor
@Tag(name = "Challenges", description = "Browse and select self-validated challenges")
public class ChallengeController {
    private final ChallengeService challengeService;

    @GetMapping
    @Operation(summary = "List challenges", description = "Returns every configured challenge for the requested user. Challenges do not require menu validation; use onlyAvailable=true to hide challenges on cooldown.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Challenges returned", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ChallengeResponse.class)))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<ChallengeResponse> findChallenges(@RequestParam Long payerUserId, @RequestParam(defaultValue = "false") boolean onlyAvailable) {
        return challengeService.findChallenges(payerUserId, onlyAvailable);
    }

    @PostMapping("/{challengeCode}/redeem")
    @Operation(summary = "Select a challenge", description = "Immediately credits the challenge reward to the user's money box and starts its cooldown. No completion validation is performed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Challenge selected and reward credited", content = @Content(schema = @Schema(implementation = ChallengeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Unknown or unavailable challenge", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ChallengeResponse redeemChallenge(@PathVariable String challengeCode, @RequestParam Long payerUserId) {
        return challengeService.redeemChallenge(payerUserId, challengeCode);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create challenge definition")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Challenge created", content = @Content(schema = @Schema(implementation = ChallengeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Challenge code already exists", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ChallengeResponse create(@Valid @RequestBody ChallengeDefinitionRequest request) { return toResponse(challengeService.createDefinition(toDefinition(request))); }

    @PutMapping("/{challengeCode}")
    @Operation(summary = "Update challenge definition")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Challenge updated", content = @Content(schema = @Schema(implementation = ChallengeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Challenge not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ChallengeResponse update(@PathVariable String challengeCode, @Valid @RequestBody ChallengeDefinitionRequest request) { return toResponse(challengeService.updateDefinition(challengeCode, toDefinition(request))); }

    @DeleteMapping("/{challengeCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete challenge definition")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Challenge deleted"),
            @ApiResponse(responseCode = "404", description = "Challenge not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public void delete(@PathVariable String challengeCode) { challengeService.deleteDefinition(challengeCode); }

    private ChallengeDefinition toDefinition(ChallengeDefinitionRequest r) { return ChallengeDefinition.builder().code(r.code()).name(r.name()).description(r.description()).rewardAmount(r.rewardAmount()).periodDays(r.periodDays()).build(); }
    private ChallengeResponse toResponse(ChallengeDefinition c) { return new ChallengeResponse(c.getId(), c.getCode(), c.getName(), c.getDescription(), c.getRewardAmount(), c.getPeriodDays(), true, null, null); }
}
