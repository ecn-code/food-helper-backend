package com.eliascanalesnieto.foodhelper.presentation;

import com.eliascanalesnieto.foodhelper.application.NutritionalRulesService;
import com.eliascanalesnieto.foodhelper.domain.NutritionalValues;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenu;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDay;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuProduct;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuRecipeProduction;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuSection;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuStockRequirement;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuStockSummary;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuStockSummaryCalories;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuStockSummaryDayCalories;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProposedWeekMenuApiMapper {
    private final NutritionalRulesService nutritionalRulesService;

    public ProposedWeekMenuResponse toResponse(ProposedWeekMenu menu) {
        java.util.List<ProposedWeekMenuDay> days = menu.getDays() == null ? java.util.List.of() : menu.getDays();
        return new ProposedWeekMenuResponse(
                menu.getId(),
                menu.getStartDate(),
                menu.getEndDate(),
                days.stream().map(this::toResponse).toList(),
                toResponse(menu.getNutritionalValues()),
                toResponse(menu.getStockSummary()),
                nutritionalRulesService.evaluate(menu.getNutritionalValues(), days.size())
        );
    }

    public ProposedWeekMenuDay toDomain(UpsertProposedWeekMenuDayRequest request) {
        return ProposedWeekMenuDay.builder()
                .date(request.date())
                .sections((request.sections() == null ? java.util.List.<ProposedWeekMenuSectionRequest>of() : request.sections()).stream()
                        .map(this::toDomain)
                        .toList())
                .recipeProductions((request.recipeProductions() == null ? java.util.List.<ProposedWeekMenuRecipeProductionRequest>of() : request.recipeProductions()).stream()
                        .map(this::toDomain)
                        .toList())
                .build();
    }

    private ProposedWeekMenuSection toDomain(ProposedWeekMenuSectionRequest request) {
        return ProposedWeekMenuSection.builder()
                .dayPartId(request.dayPartId())
                .products(request.products().stream()
                        .map(this::toDomain)
                        .toList())
                .build();
    }

    private ProposedWeekMenuProduct toDomain(ProposedWeekMenuProductRequest request) {
        return ProposedWeekMenuProduct.builder()
                .productId(request.productId())
                .productName(request.productName())
                .units(request.units())
                .grams(request.grams())
                .sortOrder(request.sortOrder())
                .nutritionalValues(request.calories() == null
                        && request.carbohydrates() == null
                        && request.proteins() == null
                        && request.fats() == null
                        ? null
                        : NutritionalValues.builder()
                                .calories(request.calories())
                                .carbohydrates(request.carbohydrates())
                                .proteins(request.proteins())
                                .fats(request.fats())
                                .build())
                .build();
    }

    private ProposedWeekMenuRecipeProduction toDomain(ProposedWeekMenuRecipeProductionRequest request) {
        return ProposedWeekMenuRecipeProduction.builder()
                .recipeId(request.recipeId())
                .producedGrams(request.producedGrams())
                .sortOrder(request.sortOrder())
                .build();
    }

    private ProposedWeekMenuDayResponse toResponse(ProposedWeekMenuDay day) {
        return new ProposedWeekMenuDayResponse(
                day.getId(),
                day.getDate(),
                (day.getSections() == null ? java.util.List.<ProposedWeekMenuSection>of() : day.getSections()).stream().map(this::toResponse).toList(),
                (day.getRecipeProductions() == null ? java.util.List.<ProposedWeekMenuRecipeProduction>of() : day.getRecipeProductions()).stream().map(this::toResponse).toList(),
                toResponse(day.getNutritionalValues())
        );
    }

    private ProposedWeekMenuSectionResponse toResponse(ProposedWeekMenuSection section) {
        return new ProposedWeekMenuSectionResponse(
                section.getId(),
                section.getDayPartId(),
                section.getName(),
                section.getDescription(),
                section.getSortOrder(),
                section.getProducts().stream().map(this::toResponse).toList(),
                toResponse(section.getNutritionalValues())
        );
    }

    private ProposedWeekMenuProductResponse toResponse(ProposedWeekMenuProduct product) {
        return new ProposedWeekMenuProductResponse(
                product.getProductId(),
                product.getProductName(),
                product.getUnits(),
                product.getGrams(),
                product.getSortOrder(),
                toResponse(product.getNutritionalValues())
        );
    }

    private ProposedWeekMenuRecipeProductionResponse toResponse(ProposedWeekMenuRecipeProduction production) {
        return new ProposedWeekMenuRecipeProductionResponse(
                production.getId(),
                production.getRecipeId(),
                production.getRecipeName(),
                production.getProductId(),
                production.getProductName(),
                production.getProducedGrams(),
                production.getProducedUnits(),
                production.getSortOrder()
        );
    }

    private NutritionalValuesResponse toResponse(NutritionalValues nutritionalValues) {
        if (nutritionalValues == null) {
            return null;
        }
        return new NutritionalValuesResponse(
                nutritionalValues.getCalories(),
                nutritionalValues.getCarbohydrates(),
                nutritionalValues.getProteins(),
                nutritionalValues.getFats()
        );
    }

    private ProposedWeekMenuStockSummaryResponse toResponse(ProposedWeekMenuStockSummary stockSummary) {
        if (stockSummary == null) {
            return null;
        }
        return new ProposedWeekMenuStockSummaryResponse(
                stockSummary.getPlannedDays(),
                stockSummary.getDistinctProducts(),
                toResponse(stockSummary.getCalories()),
                stockSummary.getEstimatedCost(),
                stockSummary.getRequirements().stream().map(this::toResponse).toList()
        );
    }

    private ProposedWeekMenuStockSummaryCaloriesResponse toResponse(ProposedWeekMenuStockSummaryCalories calories) {
        return new ProposedWeekMenuStockSummaryCaloriesResponse(
                calories.getAveragePerPlannedDay(),
                toResponse(calories.getMaxDay()),
                toResponse(calories.getMinDay())
        );
    }

    private ProposedWeekMenuStockSummaryDayCaloriesResponse toResponse(ProposedWeekMenuStockSummaryDayCalories dayCalories) {
        if (dayCalories == null) {
            return null;
        }
        return new ProposedWeekMenuStockSummaryDayCaloriesResponse(
                dayCalories.getDate(),
                dayCalories.getCalories()
        );
    }

    private ProposedWeekMenuStockRequirementResponse toResponse(ProposedWeekMenuStockRequirement requirement) {
        return new ProposedWeekMenuStockRequirementResponse(
                requirement.getProductId(),
                requirement.getProductName(),
                requirement.getRequiredUnits(),
                requirement.getAvailableUnits(),
                requirement.getCoveredUnits(),
                requirement.getMissingUnits(),
                requirement.getEstimatedCost()
        );
    }
}
