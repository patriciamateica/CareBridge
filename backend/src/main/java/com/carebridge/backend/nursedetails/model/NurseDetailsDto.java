package com.carebridge.backend.nursedetails.model;

import java.util.UUID;

public record NurseDetailsDto(
    UUID id,
    UUID userId,
    String specialization,
    String hospitalAffiliation,
    int experienceYears,
    boolean hireMeStatus
) {
}
