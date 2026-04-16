package com.carebridge.backend.user;

import com.carebridge.backend.security.RegisterRequest;
import com.carebridge.backend.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final Sinks.Many<User> registrationSink = Sinks.many().multicast().onBackpressureBuffer();
    private final Sinks.Many<User> statusUpdateSink = Sinks.many().multicast().onBackpressureBuffer();

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

    public User getUserById(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    public User updateStatus(UUID id, UserStatus status) {
        User user = getUserById(id);
        user.setUserStatus(status);
        User updatedUser = userRepository.save(user);
        statusUpdateSink.tryEmitNext(updatedUser);
        return updatedUser;
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

    public Page<User> getUsersPaginated(Pageable pageable) {
        return userRepository.findAllPaginated(pageable);
    }

    public void deleteAll(){
        userRepository.deleteAll();
    }

    public Flux<User> getUserRegistrationStream() {
        return registrationSink.asFlux();
    }

    public void emitRegistration(User user) {
        registrationSink.tryEmitNext(user);
    }

    public Flux<User> getUserStatusStream(UUID id) {
        return statusUpdateSink.asFlux()
            .filter(user -> id == null || user.getId().equals(id));
    }
}
