package com.eliascanalesnieto.foodhelper.infra;

import com.eliascanalesnieto.foodhelper.domain.AppUser;
import com.eliascanalesnieto.foodhelper.domain.UserMenuHistoryRepository;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuResponse;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

@Repository
@RequiredArgsConstructor
public class JdbcUserMenuHistoryRepository implements UserMenuHistoryRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void save(Long menuId, AppUser person, CurrentWeekMenuResponse menuSnapshot) {
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO user_menu_history
                        (current_week_menu_id, person_id, person_name, menu_start_date, menu_end_date, menu_snapshot_json)
                    VALUES (?, ?, ?, ?, ?, ?)
                    ON CONFLICT (current_week_menu_id, person_id) DO NOTHING
                    """);
            ps.setLong(1, menuId);
            ps.setLong(2, person.getId());
            ps.setString(3, person.getUsername());
            ps.setObject(4, menuSnapshot.startDate());
            ps.setObject(5, menuSnapshot.endDate());
            ps.setString(6, objectMapper.writeValueAsString(menuSnapshot));
            return ps;
        });
    }

    @Override
    public List<CurrentWeekMenuResponse> findMenus(Long personId, LocalDate from, LocalDate to) {
        return jdbcTemplate.query("""
                        SELECT menu_snapshot_json
                        FROM user_menu_history
                        WHERE person_id = ?
                          AND menu_end_date BETWEEN ? AND ?
                        ORDER BY menu_end_date, current_week_menu_id
                        """,
                (rs, rowNum) -> objectMapper.readValue(rs.getString("menu_snapshot_json"), CurrentWeekMenuResponse.class),
                personId, from, to
        );
    }

    @Override
    public List<Long> findPersonIds(Long menuId) {
        return jdbcTemplate.query("""
                        SELECT DISTINCT person_id
                        FROM user_menu_history
                        WHERE current_week_menu_id = ?
                        ORDER BY person_id
                        """,
                (rs, rowNum) -> rs.getLong("person_id"),
                menuId
        );
    }
}
