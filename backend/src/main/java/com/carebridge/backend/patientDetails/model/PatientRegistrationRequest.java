package com.carebridge.backend.patientDetails.model;

import com.carebridge.backend.security.RegisterRequest;

public record PatientRegistrationRequest(
    RegisterRequest userRequest,
    String primaryDiagnosis,
    String assignedNurseId,
    String emergencyContact,
    String status
) {}
