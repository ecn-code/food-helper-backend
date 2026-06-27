package com.eliascanalesnieto.foodhelper.infra;

import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuStatsRepository;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuStatsResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuResponse;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import java.time.YearMonth;
import java.util.List;
import java.sql.PreparedStatement;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

@Repository
@RequiredArgsConstructor
public class JdbcCurrentWeekMenuStatsRepository implements CurrentWeekMenuStatsRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public CurrentWeekMenuStatsResponse save(CurrentWeekMenuStatsResponse stats) {
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO current_week_menu_stats (current_week_menu_id, stats_json)
                    VALUES (?, ?)
                    ON CONFLICT (current_week_menu_id)
                    DO UPDATE SET stats_json = EXCLUDED.stats_json, closed_at = now()
                    """);
            ps.setLong(1, stats.menuId());
            ps.setString(2, objectMapper.writeValueAsString(stats));
            return ps;
        });
        return stats;
    }

    @Override
    public CurrentWeekMenuStatsResponse findByCurrentWeekMenuId(Long currentWeekMenuId) {
        String snapshot = jdbcTemplate.query("""
                        SELECT stats_json
                        FROM current_week_menu_stats
                        WHERE current_week_menu_id = ?
                        """,
                (rs, rowNum) -> rs.getString("stats_json"),
                currentWeekMenuId
        ).stream().findFirst().orElseThrow(() -> new ResourceNotFoundException("Menu stats not found"));
        return objectMapper.readValue(snapshot, CurrentWeekMenuStatsResponse.class);
    }

    @Override
    public CurrentWeekMenuStatsResponse findByProposedWeekMenuId(Long proposedWeekMenuId) {
        String snapshot = jdbcTemplate.query("""
                        SELECT cwms.stats_json
                        FROM current_week_menu_stats cwms
                        JOIN current_week_menus cwm ON cwm.id = cwms.current_week_menu_id
                        WHERE cwm.proposed_week_menu_id = ?
                        """,
                (rs, rowNum) -> rs.getString("stats_json"),
                proposedWeekMenuId
        ).stream().findFirst().orElseThrow(() -> new ResourceNotFoundException("Menu stats not found"));
        return objectMapper.readValue(snapshot, CurrentWeekMenuStatsResponse.class);
    }

    @Override
    public List<CurrentWeekMenuResponse> findClosedWeekMenusByMonth(YearMonth month) {
        return jdbcTemplate.query("""
                        SELECT cwm.snapshot_json
                        FROM current_week_menu_stats cwms
                        JOIN current_week_menus cwm ON cwm.id = cwms.current_week_menu_id
                        WHERE EXTRACT(YEAR FROM (cwm.snapshot_json::json ->> 'endDate')::date) = ?
                          AND EXTRACT(MONTH FROM (cwm.snapshot_json::json ->> 'endDate')::date) = ?
                        ORDER BY (cwm.snapshot_json::json ->> 'endDate')::date, cwm.id
                        """,
                (rs, rowNum) -> objectMapper.readValue(rs.getString("snapshot_json"), CurrentWeekMenuResponse.class),
                month.getYear(),
                month.getMonthValue()
        );
    }
}
