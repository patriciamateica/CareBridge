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
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.UUID;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final Sinks.Many<User> registrationSink = Sinks.many().multicast().onBackpressureBuffer();
    private final Sinks.Many<User> statusUpdateSink = Sinks.many().multicast().onBackpressureBuffer();

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.findByEmailIgnoreCase(request.email()).isPresent()) {
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

        User savedUser = userRepository.save(user);
        emitRegistration(savedUser);
        return savedUser;
    }

    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public User getUserById(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    @Transactional
    public User updateStatus(UUID id, UserStatus status) {
        User user = getUserById(id);
        user.setUserStatus(status);
        User updatedUser = userRepository.save(user);
        statusUpdateSink.tryEmitNext(updatedUser);
        return updatedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new CustomUserDetails(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserByEmail(String emailFromToken) {
        return loadUserByUsername(emailFromToken);
    }

    @Transactional(readOnly = true)
    public Page<User> getUsersPaginated(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional
    public void deleteAll(){
        userRepository.deleteAll();
    }

    @Transactional
    public Flux<User> getUserRegistrationStream() {
        return registrationSink.asFlux();
    }

    @Transactional
    public void emitRegistration(User user) {
        registrationSink.tryEmitNext(user);
    }

    @Transactional
    public Flux<User> getUserStatusStream(UUID id) {
        return statusUpdateSink.asFlux()
            .filter(user -> id == null || user.getId().equals(id));
    }
}
