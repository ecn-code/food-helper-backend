package com.eliascanalesnieto.foodhelper.infra;

import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuRepository;
import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuState;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuResponse;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.sql.PreparedStatement;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

@Repository
@RequiredArgsConstructor
public class JdbcCurrentWeekMenuRepository implements CurrentWeekMenuRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public CurrentWeekMenuResponse create(CurrentWeekMenuResponse menu) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO current_week_menus (proposed_week_menu_id, snapshot_json)
                    VALUES (?, ?)
                    """, new String[]{"id"});
            ps.setLong(1, menu.planningId());
            ps.setString(2, objectMapper.writeValueAsString(menu));
            return ps;
        }, keyHolder);
        return save(new CurrentWeekMenuResponse(
                keyHolder.getKey().longValue(),
                menu.planningId(),
                menu.payerUserId(),
                menu.payerUsername(),
                menu.personIds() == null ? List.of() : menu.personIds(),
                menu.startDate(),
                menu.endDate(),
                menu.days(),
                menu.nutritionalValues(),
                menu.stockSummary(),
                menu.usedStock(),
                menu.weekStock(),
                menu.shoppingList(),
                menu.stockMovements(),
                menu.recipeProductions(),
                menu.nutritionalRules(),
                CurrentWeekMenuState.ESTABLISHED
        ));
    }

    @Override
    public CurrentWeekMenuResponse save(CurrentWeekMenuResponse menu) {
        jdbcTemplate.update(
                "UPDATE current_week_menus SET snapshot_json = ? WHERE id = ?",
                objectMapper.writeValueAsString(menu),
                menu.id()
        );
        return menu;
    }

    @Override
    public CurrentWeekMenuResponse findById(Long id) {
        return load("""
                SELECT cwm.snapshot_json, cwms.current_week_menu_id IS NOT NULL AS closed
                FROM current_week_menus cwm
                LEFT JOIN current_week_menu_stats cwms ON cwms.current_week_menu_id = cwm.id
                WHERE cwm.id = ?
                """, id);
    }

    @Override
    public CurrentWeekMenuResponse findByProposedWeekMenuId(Long proposedWeekMenuId) {
        return load("""
                SELECT cwm.snapshot_json, cwms.current_week_menu_id IS NOT NULL AS closed
                FROM current_week_menus cwm
                LEFT JOIN current_week_menu_stats cwms ON cwms.current_week_menu_id = cwm.id
                WHERE cwm.proposed_week_menu_id = ?
                """, proposedWeekMenuId);
    }

    @Override
    public List<CurrentWeekMenuResponse> findAll() {
        return jdbcTemplate.query("""
                        SELECT cwm.snapshot_json, cwms.current_week_menu_id IS NOT NULL AS closed
                        FROM current_week_menus cwm
                        LEFT JOIN current_week_menu_stats cwms ON cwms.current_week_menu_id = cwm.id
                        ORDER BY cwm.id
                        """,
                (rs, rowNum) -> withState(
                        objectMapper.readValue(rs.getString("snapshot_json"), CurrentWeekMenuResponse.class),
                        rs.getBoolean("closed") ? CurrentWeekMenuState.CLOSED : CurrentWeekMenuState.ESTABLISHED
                )
        );
    }

    @Override
    public List<CurrentWeekMenuResponse> findAll(CurrentWeekMenuState state) {
        StringBuilder sql = new StringBuilder("""
                SELECT cwm.snapshot_json, cwms.current_week_menu_id IS NOT NULL AS closed
                FROM current_week_menus cwm
                LEFT JOIN current_week_menu_stats cwms ON cwms.current_week_menu_id = cwm.id
                """);
        appendStateFilter(sql, state);
        sql.append("""

                ORDER BY (cwm.snapshot_json::json ->> 'startDate')::date DESC, cwm.id DESC
                """);
        return jdbcTemplate.query(
                sql.toString(),
                (rs, rowNum) -> withState(
                        objectMapper.readValue(rs.getString("snapshot_json"), CurrentWeekMenuResponse.class),
                        rs.getBoolean("closed") ? CurrentWeekMenuState.CLOSED : CurrentWeekMenuState.ESTABLISHED
                )
        );
    }

    @Override
    public List<CurrentWeekMenuResponse> findPage(int offset, int limit, CurrentWeekMenuState state) {
        StringBuilder sql = new StringBuilder("""
                SELECT cwm.snapshot_json, cwms.current_week_menu_id IS NOT NULL AS closed
                FROM current_week_menus cwm
                LEFT JOIN current_week_menu_stats cwms ON cwms.current_week_menu_id = cwm.id
                """);
        List<Object> params = new ArrayList<>();
        appendStateFilter(sql, state);
        sql.append("""
                
                ORDER BY (cwm.snapshot_json::json ->> 'startDate')::date DESC, cwm.id DESC
                LIMIT ?
                OFFSET ?
                """);
        params.add(limit);
        params.add(offset);
        return jdbcTemplate.query(
                sql.toString(),
                params.toArray(),
                (rs, rowNum) -> withState(
                        objectMapper.readValue(rs.getString("snapshot_json"), CurrentWeekMenuResponse.class),
                        rs.getBoolean("closed") ? CurrentWeekMenuState.CLOSED : CurrentWeekMenuState.ESTABLISHED
                )
        );
    }

    @Override
    public long count(CurrentWeekMenuState state) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM current_week_menus cwm
                LEFT JOIN current_week_menu_stats cwms ON cwms.current_week_menu_id = cwm.id
                """);
        appendStateFilter(sql, state);
        Long value = jdbcTemplate.queryForObject(sql.toString(), Long.class);
        return value == null ? 0 : value;
    }

    @Override
    public void delete(Long id) {
        if (jdbcTemplate.update("DELETE FROM current_week_menus WHERE id = ?", id) == 0) {
            throw new ResourceNotFoundException("Menu not found");
        }
    }

    private CurrentWeekMenuResponse load(String sql, Long value) {
        return jdbcTemplate.query(sql, (rs, rowNum) -> withState(
                        objectMapper.readValue(rs.getString("snapshot_json"), CurrentWeekMenuResponse.class),
                        rs.getBoolean("closed") ? CurrentWeekMenuState.CLOSED : CurrentWeekMenuState.ESTABLISHED
                ),
                value)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found"));
    }

    private CurrentWeekMenuResponse withState(CurrentWeekMenuResponse menu, CurrentWeekMenuState state) {
        return new CurrentWeekMenuResponse(
                menu.id(),
                menu.planningId(),
                menu.payerUserId(),
                menu.payerUsername(),
                menu.personIds(),
                menu.startDate(),
                menu.endDate(),
                menu.days(),
                menu.nutritionalValues(),
                menu.stockSummary(),
                menu.usedStock(),
                menu.weekStock(),
                menu.shoppingList(),
                menu.stockMovements(),
                menu.recipeProductions(),
                menu.nutritionalRules(),
                state
        );
    }

    private void appendStateFilter(StringBuilder sql, CurrentWeekMenuState state) {
        if (state == null) {
            return;
        }
        sql.append(" WHERE ");
        if (state == CurrentWeekMenuState.CLOSED) {
            sql.append("cwms.current_week_menu_id IS NOT NULL");
        } else {
            sql.append("cwms.current_week_menu_id IS NULL");
        }
    }
}
