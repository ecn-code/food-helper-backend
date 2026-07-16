package com.eliascanalesnieto.foodhelper.infra;

import com.eliascanalesnieto.foodhelper.domain.ChallengeDefinition;
import com.eliascanalesnieto.foodhelper.domain.ChallengeDefinitionRepository;
import com.eliascanalesnieto.foodhelper.presentation.error.DuplicateResourceException;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcChallengeDefinitionRepository implements ChallengeDefinitionRepository {
    private final JdbcClient jdbcClient;
    @Override public List<ChallengeDefinition> findAll() { return jdbcClient.sql("SELECT id, code, name, description, reward_amount, period_days FROM challenge_definitions ORDER BY code").query(this::map).list(); }
    @Override public ChallengeDefinition findByCode(String code) { return jdbcClient.sql("SELECT id, code, name, description, reward_amount, period_days FROM challenge_definitions WHERE code = :code").param("code", code).query(this::map).optional().orElseThrow(() -> new ResourceNotFoundException("Challenge not found")); }
    @Override public ChallengeDefinition create(ChallengeDefinition challenge) { try { Long id = jdbcClient.sql("INSERT INTO challenge_definitions (code, name, description, reward_amount, period_days) VALUES (:code, :name, :description, :rewardAmount, :periodDays) RETURNING id").param("code", challenge.getCode()).param("name", challenge.getName()).param("description", challenge.getDescription()).param("rewardAmount", challenge.getRewardAmount()).param("periodDays", challenge.getPeriodDays()).query(Long.class).single(); return challenge.toBuilder().id(id).build(); } catch (DataIntegrityViolationException ex) { throw new DuplicateResourceException("Challenge code already exists"); } }
    @Override public ChallengeDefinition update(String code, ChallengeDefinition challenge) { int count = jdbcClient.sql("UPDATE challenge_definitions SET name = :name, description = :description, reward_amount = :rewardAmount, period_days = :periodDays WHERE code = :code").param("code", code).param("name", challenge.getName()).param("description", challenge.getDescription()).param("rewardAmount", challenge.getRewardAmount()).param("periodDays", challenge.getPeriodDays()).update(); if (count == 0) throw new ResourceNotFoundException("Challenge not found"); return findByCode(code); }
    @Override public void delete(String code) { if (jdbcClient.sql("DELETE FROM challenge_definitions WHERE code = :code").param("code", code).update() == 0) throw new ResourceNotFoundException("Challenge not found"); }
    private ChallengeDefinition map(java.sql.ResultSet rs, int row) throws java.sql.SQLException { return ChallengeDefinition.builder().id(rs.getLong("id")).code(rs.getString("code")).name(rs.getString("name")).description(rs.getString("description")).rewardAmount(rs.getBigDecimal("reward_amount")).periodDays(rs.getInt("period_days")).build(); }
}
