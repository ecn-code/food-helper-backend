package com.eliascanalesnieto.foodhelper.infra;

import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuRepository;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuResponse;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
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
        CurrentWeekMenuResponse persisted = new CurrentWeekMenuResponse(
                keyHolder.getKey().longValue(),
                menu.planningId(),
                menu.payerUserId(),
                menu.payerUsername(),
                menu.startDate(),
                menu.endDate(),
                menu.days(),
                menu.nutritionalValues(),
                menu.stockSummary(),
                menu.usedStock(),
                menu.shoppingList(),
                menu.nutritionalRules()
        );
        jdbcTemplate.update(
                "UPDATE current_week_menus SET snapshot_json = ? WHERE id = ?",
                objectMapper.writeValueAsString(persisted),
                persisted.id()
        );
        return persisted;
    }

    @Override
    public CurrentWeekMenuResponse findById(Long id) {
        return load("""
                SELECT snapshot_json
                FROM current_week_menus
                WHERE id = ?
                """, id);
    }

    @Override
    public CurrentWeekMenuResponse findByProposedWeekMenuId(Long proposedWeekMenuId) {
        return load("""
                SELECT snapshot_json
                FROM current_week_menus
                WHERE proposed_week_menu_id = ?
                """, proposedWeekMenuId);
    }

    @Override
    public List<CurrentWeekMenuResponse> findAll() {
        return jdbcTemplate.query("""
                        SELECT snapshot_json
                        FROM current_week_menus
                        ORDER BY id
                        """,
                (rs, rowNum) -> objectMapper.readValue(rs.getString("snapshot_json"), CurrentWeekMenuResponse.class)
        );
    }

    @Override
    public void delete(Long id) {
        if (jdbcTemplate.update("DELETE FROM current_week_menus WHERE id = ?", id) == 0) {
            throw new ResourceNotFoundException("Menu not found");
        }
    }

    private CurrentWeekMenuResponse load(String sql, Long value) {
        String snapshot = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("snapshot_json"), value)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found"));
        return objectMapper.readValue(snapshot, CurrentWeekMenuResponse.class);
    }
}
