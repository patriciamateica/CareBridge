package com.carebridge.backend.user;

import com.carebridge.backend.security.RegisterRequest;
import com.carebridge.backend.user.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPhoneNumber(Integer.parseInt(request.phoneNumber()));
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.PATIENT);
        user.setUserStatus(UserStatus.ACTIVE);

        return userRepository.save(user);
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new CustomUserDetails(user);
    }

    public UserDetails loadUserByEmail(String emailFromToken) {
        return loadUserByUsername(emailFromToken);
    }

    public UserDetails findByEmail(String email) {
        return loadUserByUsername(email);
    }

    public List<User> getUsersPaginated(int page, int size) {
        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("Invalid page or size parameters");
        }
        return userRepository.findAllPaginated(page, size);
    }
}
