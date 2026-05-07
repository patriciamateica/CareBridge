package com.carebridge.backend.user;

import com.carebridge.backend.user.model.User;
import com.carebridge.backend.user.model.UserDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto mapToDto(User user) {
        return new UserDto(
            user.getId(),
            user.getActivationNumber(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getPhoneNumber(),
            user.getDateOfBirth(),
            user.getResidentialAddress(),
            user.getNationality(),
            user.getRoles().stream().map(com.carebridge.backend.user.model.Role::getName).collect(java.util.stream.Collectors.toSet()),
            user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(com.carebridge.backend.user.model.Permission::getName)
                .collect(java.util.stream.Collectors.toSet()),
            user.getUserStatus(),
            user.isActive()
        );
    }

}
