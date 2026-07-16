package com.eliascanalesnieto.foodhelper.domain;

import java.util.List;

public interface ChallengeDefinitionRepository {
    List<ChallengeDefinition> findAll();
    ChallengeDefinition findByCode(String code);
    ChallengeDefinition create(ChallengeDefinition challenge);
    ChallengeDefinition update(String code, ChallengeDefinition challenge);
    void delete(String code);
}
