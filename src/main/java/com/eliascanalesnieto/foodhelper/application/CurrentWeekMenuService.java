package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenu;
import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuRepository;
import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuShoppingListItem;
import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuUsedStock;
import com.eliascanalesnieto.foodhelper.domain.NutritionalValues;
import com.eliascanalesnieto.foodhelper.domain.Product;
import com.eliascanalesnieto.foodhelper.domain.ProductRepository;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenu;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDay;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuProduct;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuSection;
import com.eliascanalesnieto.foodhelper.domain.StockEntry;
import com.eliascanalesnieto.foodhelper.domain.StockRepository;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuApiMapper;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuResponse;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentWeekMenuService {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final int SCALE = 2;

    private final ProposedWeekMenuService proposedWeekMenuService;
    private final CurrentWeekMenuRepository currentWeekMenuRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final CurrentWeekMenuApiMapper mapper;

    @Transactional
    public CurrentWeekMenuResponse publishFromProposed(Long proposedWeekMenuId) {
        try {
            return currentWeekMenuRepository.findByProposedWeekMenuId(proposedWeekMenuId);
        } catch (ResourceNotFoundException ex) {
            // Fall through and create the established week when no snapshot exists yet.
        }

        ProposedWeekMenu proposedMenu = proposedWeekMenuService.findById(proposedWeekMenuId);
        AllocationResult allocation = allocateStock(proposedMenu);
        CurrentWeekMenu currentWeekMenu = CurrentWeekMenu.builder()
                .proposedWeekMenuId(proposedWeekMenuId)
                .startDate(proposedMenu.getStartDate())
                .endDate(proposedMenu.getEndDate())
                .days(proposedMenu.getDays())
                .nutritionalValues(proposedMenu.getNutritionalValues())
                .stockSummary(proposedMenu.getStockSummary())
                .usedStock(allocation.usedStock())
                .shoppingList(allocation.shoppingList())
                .build();
        return currentWeekMenuRepository.create(mapper.toResponse(currentWeekMenu));
    }

    @Transactional(readOnly = true)
    public CurrentWeekMenuResponse findById(Long id) {
        return currentWeekMenuRepository.findById(id);
    }

    private AllocationResult allocateStock(ProposedWeekMenu menu) {
        Map<Long, Product> productsById = loadProducts(menu.getDays());
        Map<Long, BigDecimal> requiredUnitsByProduct = requiredUnitsByProduct(menu.getDays());
        Map<Long, List<StockEntry>> stockByProduct = stockRepository.findStock(null, requiredUnitsByProduct.keySet()).stream()
                .collect(Collectors.groupingBy(
                        StockEntry::getProductId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<CurrentWeekMenuUsedStock> usedStock = new ArrayList<>();
        List<CurrentWeekMenuShoppingListItem> shoppingList = new ArrayList<>();

        for (Map.Entry<Long, BigDecimal> requirement : requiredUnitsByProduct.entrySet()) {
            Long productId = requirement.getKey();
            Product product = productsById.get(productId);
            BigDecimal remainingRequiredUnits = scale(requirement.getValue());
            for (StockEntry stockEntry : stockByProduct.getOrDefault(productId, List.of())) {
                if (remainingRequiredUnits.signum() <= 0) {
                    break;
                }
                BigDecimal availableUnits = scale(stockEntry.getQuantity());
                BigDecimal unitsToConsume = availableUnits.min(remainingRequiredUnits);
                if (unitsToConsume.signum() <= 0) {
                    continue;
                }
                stockRepository.removeQuantity(stockEntry.getId(), unitsToConsume);
                usedStock.add(CurrentWeekMenuUsedStock.builder()
                        .stockEntryId(stockEntry.getId())
                        .productId(productId)
                        .productName(product.getName())
                        .usedUnits(scale(unitsToConsume))
                        .price(scale(stockEntry.getPrice()))
                        .totalCost(scale(unitsToConsume.multiply(stockEntry.getPrice())))
                        .expirationDate(stockEntry.getExpirationDate())
                        .entryDate(stockEntry.getEntryDate())
                        .build());
                remainingRequiredUnits = remainingRequiredUnits.subtract(unitsToConsume);
            }
            if (remainingRequiredUnits.signum() > 0) {
                shoppingList.add(CurrentWeekMenuShoppingListItem.builder()
                        .productId(productId)
                        .productName(product.getName())
                        .missingUnits(scale(remainingRequiredUnits))
                        .build());
            }
        }

        return new AllocationResult(usedStock, shoppingList);
    }

    private Map<Long, Product> loadProducts(List<ProposedWeekMenuDay> days) {
        Collection<Long> productIds = days.stream()
                .flatMap(day -> day.getSections().stream())
                .flatMap(section -> section.getProducts().stream())
                .map(ProposedWeekMenuProduct::getProductId)
                .distinct()
                .toList();
        List<Product> products = productRepository.findByIds(productIds);
        Map<Long, Product> productsById = new HashMap<>();
        products.forEach(product -> productsById.put(product.getId(), product));
        return productsById;
    }

    private Map<Long, BigDecimal> requiredUnitsByProduct(List<ProposedWeekMenuDay> days) {
        Map<Long, BigDecimal> requiredUnits = new LinkedHashMap<>();
        for (ProposedWeekMenuDay day : days) {
            for (ProposedWeekMenuSection section : day.getSections()) {
                for (ProposedWeekMenuProduct product : section.getProducts()) {
                    requiredUnits.merge(product.getProductId(), normalizeRequiredUnits(product), BigDecimal::add);
                }
            }
        }
        return requiredUnits;
    }

    private BigDecimal normalizeRequiredUnits(ProposedWeekMenuProduct product) {
        if (product.getUnits() != null) {
            return scale(product.getUnits());
        }
        if (product.getGrams() != null) {
            Product linkedProduct = productRepository.findById(product.getProductId());
            return scale(product.getGrams().divide(linkedProduct.getGramsPerUnit(), SCALE, RoundingMode.HALF_UP));
        }
        return BigDecimal.ONE.setScale(SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    private record AllocationResult(
            List<CurrentWeekMenuUsedStock> usedStock,
            List<CurrentWeekMenuShoppingListItem> shoppingList
    ) {
    }
}
