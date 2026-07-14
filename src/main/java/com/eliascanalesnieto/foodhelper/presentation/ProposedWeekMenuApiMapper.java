package com.eliascanalesnieto.foodhelper.presentation;

import com.eliascanalesnieto.foodhelper.application.NutritionalRulesService;
import com.eliascanalesnieto.foodhelper.domain.NutritionalValues;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenu;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDay;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuProduct;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuRecipeProduction;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuSection;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProposedWeekMenuApiMapper {
    private final NutritionalRulesService nutritionalRulesService;
    private final WeekMenuSnapshotMapper snapshotMapper;

    public ProposedWeekMenuResponse toResponse(ProposedWeekMenu menu) {
        java.util.List<ProposedWeekMenuDay> days = menu.getDays() == null ? java.util.List.of() : menu.getDays();
        return new ProposedWeekMenuResponse(
                menu.getId(),
                menu.getUsers(),
                menu.getStartDate(),
                menu.getEndDate(),
                snapshotMapper.toResponseDays(days),
                snapshotMapper.toResponse(menu.getNutritionalValues()),
                snapshotMapper.toResponse(menu.getStockSummary()),
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
                .units(request.units())
                .sortOrder(request.sortOrder())
                .build();
    }

}
