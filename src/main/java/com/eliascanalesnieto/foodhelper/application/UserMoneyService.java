package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.UserMoneyBox;
import com.eliascanalesnieto.foodhelper.domain.UserMoneyMovement;
import com.eliascanalesnieto.foodhelper.domain.UserMoneyRepository;
import com.eliascanalesnieto.foodhelper.presentation.UserMoneyBoxResponse;
import com.eliascanalesnieto.foodhelper.presentation.UserMoneyMovementResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserMoneyService {
    private static final int SCALE = 2;

    private final UserMoneyRepository userMoneyRepository;

    @Transactional
    public UserMoneyMovementResponse addMovement(Long userId, BigDecimal amount, String description) {
        return toResponse(userMoneyRepository.addMovement(userId, scale(amount), description, null));
    }

    @Transactional(readOnly = true)
    public UserMoneyBoxResponse findMoneyBox(Long userId) {
        return toResponse(userMoneyRepository.findMoneyBox(userId));
    }

    private UserMoneyBoxResponse toResponse(UserMoneyBox moneyBox) {
        return new UserMoneyBoxResponse(
                moneyBox.getUserId(),
                moneyBox.getUsername(),
                scale(moneyBox.getBalance()),
                moneyBox.getMovements().stream().map(this::toResponse).toList()
        );
    }

    private UserMoneyMovementResponse toResponse(UserMoneyMovement movement) {
        return new UserMoneyMovementResponse(
                movement.getId(),
                movement.getUserId(),
                scale(movement.getAmount()),
                movement.getDescription(),
                movement.getCurrentWeekMenuId(),
                movement.getCreatedAt()
        );
    }

    private BigDecimal scale(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("Amount is required");
        }
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }
}
