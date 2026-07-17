package com.eliascanalesnieto.foodhelper.presentation;

import com.eliascanalesnieto.foodhelper.domain.NutritionalValues;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDay;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuProduct;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuRecipeProduction;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuSection;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuStockRequirement;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuStockSummary;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuStockSummaryCalories;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuStockSummaryDayCalories;
import java.util.List;
import org.springframework.stereotype.Component;

/** Maps the planning snapshot shared by proposed and established menus. */
@Component
public class WeekMenuSnapshotMapper {

    public List<ProposedWeekMenuDayResponse> toResponseDays(List<ProposedWeekMenuDay> days) {
        return safe(days).stream().map(this::toResponse).toList();
    }

    public ProposedWeekMenuDayResponse toResponse(ProposedWeekMenuDay day) {
        return new ProposedWeekMenuDayResponse(
                day.getId(),
                day.getDate(),
                safe(day.getSections()).stream().map(this::toResponse).toList(),
                safe(day.getRecipeProductions()).stream().map(this::toResponse).toList(),
                toResponse(day.getNutritionalValues())
        );
    }

    public ProposedWeekMenuSectionResponse toResponse(ProposedWeekMenuSection section) {
        return new ProposedWeekMenuSectionResponse(
                section.getId(),
                section.getDayPartId(),
                section.getName(),
                section.getDescription(),
                section.getSortOrder(),
                safe(section.getProducts()).stream().map(this::toResponse).toList(),
                toResponse(section.getNutritionalValues())
        );
    }

    public ProposedWeekMenuProductResponse toResponse(ProposedWeekMenuProduct product) {
        return new ProposedWeekMenuProductResponse(
                product.getProductId(),
                product.getProductName(),
                product.getProductId() == null ? null : product.getUnits(),
                product.getProductId() == null ? null : product.getGrams(),
                product.getSortOrder(),
                toResponse(product.getNutritionalValues())
        );
    }

    public ProposedWeekMenuRecipeProductionResponse toResponse(ProposedWeekMenuRecipeProduction production) {
        return new ProposedWeekMenuRecipeProductionResponse(
                production.getId(),
                production.getRecipeId(),
                production.getRecipeName(),
                production.getProductId(),
                production.getProductName(),
                production.getUnits(),
                production.getSortOrder()
        );
    }

    public NutritionalValuesResponse toResponse(NutritionalValues nutritionalValues) {
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

    public ProposedWeekMenuStockSummaryResponse toResponse(ProposedWeekMenuStockSummary stockSummary) {
        if (stockSummary == null) {
            return null;
        }
        return new ProposedWeekMenuStockSummaryResponse(
                stockSummary.getPlannedDays(),
                stockSummary.getDistinctProducts(),
                toResponse(stockSummary.getCalories()),
                stockSummary.getEstimatedCost(),
                safe(stockSummary.getRequirements()).stream().map(this::toResponse).toList()
        );
    }

    public ProposedWeekMenuStockSummaryCaloriesResponse toResponse(ProposedWeekMenuStockSummaryCalories calories) {
        if (calories == null) {
            return null;
        }
        return new ProposedWeekMenuStockSummaryCaloriesResponse(
                calories.getAveragePerPlannedDay(),
                toResponse(calories.getMaxDay()),
                toResponse(calories.getMinDay())
        );
    }

    public ProposedWeekMenuStockSummaryDayCaloriesResponse toResponse(ProposedWeekMenuStockSummaryDayCalories dayCalories) {
        if (dayCalories == null) {
            return null;
        }
        return new ProposedWeekMenuStockSummaryDayCaloriesResponse(dayCalories.getDate(), dayCalories.getCalories());
    }

    public ProposedWeekMenuStockRequirementResponse toResponse(ProposedWeekMenuStockRequirement requirement) {
        return new ProposedWeekMenuStockRequirementResponse(
                requirement.getProductId(),
                requirement.getProductName(),
                requirement.isStockInUnits(),
                requirement.getRequiredUnits(),
                requirement.getAvailableUnits(),
                requirement.getCoveredUnits(),
                requirement.getMissingUnits(),
                requirement.getEstimatedCost()
        );
    }

    private <T> List<T> safe(List<T> values) {
        return values == null ? List.of() : values;
    }
}
