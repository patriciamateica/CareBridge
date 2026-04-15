package com.carebridge.backend.user.model;


import com.carebridge.backend.user.Role;
import com.carebridge.backend.user.UserStatus;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class User {

    private UUID id;
    private String activationNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private int phoneNumber;
    private LocalDate dateOfBirth;
    private String residentialAddress;
    private String nationality;
    private Role role;
    private UserStatus userStatus;

    public User() {
    }

    public User(
        UUID id,
        String activationNumber,
        String firstName,
        String lastName,
        String email,
        String password,
        int phoneNumber,
        Role role
    ) {
        this.id = id;
        this.activationNumber = activationNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(int phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getActivationNumber() {
        return activationNumber;
    }

    public void setActivationNumber(String activationNumber) {
        this.activationNumber = activationNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getResidentialAddress() {
        return residentialAddress;
    }

    public void setResidentialAddress(String residentialAddress) {
        this.residentialAddress = residentialAddress;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public UserStatus getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
