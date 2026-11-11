package com.skillforge.opportunity.integration;

import java.util.Map;

public interface SkillCatalogGateway {
    boolean existsById(Long skillId);
    String getSkillName(Long skillId);
    Map<Long, String> getSkillNames(Iterable<Long> skillIds);
}
