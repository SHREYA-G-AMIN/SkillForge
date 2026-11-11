package com.skillforge.opportunity.integration;

public interface CurrentUserProvider {
    /**
     * Returns the currently authenticated user's context.
     * The role is returned without the "ROLE_" prefix (e.g. "STUDENT", "RECRUITER", "ADMIN").
     */
    UserContext getCurrentUser();
}
