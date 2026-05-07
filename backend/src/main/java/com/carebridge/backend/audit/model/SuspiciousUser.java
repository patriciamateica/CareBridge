package com.carebridge.backend.audit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "suspicious_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuspiciousUser {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID userId;
    private String username;
    private String reason;
    private LocalDateTime flaggedAt;
    private String severity;
    private boolean resolved;
}
