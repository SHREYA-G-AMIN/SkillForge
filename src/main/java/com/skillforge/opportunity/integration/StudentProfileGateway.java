package com.skillforge.opportunity.integration;

public interface StudentProfileGateway {
    /**
     * Retrieves the student profile data for the given user ID.
     * Returns null if no profile has been created yet.
     */
    StudentProfileData getStudentProfile(Long userId);
}
