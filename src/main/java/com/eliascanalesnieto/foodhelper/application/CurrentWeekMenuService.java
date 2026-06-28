package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.AppUser;
import com.eliascanalesnieto.foodhelper.domain.AppUserRepository;
import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenu;
import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuRepository;
import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuStatsRepository;
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
import com.eliascanalesnieto.foodhelper.domain.SupermarketRepository;
import com.eliascanalesnieto.foodhelper.domain.UserMoneyRepository;
import com.eliascanalesnieto.foodhelper.domain.UserMenuHistoryRepository;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuApiMapper;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuShoppingListItemResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuStatsResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuUsedStockResponse;
import com.eliascanalesnieto.foodhelper.presentation.MenuStockAllocationRequest;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuDayResponse;
import com.eliascanalesnieto.foodhelper.presentation.NutritionalValuesResponse;
import com.eliascanalesnieto.foodhelper.presentation.UserMenuHistoryEntryResponse;
import com.eliascanalesnieto.foodhelper.presentation.UserMenuHistoryResponse;
import com.eliascanalesnieto.foodhelper.presentation.UserResponse;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final CurrentWeekMenuStatsRepository currentWeekMenuStatsRepository;
    private final CurrentWeekMenuStatsService currentWeekMenuStatsService;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final SupermarketRepository supermarketRepository;
    private final AppUserRepository appUserRepository;
    private final UserMoneyRepository userMoneyRepository;
    private final UserMenuHistoryRepository userMenuHistoryRepository;
    private final CurrentWeekMenuApiMapper mapper;
    private final NutritionalRulesService nutritionalRulesService;
    private final Clock clock;

    @Transactional
    public CurrentWeekMenuResponse establishFromProposed(Long proposedWeekMenuId, Long payerUserId) {
        return establishFromProposed(proposedWeekMenuId, payerUserId, null);
    }

    @Transactional
    public CurrentWeekMenuResponse establishFromProposed(
            Long proposedWeekMenuId,
            Long payerUserId,
            List<MenuStockAllocationRequest> stockAllocations
    ) {
        try {
            return applyCurrentRules(currentWeekMenuRepository.findByProposedWeekMenuId(proposedWeekMenuId));
        } catch (ResourceNotFoundException ex) {
            // Fall through and create the established week when no snapshot exists yet.
        }

        AppUser payer = appUserRepository.findById(payerUserId);
        ProposedWeekMenu proposedMenu = proposedWeekMenuService.findById(proposedWeekMenuId);
        AllocationResult allocation = allocateStock(proposedMenu, stockAllocations);
        CurrentWeekMenu currentWeekMenu = CurrentWeekMenu.builder()
                .proposedWeekMenuId(proposedWeekMenuId)
                .payerUserId(payer.getId())
                .payerUsername(payer.getUsername())
                .startDate(proposedMenu.getStartDate())
                .endDate(proposedMenu.getEndDate())
                .days(proposedMenu.getDays())
                .nutritionalValues(proposedMenu.getNutritionalValues())
                .stockSummary(proposedMenu.getStockSummary())
                .usedStock(allocation.usedStock())
                .shoppingList(allocation.shoppingList())
                .build();
        CurrentWeekMenuResponse created = currentWeekMenuRepository.create(mapper.toResponse(currentWeekMenu));
        userMoneyRepository.addMovement(
                payer.getId(),
                allocationCost(allocation.usedStock()).negate(),
                "Menu #" + created.id(),
                created.id()
        );
        return created;
    }

    @Transactional(readOnly = true)
    public List<CurrentWeekMenuResponse> findAll() {
        return currentWeekMenuRepository.findAll().stream()
                .map(this::applyCurrentRules)
                .toList();
    }

    @Transactional(readOnly = true)
    public CurrentWeekMenuResponse findById(Long id) {
        return applyCurrentRules(currentWeekMenuRepository.findById(id));
    }

    @Transactional
    public void undo(Long id) {
        CurrentWeekMenuResponse menu = currentWeekMenuRepository.findById(id);
        ensureMenuIsOpen(id);
        menu.usedStock().forEach(usedStock -> stockRepository.restore(toDomain(usedStock)));
        userMoneyRepository.deleteMovementsByCurrentWeekMenuId(id);
        currentWeekMenuRepository.delete(id);
    }

    @Transactional(readOnly = true)
    public List<CurrentWeekMenuShoppingListItemResponse> findShoppingList(
            Long id,
            Long supermarketId
    ) {
        CurrentWeekMenuResponse menu = currentWeekMenuRepository.findById(id);
        if (supermarketId == null) {
            return menu.shoppingList();
        }
        supermarketRepository.findById(supermarketId);
        Set<Long> availableProductIds = Set.copyOf(productRepository.findProductIdsBySupermarket(
                supermarketId,
                menu.shoppingList().stream()
                        .map(CurrentWeekMenuShoppingListItemResponse::productId)
                        .toList()
        ));
        return menu.shoppingList().stream()
                .filter(item -> availableProductIds.contains(item.productId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public CurrentWeekMenuStatsResponse findStatsById(Long id) {
        return currentWeekMenuStatsRepository.findByCurrentWeekMenuId(id);
    }

    @Transactional
    public CurrentWeekMenuStatsResponse close(Long id, List<Long> personIds) {
        try {
            return currentWeekMenuStatsRepository.findByCurrentWeekMenuId(id);
        } catch (ResourceNotFoundException ex) {
            // Keep closing flow idempotent only for already closed weeks.
        }

        CurrentWeekMenuResponse closedWeek = currentWeekMenuRepository.findById(id);
        ensureMenuCanBeClosed(closedWeek);
        List<AppUser> people = loadPeople(personIds);
        List<CurrentWeekMenuResponse> closedWeeksInMonth = new ArrayList<>(currentWeekMenuStatsRepository.findClosedWeekMenusByMonth(YearMonth.from(closedWeek.endDate())));
        closedWeeksInMonth.add(closedWeek);
        CurrentWeekMenuStatsResponse stats = currentWeekMenuStatsService.build(closedWeek, closedWeeksInMonth);
        CurrentWeekMenuStatsResponse saved = currentWeekMenuStatsRepository.save(stats);
        people.forEach(person -> userMenuHistoryRepository.save(id, person, closedWeek));
        return saved;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findPeople() {
        return appUserRepository.findAll().stream()
                .map(person -> new UserResponse(person.getId(), person.getUsername()))
                .toList();
    }

    @Transactional(readOnly = true)
    public UserMenuHistoryResponse findMonthlyHistory(Long personId, int year, int month) {
        YearMonth period;
        try {
            period = YearMonth.of(year, month);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Invalid history month");
        }
        return findHistory(personId, year, month, period.atDay(1), period.atEndOfMonth());
    }

    @Transactional(readOnly = true)
    public UserMenuHistoryResponse findAnnualHistory(Long personId, int year) {
        LocalDate from;
        try {
            from = LocalDate.of(year, 1, 1);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Invalid history year");
        }
        return findHistory(personId, year, null, from, from.withMonth(12).withDayOfMonth(31));
    }

    private UserMenuHistoryResponse findHistory(
            Long personId, Integer year, Integer month, LocalDate from, LocalDate to
    ) {
        AppUser person = appUserRepository.findById(personId);
        List<CurrentWeekMenuResponse> menus = userMenuHistoryRepository.findMenus(personId, from, to);
        return new UserMenuHistoryResponse(
                person.getId(), person.getUsername(), year, month,
                currentWeekMenuStatsService.summarize(menus),
                menus.stream().map(menu -> new UserMenuHistoryEntryResponse(
                        menu.id(), menu.startDate(), menu.endDate(),
                        currentWeekMenuStatsService.summarize(List.of(menu))
                )).toList()
        );
    }

    private List<AppUser> loadPeople(List<Long> personIds) {
        if (personIds == null || personIds.isEmpty()) {
            throw new IllegalArgumentException("At least one person must be selected");
        }
        List<Long> uniqueIds = personIds.stream().distinct().toList();
        if (uniqueIds.size() != personIds.size() || uniqueIds.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("personIds must contain distinct valid identifiers");
        }
        return uniqueIds.stream().map(appUserRepository::findById).toList();
    }

    private AllocationResult allocateStock(
            ProposedWeekMenu menu,
            List<MenuStockAllocationRequest> requestedAllocations
    ) {
        Map<Long, Product> productsById = loadProducts(menu.getDays());
        Map<Long, BigDecimal> requiredUnitsByProduct = requiredUnitsByProduct(menu.getDays());
        List<StockEntry> availableStock = stockRepository.findStock(null, requiredUnitsByProduct.keySet());
        if (requestedAllocations != null) {
            return allocateRequestedStock(
                    productsById,
                    requiredUnitsByProduct,
                    availableStock,
                    requestedAllocations
            );
        }
        Map<Long, List<StockEntry>> stockByProduct = availableStock.stream()
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

    private AllocationResult allocateRequestedStock(
            Map<Long, Product> productsById,
            Map<Long, BigDecimal> requiredUnitsByProduct,
            List<StockEntry> availableStock,
            List<MenuStockAllocationRequest> requestedAllocations
    ) {
        Map<Long, StockEntry> stockById = availableStock.stream()
                .collect(Collectors.toMap(StockEntry::getId, stock -> stock));
        Set<Long> seenStockIds = new java.util.HashSet<>();
        Map<Long, BigDecimal> allocatedByProduct = new HashMap<>();
        List<CurrentWeekMenuUsedStock> usedStock = new ArrayList<>();

        for (MenuStockAllocationRequest requested : requestedAllocations) {
            if (requested == null || requested.stockEntryId() == null || requested.usedUnits() == null
                    || requested.usedUnits().signum() <= 0) {
                throw new IllegalArgumentException("Stock allocations require a stockEntryId and positive usedUnits");
            }
            if (!seenStockIds.add(requested.stockEntryId())) {
                throw new IllegalArgumentException("Each stock entry can appear only once in stockAllocations");
            }
            StockEntry stockEntry = stockById.get(requested.stockEntryId());
            if (stockEntry == null) {
                throw new ResourceNotFoundException("Stock entry not found for a product required by the menu");
            }
            BigDecimal usedUnits = scale(requested.usedUnits());
            if (usedUnits.compareTo(scale(stockEntry.getQuantity())) > 0) {
                throw new IllegalArgumentException("Allocated quantity exceeds current stock");
            }
            BigDecimal allocated = allocatedByProduct.merge(
                    stockEntry.getProductId(),
                    usedUnits,
                    BigDecimal::add
            );
            if (allocated.compareTo(scale(requiredUnitsByProduct.get(stockEntry.getProductId()))) > 0) {
                throw new IllegalArgumentException("Allocated quantity exceeds the quantity required by the menu");
            }
            stockRepository.removeQuantity(stockEntry.getId(), usedUnits);
            Product product = productsById.get(stockEntry.getProductId());
            usedStock.add(CurrentWeekMenuUsedStock.builder()
                    .stockEntryId(stockEntry.getId())
                    .productId(stockEntry.getProductId())
                    .productName(product.getName())
                    .usedUnits(usedUnits)
                    .price(scale(stockEntry.getPrice()))
                    .totalCost(scale(usedUnits.multiply(stockEntry.getPrice())))
                    .expirationDate(stockEntry.getExpirationDate())
                    .entryDate(stockEntry.getEntryDate())
                    .build());
        }

        List<CurrentWeekMenuShoppingListItem> shoppingList = requiredUnitsByProduct.entrySet().stream()
                .map(requirement -> {
                    BigDecimal missingUnits = scale(requirement.getValue()).subtract(
                            allocatedByProduct.getOrDefault(requirement.getKey(), ZERO)
                    );
                    if (missingUnits.signum() <= 0) {
                        return null;
                    }
                    return CurrentWeekMenuShoppingListItem.builder()
                            .productId(requirement.getKey())
                            .productName(productsById.get(requirement.getKey()).getName())
                            .missingUnits(scale(missingUnits))
                            .build();
                })
                .filter(java.util.Objects::nonNull)
                .toList();
        return new AllocationResult(usedStock, shoppingList);
    }

    private BigDecimal allocationCost(List<CurrentWeekMenuUsedStock> usedStock) {
        return scale(usedStock.stream()
                .map(CurrentWeekMenuUsedStock::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private void ensureMenuIsOpen(Long id) {
        try {
            currentWeekMenuStatsRepository.findByCurrentWeekMenuId(id);
            throw new IllegalArgumentException("A closed menu cannot be undone");
        } catch (ResourceNotFoundException ex) {
            // No saved stats means that the menu is still open and can be undone.
        }
    }

    private CurrentWeekMenuUsedStock toDomain(CurrentWeekMenuUsedStockResponse usedStock) {
        return CurrentWeekMenuUsedStock.builder()
                .stockEntryId(usedStock.stockEntryId())
                .productId(usedStock.productId())
                .productName(usedStock.productName())
                .usedUnits(usedStock.usedUnits())
                .price(usedStock.price())
                .totalCost(usedStock.totalCost())
                .expirationDate(usedStock.expirationDate())
                .entryDate(usedStock.entryDate())
                .build();
    }

    private void ensureMenuCanBeClosed(CurrentWeekMenuResponse closedWeek) {
        LocalDate today = LocalDate.now(clock);
        if (!today.isAfter(closedWeek.endDate())) {
            throw new IllegalArgumentException("Menu can only be closed after its end date");
        }
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

    private CurrentWeekMenuResponse applyCurrentRules(CurrentWeekMenuResponse menu) {
        List<ProposedWeekMenuDayResponse> days = menu.days() == null ? List.of() : menu.days();
        NutritionalValuesResponse totals = menu.nutritionalValues() == null
                ? new NutritionalValuesResponse(ZERO, ZERO, ZERO, ZERO)
                : menu.nutritionalValues();
        return new CurrentWeekMenuResponse(
                menu.id(), menu.planningId(), menu.payerUserId(), menu.payerUsername(),
                menu.startDate(), menu.endDate(), days, totals,
                menu.stockSummary(),
                menu.usedStock() == null ? List.of() : menu.usedStock(),
                menu.shoppingList() == null ? List.of() : menu.shoppingList(),
                nutritionalRulesService.evaluate(totals, days.size())
        );
    }

    private record AllocationResult(
            List<CurrentWeekMenuUsedStock> usedStock,
            List<CurrentWeekMenuShoppingListItem> shoppingList
    ) {
    }
}
