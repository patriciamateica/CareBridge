package com.carebridge.backend.nursedetails.model;

import com.carebridge.backend.user.model.User;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "nurse_details")
public class NurseDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String specialization;

    @Column(name = "hospital_affiliation")
    private String hospitalAffiliation;

    @Column(name = "experience_years", nullable = false)
    private int experienceYears;

    @Column(name = "hire_me_status", nullable = false)
    private boolean hireMeStatus;

    public NurseDetails() {
    }

    public NurseDetails(
        User user,
        String specialization,
        String hospitalAffiliation,
        int experienceYears,
        boolean hireMeStatus
    ) {
        this.id = user.getId();
        this.user = user;
        this.specialization = specialization;
        this.hospitalAffiliation = hospitalAffiliation;
        this.experienceYears = experienceYears;
        this.hireMeStatus = hireMeStatus;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getHospitalAffiliation() {
        return hospitalAffiliation;
    }

    public void setHospitalAffiliation(String hospitalAffiliation) {
        this.hospitalAffiliation = hospitalAffiliation;
    }

    public int getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(int experienceYears) {
        this.experienceYears = experienceYears;
    }

    public boolean isHireMeStatus() {
        return hireMeStatus;
    }

    public void setHireMeStatus(boolean hireMeStatus) {
        this.hireMeStatus = hireMeStatus;
    }
}
