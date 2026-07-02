package com.eliascanalesnieto.foodhelper.infra;

import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenu;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDay;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDayPart;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDayPartRepository;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuProduct;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuRecipeProduction;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuRepository;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuSection;
import com.eliascanalesnieto.foodhelper.domain.NutritionalValues;
import com.eliascanalesnieto.foodhelper.domain.PlanningState;
import com.eliascanalesnieto.foodhelper.domain.PlanningSummary;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class JdbcProposedWeekMenuRepository implements ProposedWeekMenuRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ProposedWeekMenuDayPartRepository dayPartRepository;

    @Override
    @Transactional
    public ProposedWeekMenu create(ProposedWeekMenu menu) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO proposed_week_menus (start_date, end_date, users)
                    VALUES (?, ?, ?)
                    """, new String[]{"id"});
            ps.setDate(1, Date.valueOf(menu.getStartDate()));
            ps.setDate(2, Date.valueOf(menu.getEndDate()));
            ps.setInt(3, menu.getUsers() == null ? 1 : menu.getUsers());
            return ps;
        }, keyHolder);
        return menu.toBuilder()
                .id(keyHolder.getKey().longValue())
                .days(List.of())
                .build();
    }

    @Override
    public ProposedWeekMenu findById(Long id) {
        ProposedWeekMenu menu = jdbcTemplate.query("""
                        SELECT id, start_date, end_date, users
                        FROM proposed_week_menus
                        WHERE id = ?
                        """,
                menuRowMapper(),
                id
        ).stream().findFirst().orElseThrow(() -> new ResourceNotFoundException("Planning not found"));
        return menu.toBuilder()
                .days(findDays(id))
                .build();
    }

    @Override
    public List<PlanningSummary> findAllSummaries() {
        return jdbcTemplate.query("""
                        SELECT pwm.id,
                               pwm.start_date,
                               pwm.end_date,
                               COUNT(DISTINCT pwmd.id) AS planned_days,
                               cwm.id AS menu_id,
                               CASE
                                   WHEN cwms.current_week_menu_id IS NOT NULL THEN 'CLOSED'
                                   WHEN cwm.id IS NOT NULL THEN 'ESTABLISHED'
                                   ELSE 'DRAFT'
                               END AS state
                        FROM proposed_week_menus pwm
                        LEFT JOIN proposed_week_menu_days pwmd ON pwmd.menu_id = pwm.id
                        LEFT JOIN current_week_menus cwm ON cwm.proposed_week_menu_id = pwm.id
                        LEFT JOIN current_week_menu_stats cwms ON cwms.current_week_menu_id = cwm.id
                        GROUP BY pwm.id, pwm.start_date, pwm.end_date, cwm.id, cwms.current_week_menu_id
                        ORDER BY pwm.start_date DESC, pwm.id DESC
                        """,
                (rs, rowNum) -> new PlanningSummary(
                        rs.getLong("id"),
                        rs.getDate("start_date").toLocalDate(),
                        rs.getDate("end_date").toLocalDate(),
                        rs.getInt("planned_days"),
                        PlanningState.valueOf(rs.getString("state")),
                        rs.getObject("menu_id", Long.class)
                ));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (jdbcTemplate.update("DELETE FROM proposed_week_menus WHERE id = ?", id) == 0) {
            throw new ResourceNotFoundException("Planning not found");
        }
    }

    @Override
    @Transactional
    public ProposedWeekMenu upsertDay(Long menuId, ProposedWeekMenuDay day) {
        ProposedWeekMenu menu = findById(menuId);
        if (day.getDate().isBefore(menu.getStartDate()) || day.getDate().isAfter(menu.getEndDate())) {
            throw new IllegalArgumentException("Day date must be inside the planning range");
        }

        Long dayId = jdbcTemplate.query("""
                        SELECT id
                        FROM proposed_week_menu_days
                        WHERE menu_id = ? AND menu_date = ?
                        """,
                (rs, rowNum) -> rs.getLong("id"),
                menuId,
                Date.valueOf(day.getDate())
        ).stream().findFirst().orElseGet(() -> insertDay(menuId, day));

        jdbcTemplate.update("DELETE FROM proposed_week_menu_sections WHERE day_id = ?", dayId);
        saveSections(dayId, day.getSections());
        jdbcTemplate.update("DELETE FROM proposed_week_menu_recipe_productions WHERE day_id = ?", dayId);
        saveRecipeProductions(dayId, day.getRecipeProductions());
        return findById(menuId);
    }

    private Long insertDay(Long menuId, ProposedWeekMenuDay day) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO proposed_week_menu_days (menu_id, menu_date)
                    VALUES (?, ?)
                    """, new String[]{"id"});
            ps.setLong(1, menuId);
            ps.setDate(2, Date.valueOf(day.getDate()));
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    private List<ProposedWeekMenuDay> findDays(Long menuId) {
        return jdbcTemplate.query("""
                        SELECT id, menu_date
                        FROM proposed_week_menu_days
                        WHERE menu_id = ?
                        ORDER BY menu_date
                        """,
                (rs, rowNum) -> ProposedWeekMenuDay.builder()
                        .id(rs.getLong("id"))
                        .date(rs.getDate("menu_date").toLocalDate())
                        .sections(findSections(rs.getLong("id")))
                        .recipeProductions(findRecipeProductions(rs.getLong("id")))
                        .build(),
                menuId
        );
    }

    private List<ProposedWeekMenuSection> findSections(Long dayId) {
        return jdbcTemplate.query("""
                        SELECT id, day_part_id, name, description, sort_order
                        FROM proposed_week_menu_sections
                        WHERE day_id = ?
                        ORDER BY sort_order, id
                        """,
                (rs, rowNum) -> ProposedWeekMenuSection.builder()
                        .id(rs.getLong("id"))
                        .dayPartId(rs.getLong("day_part_id"))
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .sortOrder(rs.getInt("sort_order"))
                        .products(findProducts(rs.getLong("id")))
                        .build(),
                dayId
        );
    }

    private List<ProposedWeekMenuRecipeProduction> findRecipeProductions(Long dayId) {
        return jdbcTemplate.query("""
                        SELECT id, recipe_id, units, sort_order
                        FROM proposed_week_menu_recipe_productions
                        WHERE day_id = ?
                        ORDER BY sort_order, id
                        """,
                (rs, rowNum) -> ProposedWeekMenuRecipeProduction.builder()
                        .id(rs.getLong("id"))
                        .recipeId(rs.getLong("recipe_id"))
                        .units(rs.getBigDecimal("units"))
                        .sortOrder(rs.getInt("sort_order"))
                        .build(),
                dayId
        );
    }

    private List<ProposedWeekMenuProduct> findProducts(Long sectionId) {
        return jdbcTemplate.query("""
                        SELECT product_id, product_name, units, grams, calories, carbohydrates, proteins, fats, sort_order
                        FROM proposed_week_menu_products
                        WHERE section_id = ?
                        ORDER BY sort_order
                        """,
                (rs, rowNum) -> ProposedWeekMenuProduct.builder()
                        .productId(rs.getObject("product_id", Long.class))
                        .productName(rs.getString("product_name"))
                        .units(rs.getBigDecimal("units"))
                        .grams(rs.getBigDecimal("grams"))
                        .nutritionalValues(readNutritionalValues(rs))
                        .sortOrder(rs.getInt("sort_order"))
                        .build(),
                sectionId
        );
    }

    private void saveSections(Long dayId, List<ProposedWeekMenuSection> sections) {
        try {
            for (ProposedWeekMenuSection section : sections == null ? List.<ProposedWeekMenuSection>of() : sections) {
                ProposedWeekMenuDayPart dayPart = dayPartRepository.findById(section.getDayPartId());
                Long sectionId = insertSection(dayId, dayPart);
                saveProducts(sectionId, section.getProducts());
            }
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Day parts and product sort orders must be unique within their parent");
        }
    }

    private void saveRecipeProductions(Long dayId, List<ProposedWeekMenuRecipeProduction> recipeProductions) {
        if (recipeProductions == null || recipeProductions.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate("""
                INSERT INTO proposed_week_menu_recipe_productions (day_id, recipe_id, units, sort_order)
                VALUES (?, ?, ?, ?)
                """, recipeProductions, recipeProductions.size(), (ps, recipeProduction) -> {
            ps.setLong(1, dayId);
            ps.setLong(2, recipeProduction.getRecipeId());
            ps.setBigDecimal(3, recipeProduction.getUnits());
            ps.setInt(4, recipeProduction.getSortOrder());
        });
    }

    private Long insertSection(Long dayId, ProposedWeekMenuDayPart dayPart) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO proposed_week_menu_sections (day_id, day_part_id, name, description, sort_order)
                    VALUES (?, ?, ?, ?, ?)
                    """, new String[]{"id"});
            ps.setLong(1, dayId);
            ps.setLong(2, dayPart.getId());
            ps.setString(3, dayPart.getName());
            ps.setString(4, dayPart.getDescription());
            ps.setInt(5, dayPart.getSortOrder());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    private void saveProducts(Long sectionId, List<ProposedWeekMenuProduct> products) {
        jdbcTemplate.batchUpdate("""
                INSERT INTO proposed_week_menu_products (
                    section_id, product_id, product_name, units, grams, calories, carbohydrates, proteins, fats, sort_order
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, products, products.size(), (ps, product) -> {
            ps.setLong(1, sectionId);
            if (product.getProductId() == null) {
                ps.setNull(2, Types.BIGINT);
            } else {
                ps.setLong(2, product.getProductId());
            }
            ps.setString(3, product.getProductName());
            if (product.getProductId() == null) {
                ps.setNull(4, Types.NUMERIC);
                ps.setNull(5, Types.NUMERIC);
            } else {
                ps.setBigDecimal(4, product.getUnits());
                ps.setBigDecimal(5, product.getGrams());
            }
            NutritionalValues nutritionalValues = product.getNutritionalValues();
            ps.setBigDecimal(6, nutritionalValues == null ? null : nutritionalValues.getCalories());
            ps.setBigDecimal(7, nutritionalValues == null ? null : nutritionalValues.getCarbohydrates());
            ps.setBigDecimal(8, nutritionalValues == null ? null : nutritionalValues.getProteins());
            ps.setBigDecimal(9, nutritionalValues == null ? null : nutritionalValues.getFats());
            ps.setInt(10, product.getSortOrder());
        });
    }

    private NutritionalValues readNutritionalValues(java.sql.ResultSet rs) throws java.sql.SQLException {
        BigDecimal calories = rs.getBigDecimal("calories");
        BigDecimal carbohydrates = rs.getBigDecimal("carbohydrates");
        BigDecimal proteins = rs.getBigDecimal("proteins");
        BigDecimal fats = rs.getBigDecimal("fats");
        if (calories == null && carbohydrates == null && proteins == null && fats == null) {
            return null;
        }
        return NutritionalValues.builder()
                .calories(calories)
                .carbohydrates(carbohydrates)
                .proteins(proteins)
                .fats(fats)
                .build();
    }

    private RowMapper<ProposedWeekMenu> menuRowMapper() {
        return (rs, rowNum) -> ProposedWeekMenu.builder()
                .id(rs.getLong("id"))
                .users(rs.getInt("users"))
                .startDate(rs.getDate("start_date").toLocalDate())
                .endDate(rs.getDate("end_date").toLocalDate())
                .build();
    }
}
