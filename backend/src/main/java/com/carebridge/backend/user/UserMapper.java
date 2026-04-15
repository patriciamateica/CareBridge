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
            user.getRole(),
            user.getUserStatus()
        );
    }

}
