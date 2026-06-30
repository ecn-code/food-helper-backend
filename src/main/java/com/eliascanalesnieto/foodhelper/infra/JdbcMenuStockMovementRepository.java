package com.eliascanalesnieto.foodhelper.infra;

import com.eliascanalesnieto.foodhelper.domain.MenuStockMovement;
import com.eliascanalesnieto.foodhelper.domain.MenuStockMovementRepository;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcMenuStockMovementRepository implements MenuStockMovementRepository {
    private static final int SCALE = 2;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public MenuStockMovement save(MenuStockMovement movement) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO menu_stock_movements
                        (current_week_menu_id, user_id, user_username, product_id, product_name, quantity, price, total_cost, description)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, new String[]{"id"});
            ps.setLong(1, movement.getCurrentWeekMenuId());
            ps.setLong(2, movement.getUserId());
            ps.setString(3, movement.getUserUsername());
            ps.setLong(4, movement.getProductId());
            ps.setString(5, movement.getProductName());
            ps.setBigDecimal(6, movement.getQuantity());
            ps.setBigDecimal(7, scale(movement.getPrice()));
            ps.setBigDecimal(8, scale(movement.getTotalCost()));
            ps.setString(9, movement.getDescription());
            return ps;
        }, keyHolder);
        return movement.toBuilder().id(keyHolder.getKey().longValue()).build();
    }

    @Override
    public List<MenuStockMovement> findByCurrentWeekMenuId(Long currentWeekMenuId) {
        return jdbcTemplate.query("""
                        SELECT id, current_week_menu_id, user_id, user_username, product_id, product_name,
                               quantity, price, total_cost, description, created_at
                        FROM menu_stock_movements
                        WHERE current_week_menu_id = ?
                        ORDER BY created_at, id
                        """,
                (rs, rowNum) -> MenuStockMovement.builder()
                        .id(rs.getLong("id"))
                        .currentWeekMenuId(rs.getLong("current_week_menu_id"))
                        .userId(rs.getLong("user_id"))
                        .userUsername(rs.getString("user_username"))
                        .productId(rs.getLong("product_id"))
                        .productName(rs.getString("product_name"))
                        .quantity(rs.getBigDecimal("quantity"))
                        .price(rs.getBigDecimal("price"))
                        .totalCost(rs.getBigDecimal("total_cost"))
                        .description(rs.getString("description"))
                        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                        .build(),
                currentWeekMenuId);
    }

    @Override
    public void deleteByCurrentWeekMenuId(Long currentWeekMenuId) {
        jdbcTemplate.update("DELETE FROM menu_stock_movements WHERE current_week_menu_id = ?", currentWeekMenuId);
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(SCALE, java.math.RoundingMode.HALF_UP);
    }
}
