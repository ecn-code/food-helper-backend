package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.MoneyBox;
import com.eliascanalesnieto.foodhelper.domain.MoneyBoxType;
import com.eliascanalesnieto.foodhelper.domain.UserMoneyBox;
import com.eliascanalesnieto.foodhelper.domain.UserMoneyMovement;
import com.eliascanalesnieto.foodhelper.domain.UserMoneyRepository;
import com.eliascanalesnieto.foodhelper.presentation.MoneyBoxMovementResponse;
import com.eliascanalesnieto.foodhelper.presentation.MoneyBoxResponse;
import com.eliascanalesnieto.foodhelper.presentation.UserMoneyBoxResponse;
import com.eliascanalesnieto.foodhelper.presentation.UserMoneyMovementResponse;
import com.eliascanalesnieto.foodhelper.presentation.error.DuplicateResourceException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
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

    @Transactional
    public MoneyBoxResponse createManualMoneyBox(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Money box name is required");
        }
        return toResponse(userMoneyRepository.createManualMoneyBox(name.trim()));
    }

    @Transactional(readOnly = true)
    public List<MoneyBoxResponse> findAllMoneyBoxes() {
        return userMoneyRepository.findAllMoneyBoxes().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MoneyBoxResponse findMoneyBoxById(Long moneyBoxId) {
        return toResponse(userMoneyRepository.findMoneyBoxById(moneyBoxId));
    }

    @Transactional
    public MoneyBoxMovementResponse addMoneyBoxMovement(Long moneyBoxId, BigDecimal amount, String description) {
        return toMoneyBoxMovementResponse(userMoneyRepository.addMoneyBoxMovement(
                moneyBoxId,
                scale(amount),
                description
        ));
    }

    @Transactional
    public void deleteMoneyBox(Long moneyBoxId) {
        MoneyBox moneyBox = userMoneyRepository.findMoneyBoxById(moneyBoxId);
        if (moneyBox.getType() != MoneyBoxType.MANUAL) {
            throw new DuplicateResourceException("User-owned money box cannot be deleted");
        }
        userMoneyRepository.deleteMoneyBox(moneyBoxId);
    }

    @Transactional
    public void deleteMoneyBoxMovement(Long moneyBoxId, Long movementId) {
        userMoneyRepository.deleteMoneyBoxMovement(moneyBoxId, movementId);
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

    private MoneyBoxResponse toResponse(MoneyBox moneyBox) {
        return new MoneyBoxResponse(
                moneyBox.getId(),
                moneyBox.getType(),
                moneyBox.getName(),
                moneyBox.getUserId(),
                moneyBox.getUsername(),
                scale(moneyBox.getBalance()),
                moneyBox.getMovements().stream().map(this::toMoneyBoxMovementResponse).toList()
        );
    }

    private MoneyBoxMovementResponse toMoneyBoxMovementResponse(UserMoneyMovement movement) {
        return new MoneyBoxMovementResponse(
                movement.getId(),
                movement.getMoneyBoxId(),
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
