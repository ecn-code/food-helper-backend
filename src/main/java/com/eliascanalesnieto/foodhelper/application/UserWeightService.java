package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.UserWeightEntry;
import com.eliascanalesnieto.foodhelper.domain.UserWeightRepository;
import com.eliascanalesnieto.foodhelper.presentation.UserWeightResponse;
import com.eliascanalesnieto.foodhelper.presentation.UserWeightStatsResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserWeightService {
    private static final int SCALE = 2;

    private final UserWeightRepository repository;

    @Transactional
    public UserWeightResponse create(Long userId, BigDecimal weight, Instant recordedAt) {
        validateMeasurement(weight, recordedAt);
        return toResponse(repository.create(userId, scale(weight), recordedAt));
    }

    @Transactional
    public UserWeightResponse update(Long userId, Long weightId, BigDecimal weight, Instant recordedAt) {
        validateMeasurement(weight, recordedAt);
        return toResponse(repository.update(userId, weightId, scale(weight), recordedAt));
    }

    @Transactional
    public void delete(Long userId, Long weightId) {
        repository.delete(userId, weightId);
    }

    @Transactional(readOnly = true)
    public List<UserWeightResponse> findByPeriod(Long userId, Instant from, Instant to) {
        validatePeriod(from, to);
        return repository.findByPeriod(userId, from, to).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserWeightStatsResponse findStats(Long userId, Instant from, Instant to) {
        validatePeriod(from, to);
        List<UserWeightEntry> entries = repository.findByPeriod(userId, from, to);
        BigDecimal highestWeight = entries.stream().map(UserWeightEntry::getWeight).max(Comparator.naturalOrder()).orElse(null);
        BigDecimal lowestWeight = entries.stream().map(UserWeightEntry::getWeight).min(Comparator.naturalOrder()).orElse(null);
        UserWeightResponse highest = firstWithWeight(entries, highestWeight);
        UserWeightResponse lowest = firstWithWeight(entries, lowestWeight);
        return new UserWeightStatsResponse(userId, from, to, highest, lowest);
    }

    private UserWeightResponse firstWithWeight(List<UserWeightEntry> entries, BigDecimal weight) {
        if (weight == null) {
            return null;
        }
        return entries.stream()
                .filter(entry -> entry.getWeight().compareTo(weight) == 0)
                .findFirst()
                .map(this::toResponse)
                .orElse(null);
    }

    private void validatePeriod(Instant from, Instant to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Period start and end are required");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("Period start must not be after period end");
        }
    }

    private void validateMeasurement(BigDecimal weight, Instant recordedAt) {
        if (weight == null || weight.signum() <= 0) {
            throw new IllegalArgumentException("Weight must be greater than zero");
        }
        if (recordedAt == null) {
            throw new IllegalArgumentException("Recording date and time are required");
        }
    }

    private UserWeightResponse toResponse(UserWeightEntry entry) {
        return new UserWeightResponse(entry.getId(), entry.getUserId(), scale(entry.getWeight()), entry.getRecordedAt());
    }

    private BigDecimal scale(BigDecimal weight) {
        return weight.setScale(SCALE, RoundingMode.HALF_UP);
    }
}
