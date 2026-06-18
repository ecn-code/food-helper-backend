package com.eliascanalesnieto.foodhelper.infra;

import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDayPart;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDayPartRepository;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import java.sql.PreparedStatement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class JdbcProposedWeekMenuDayPartRepository implements ProposedWeekMenuDayPartRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public ProposedWeekMenuDayPart create(ProposedWeekMenuDayPart dayPart) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO proposed_week_menu_day_parts (name, description, sort_order)
                    VALUES (?, ?, ?)
                    """, new String[]{"id"});
            ps.setString(1, dayPart.getName());
            ps.setString(2, dayPart.getDescription());
            ps.setInt(3, dayPart.getSortOrder());
            return ps;
        }, keyHolder);
        return dayPart.toBuilder()
                .id(keyHolder.getKey().longValue())
                .build();
    }

    @Override
    @Transactional
    public ProposedWeekMenuDayPart update(Long id, ProposedWeekMenuDayPart dayPart) {
        int updated = jdbcTemplate.update("""
                        UPDATE proposed_week_menu_day_parts
                        SET name = ?, description = ?, sort_order = ?
                        WHERE id = ?
                        """,
                dayPart.getName(),
                dayPart.getDescription(),
                dayPart.getSortOrder(),
                id
        );
        if (updated == 0) {
            throw new ResourceNotFoundException("Day part not found");
        }
        return dayPart.toBuilder().id(id).build();
    }

    @Override
    public ProposedWeekMenuDayPart findById(Long id) {
        return jdbcTemplate.query("""
                        SELECT id, name, description, sort_order
                        FROM proposed_week_menu_day_parts
                        WHERE id = ?
                        """,
                rowMapper(),
                id
        ).stream().findFirst().orElseThrow(() -> new ResourceNotFoundException("Day part not found"));
    }

    @Override
    public List<ProposedWeekMenuDayPart> findAll() {
        return jdbcTemplate.query("""
                        SELECT id, name, description, sort_order
                        FROM proposed_week_menu_day_parts
                        ORDER BY sort_order, id
                        """,
                rowMapper());
    }

    private RowMapper<ProposedWeekMenuDayPart> rowMapper() {
        return (rs, rowNum) -> ProposedWeekMenuDayPart.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .sortOrder(rs.getInt("sort_order"))
                .build();
    }
}
