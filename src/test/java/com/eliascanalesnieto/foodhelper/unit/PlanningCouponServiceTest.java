package com.eliascanalesnieto.foodhelper.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eliascanalesnieto.foodhelper.application.NoRepeatedProductsPlanningCouponStrategy;
import com.eliascanalesnieto.foodhelper.application.SushiPlanningCouponStrategy;
import com.eliascanalesnieto.foodhelper.application.PlanningCouponService;
import com.eliascanalesnieto.foodhelper.application.ProposedWeekMenuService;
import com.eliascanalesnieto.foodhelper.domain.AppUser;
import com.eliascanalesnieto.foodhelper.domain.AppUserRepository;
import com.eliascanalesnieto.foodhelper.domain.CouponDefinition;
import com.eliascanalesnieto.foodhelper.domain.CouponDefinitionRepository;
import com.eliascanalesnieto.foodhelper.domain.PlanningCouponRedemption;
import com.eliascanalesnieto.foodhelper.domain.PlanningCouponRedemptionRepository;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenu;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDay;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuProduct;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuSection;
import com.eliascanalesnieto.foodhelper.domain.UserMoneyRepository;
import com.eliascanalesnieto.foodhelper.presentation.PlanningCouponAvailabilityState;
import com.eliascanalesnieto.foodhelper.presentation.PlanningCouponResponse;
import com.eliascanalesnieto.foodhelper.presentation.PlanningCouponUnavailabilityReason;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class PlanningCouponServiceTest {
    @Mock
    private ProposedWeekMenuService proposedWeekMenuService;

    @Mock
    private PlanningCouponRedemptionRepository redemptionRepository;

    @Mock
    private UserMoneyRepository userMoneyRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private CouponDefinitionRepository couponDefinitionRepository;

    @Mock
    private Clock clock;

    private PlanningCouponService service;

    @BeforeEach
    void setUp() {
        service = new PlanningCouponService(
                proposedWeekMenuService,
                redemptionRepository,
                userMoneyRepository,
                appUserRepository,
                List.of(new NoRepeatedProductsPlanningCouponStrategy()),
                couponDefinitionRepository,
                clock
        );
        when(appUserRepository.findById(1L)).thenReturn(AppUser.builder().id(1L).username("payer").build());
        when(couponDefinitionRepository.findAll()).thenReturn(List.of(coupon("NO_REPEATED_PRODUCTS", "NO_REPEATED_PRODUCTS")));
    }

    @Test
    void shouldDescribeANeverUsedCouponAsAvailable() {
        ProposedWeekMenu menu = validMenu();
        when(clock.instant()).thenReturn(Instant.parse("2026-06-15T10:00:00Z"));
        when(redemptionRepository.findLatestByUserIdAndCouponCode(1L, "NO_REPEATED_PRODUCTS")).thenReturn(Optional.empty());

        List<PlanningCouponResponse> coupons = service.findCoupons(menu, 1L);

        assertThat(coupons).singleElement().satisfies(coupon -> {
            assertThat(coupon.code()).isEqualTo("NO_REPEATED_PRODUCTS");
            assertThat(coupon.conditionDescription()).isNotBlank();
            assertThat(coupon.conditionMet()).isTrue();
            assertThat(coupon.available()).isTrue();
            assertThat(coupon.usedRecently()).isFalse();
            assertThat(coupon.informativeAvailabilityState()).isEqualTo(PlanningCouponAvailabilityState.AVAILABLE);
            assertThat(coupon.lastUsedAt()).isNull();
            assertThat(coupon.nextAvailableAt()).isNull();
            assertThat(coupon.unavailableReasons()).isEmpty();
        });
    }

    @Test
    void shouldMarkRecentlyUsedCouponWithItsNextAvailability() {
        ProposedWeekMenu menu = validMenu();
        Instant lastUsedAt = Instant.parse("2026-06-01T10:15:30Z");
        when(clock.instant()).thenReturn(Instant.parse("2026-06-15T10:00:00Z"));
        when(redemptionRepository.findLatestByUserIdAndCouponCode(1L, "NO_REPEATED_PRODUCTS"))
                .thenReturn(Optional.of(PlanningCouponRedemption.builder()
                        .id(7L)
                        .userId(1L)
                        .couponCode("NO_REPEATED_PRODUCTS")
                        .planningId(20L)
                        .currentWeekMenuId(30L)
                        .rewardAmount(new BigDecimal("15.00"))
                        .usedAt(lastUsedAt)
                        .build()));

        List<PlanningCouponResponse> coupons = service.findCoupons(menu, 1L);

        assertThat(coupons).singleElement().satisfies(coupon -> {
            assertThat(coupon.conditionMet()).isTrue();
            assertThat(coupon.available()).isFalse();
            assertThat(coupon.usedRecently()).isTrue();
            assertThat(coupon.informativeAvailabilityState()).isEqualTo(PlanningCouponAvailabilityState.USED_RECENTLY);
            assertThat(coupon.lastUsedAt()).isEqualTo(lastUsedAt);
            assertThat(coupon.nextAvailableAt()).isEqualTo(lastUsedAt.plusSeconds(30L * 24 * 60 * 60));
            assertThat(coupon.unavailableReasons()).containsExactly(PlanningCouponUnavailabilityReason.USED_WITHIN_PERIOD);
        });
    }

    @Test
    void shouldNotListCouponWhenThePlanningBreaksTheRule() {
        ProposedWeekMenu menu = invalidMenu();
        when(clock.instant()).thenReturn(Instant.parse("2026-06-15T10:00:00Z"));
        when(redemptionRepository.findLatestByUserIdAndCouponCode(1L, "NO_REPEATED_PRODUCTS")).thenReturn(Optional.empty());

        List<PlanningCouponResponse> coupons = service.findCoupons(menu, 1L);

        assertThat(coupons).isEmpty();
    }

    @Test
    void shouldNotListSushiCouponWhenThePlanningDoesNotIncludeProduct256() {
        ProposedWeekMenu menu = invalidMenu();
        PlanningCouponService sushiService = new PlanningCouponService(
                proposedWeekMenuService,
                redemptionRepository,
                userMoneyRepository,
                appUserRepository,
                List.of(new SushiPlanningCouponStrategy()),
                couponDefinitionRepository,
                clock
        );
        when(couponDefinitionRepository.findAll()).thenReturn(List.of(coupon("SUSHI", "SUSHI")));
        when(clock.instant()).thenReturn(Instant.parse("2026-06-15T10:00:00Z"));
        when(redemptionRepository.findLatestByUserIdAndCouponCode(1L, "SUSHI")).thenReturn(Optional.empty());

        List<PlanningCouponResponse> coupons = sushiService.findCoupons(menu, 1L);

        assertThat(coupons).isEmpty();
    }

    private CouponDefinition coupon(String code, String ruleCode) {
        return CouponDefinition.builder().id(1L).code(code).name(code).conditionDescription("Test rule").ruleCode(ruleCode).rewardAmount(new BigDecimal("15.00")).periodDays(30).build();
    }

    @Test
    void shouldRejectUnavailableCouponWhenValidatingAnInvalidPlanning() {
        ProposedWeekMenu menu = invalidMenu();
        when(clock.instant()).thenReturn(Instant.parse("2026-06-15T10:00:00Z"));
        when(redemptionRepository.findLatestByUserIdAndCouponCode(1L, "NO_REPEATED_PRODUCTS")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validateRequestedCoupons(menu, 1L, List.of("NO_REPEATED_PRODUCTS")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Coupon NO_REPEATED_PRODUCTS is not available");
    }

    @Test
    void shouldRejectManipulatedCouponCodesDuringValidation() {
        ProposedWeekMenu menu = validMenu();
        when(clock.instant()).thenReturn(Instant.parse("2026-06-15T10:00:00Z"));
        when(redemptionRepository.findLatestByUserIdAndCouponCode(1L, "NO_REPEATED_PRODUCTS")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validateRequestedCoupons(menu, 1L, List.of("NO_REPEATED_PRODUCTS", "HACKED")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown coupon code: HACKED");
    }

    @Test
    void shouldRevalidateOnEstablishmentIfTheCouponStateChangesBetweenValidationAndSave() {
        ProposedWeekMenu menu = validMenu();
        when(clock.instant()).thenReturn(Instant.parse("2026-06-15T10:00:00Z"));
        when(redemptionRepository.findLatestByUserIdAndCouponCode(1L, "NO_REPEATED_PRODUCTS")).thenReturn(Optional.empty());

        List<PlanningCouponResponse> validated = service.validateRequestedCoupons(
                menu,
                1L,
                List.of("NO_REPEATED_PRODUCTS")
        );

        assertThat(validated).singleElement().extracting(PlanningCouponResponse::available).isEqualTo(true);

        when(redemptionRepository.findLatestByUserIdAndCouponCode(1L, "NO_REPEATED_PRODUCTS"))
                .thenReturn(Optional.of(PlanningCouponRedemption.builder()
                        .id(8L)
                        .userId(1L)
                        .couponCode("NO_REPEATED_PRODUCTS")
                        .planningId(20L)
                        .currentWeekMenuId(31L)
                        .rewardAmount(new BigDecimal("15.00"))
                        .usedAt(Instant.parse("2026-06-15T09:59:30Z"))
                        .build()));

        assertThatThrownBy(() -> service.redeemCoupons(menu, 1L, 99L, List.of("NO_REPEATED_PRODUCTS")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Coupon NO_REPEATED_PRODUCTS is not available");
        verify(redemptionRepository, never()).save(any());
        verify(userMoneyRepository, never()).addMovement(
                org.mockito.ArgumentMatchers.anyLong(),
                any(BigDecimal.class),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyLong()
        );
    }

    private ProposedWeekMenu validMenu() {
        return ProposedWeekMenu.builder()
                .id(20L)
                .days(List.of(
                        ProposedWeekMenuDay.builder()
                                .date(LocalDate.of(2026, 6, 15))
                                .sections(List.of(
                                        ProposedWeekMenuSection.builder()
                                                .dayPartId(1L)
                                                .products(List.of(
                                                        ProposedWeekMenuProduct.builder()
                                                                .productId(10L)
                                                                .units(BigDecimal.ONE)
                                                                .grams(BigDecimal.ONE)
                                                                .sortOrder(10)
                                                                .build(),
                                                        ProposedWeekMenuProduct.builder()
                                                                .productId(11L)
                                                                .units(BigDecimal.ONE)
                                                                .grams(BigDecimal.ONE)
                                                                .sortOrder(11)
                                                                .build(),
                                                        ProposedWeekMenuProduct.builder()
                                                                .productId(12L)
                                                                .units(BigDecimal.ONE)
                                                                .grams(BigDecimal.ONE)
                                                                .sortOrder(12)
                                                                .build()
                                                ))
                                                .build()
                                ))
                                .build()
                ))
                .build();
    }

    private ProposedWeekMenu invalidMenu() {
        return ProposedWeekMenu.builder()
                .id(20L)
                .days(List.of(
                        ProposedWeekMenuDay.builder()
                                .date(LocalDate.of(2026, 6, 15))
                                .sections(List.of(
                                        ProposedWeekMenuSection.builder()
                                                .dayPartId(1L)
                                                .products(List.of(
                                                        ProposedWeekMenuProduct.builder()
                                                                .productId(10L)
                                                                .units(BigDecimal.ONE)
                                                                .grams(BigDecimal.ONE)
                                                                .sortOrder(10)
                                                                .build(),
                                                        ProposedWeekMenuProduct.builder()
                                                                .productId(10L)
                                                                .units(BigDecimal.ONE)
                                                                .grams(BigDecimal.ONE)
                                                                .sortOrder(11)
                                                                .build()
                                                ))
                                                .build()
                                ))
                                .build()
                ))
                .build();
    }
}
