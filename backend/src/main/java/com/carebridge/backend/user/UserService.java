package com.carebridge.backend.user;

import com.carebridge.backend.patientDetails.PatientDetailsService;
import com.carebridge.backend.patientDetails.model.PatientDetails;
import com.carebridge.backend.patientDetails.model.PatientRegistrationRequest;
import com.carebridge.backend.security.RegisterRequest;
import com.carebridge.backend.user.model.User;
import com.carebridge.backend.appointments.AppointmentsRepository;
import com.carebridge.backend.carenotes.CareNotesRepository;
import com.carebridge.backend.clinicalLog.ClinicalLogRepository;
import com.carebridge.backend.healthStatus.HealthStatusRepository;
import com.carebridge.backend.prescription.PrescriptionRepository;
import com.carebridge.backend.roster.RosterRepository;
import com.carebridge.backend.task.TaskRepository;
import com.carebridge.backend.vitals.VitalsRepository;
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
    private final PatientDetailsService patientDetailsService;
    private final VitalsRepository vitalsRepository;
    private final TaskRepository taskRepository;
    private final AppointmentsRepository appointmentsRepository;
    private final RosterRepository rosterRepository;
    private final CareNotesRepository careNotesRepository;
    private final ClinicalLogRepository clinicalLogRepository;
    private final HealthStatusRepository healthStatusRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final RoleRepository roleRepository;

    private final Sinks.Many<User> registrationSink = Sinks.many().multicast().onBackpressureBuffer();
    private final Sinks.Many<User> statusUpdateSink = Sinks.many().multicast().onBackpressureBuffer();

    public UserService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        PatientDetailsService patientDetailsService,
        VitalsRepository vitalsRepository,
        TaskRepository taskRepository,
        AppointmentsRepository appointmentsRepository,
        RosterRepository rosterRepository,
        CareNotesRepository careNotesRepository,
        ClinicalLogRepository clinicalLogRepository,
        HealthStatusRepository healthStatusRepository,
        PrescriptionRepository prescriptionRepository,
        RoleRepository roleRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.patientDetailsService = patientDetailsService;
        this.vitalsRepository = vitalsRepository;
        this.taskRepository = taskRepository;
        this.appointmentsRepository = appointmentsRepository;
        this.rosterRepository = rosterRepository;
        this.careNotesRepository = careNotesRepository;
        this.clinicalLogRepository = clinicalLogRepository;
        this.healthStatusRepository = healthStatusRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public User registerPatient(PatientRegistrationRequest request) {
        User user = register(request.userRequest());

        java.util.UUID nurseId = null;
        if (request.assignedNurseId() != null && !request.assignedNurseId().isEmpty() && !"null".equals(request.assignedNurseId())) {
            try {
                nurseId = java.util.UUID.fromString(request.assignedNurseId());
            } catch (IllegalArgumentException e) {
            }
        }

        PatientDetails details = new PatientDetails(
            user,
            request.primaryDiagnosis(),
            new java.util.ArrayList<>(),
            new java.util.ArrayList<>(),
            request.emergencyContact(),
            nurseId,
            request.status()
        );

        patientDetailsService.create(details);

        return user;
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
        user.setPhoneNumber(request.phoneNumber());
        user.setPassword(passwordEncoder.encode(request.password()));

        roleRepository.findByName("PATIENT").ifPresent(user::addRole);

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

    @Transactional
    public User update(UUID id, User userDetails) {
        User user = getUserById(id);
        if (userDetails.getFirstName() != null) user.setFirstName(userDetails.getFirstName());
        if (userDetails.getLastName() != null) user.setLastName(userDetails.getLastName());
        if (userDetails.getPhoneNumber() != null) user.setPhoneNumber(userDetails.getPhoneNumber());
        if (userDetails.getDateOfBirth() != null) user.setDateOfBirth(userDetails.getDateOfBirth());
        if (userDetails.getResidentialAddress() != null) user.setResidentialAddress(userDetails.getResidentialAddress());
        if (userDetails.getNationality() != null) user.setNationality(userDetails.getNationality());
        if (userDetails.getUserStatus() != null) user.setUserStatus(userDetails.getUserStatus());
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return loadUserByEmail(email);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new UsernameNotFoundException(
                "No account found for email: " + email));
        return new CustomUserDetails(user);
    }

    @Transactional(readOnly = true)
    public Page<User> getUsersPaginated(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<User> getUsersByRolePaginated(String roleName, Pageable pageable) {
        return userRepository.findByRoles_NameIgnoreCase(roleName, pageable);
    }

    @Transactional
    public void delete(UUID id) {
        patientDetailsService.unassignNurse(id);
        vitalsRepository.deleteByPatientId(id);
        taskRepository.deleteByPatientId(id);
        taskRepository.deleteByClaimerId(id);
        appointmentsRepository.deleteByPatientId(id);
        appointmentsRepository.deleteByNurseId(id);
        rosterRepository.deleteByNurseId(id);
        rosterRepository.deleteByPatientId(id);
        careNotesRepository.deleteByPatientId(id);
        careNotesRepository.deleteByNurseId(id);
        clinicalLogRepository.deleteByPatientId(id);
        clinicalLogRepository.deleteByNurseId(id);
        healthStatusRepository.deleteByPatientId(id);
        prescriptionRepository.deleteByPatientId(id);
        prescriptionRepository.deleteByNurseId(id);

        userRepository.deleteById(id);
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
