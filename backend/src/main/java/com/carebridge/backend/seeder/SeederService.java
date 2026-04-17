package com.carebridge.backend.seeder;

import jakarta.annotation.PostConstruct;
import com.carebridge.backend.appointments.AppointmentsService;
import com.carebridge.backend.appointments.model.Appointments;
import com.carebridge.backend.appointments.model.AppointmentsStatus;
import com.carebridge.backend.carenotes.CareNotesService;
import com.carebridge.backend.carenotes.model.CareNotes;
import com.carebridge.backend.clinicalLog.ClinicalLogService;
import com.carebridge.backend.clinicalLog.model.ClinicalLog;
import com.carebridge.backend.clinicalLog.model.ClinicalLogStatus;
import com.carebridge.backend.clinicalLog.model.DocumentType;
import com.carebridge.backend.healthStatus.HealthStatusService;
import com.carebridge.backend.healthStatus.model.HealthStatus;
import com.carebridge.backend.healthStatus.model.Mood;
import com.carebridge.backend.task.TaskService;
import com.carebridge.backend.task.model.Task;
import com.carebridge.backend.task.model.TaskStatus;
import com.carebridge.backend.task.model.TaskType;
import com.carebridge.backend.user.UserRepository;
import com.carebridge.backend.user.Role;
import com.carebridge.backend.user.UserStatus;
import com.carebridge.backend.user.model.User;
import com.carebridge.backend.vitals.VitalsService;
import com.carebridge.backend.vitals.model.Vitals;

import com.carebridge.backend.prescription.PrescriptionService;
import com.carebridge.backend.prescription.model.Prescription;
import com.carebridge.backend.patientDetails.PatientDetailsService;
import com.carebridge.backend.patientDetails.model.PatientDetails;
import com.carebridge.backend.nursedetails.NurseDetailsService;
import com.carebridge.backend.nursedetails.model.NurseDetails;
import com.carebridge.backend.roster.RosterService;
import com.carebridge.backend.roster.model.Roster;
import com.carebridge.backend.roster.model.RosterStatus;

import com.github.javafaker.Faker;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class SeederService {

    private static final int MIN_NURSE_USERS = 6;
    private static final int PATIENTS_PER_SEED = 15;
    private static final String ADMIN_EMAIL = "admin@carebridge.local";
    private static final String ADMIN_PASSWORD = "Admin123!";
    private static final String ADMIN_FIRST_NAME = "System";
    private static final String ADMIN_LAST_NAME = "Admin";

    private final AppointmentsService appointmentsService;
    private final CareNotesService careNotesService;
    private final ClinicalLogService clinicalLogService;
    private final HealthStatusService healthStatusService;
    private final TaskService taskService;
    private final VitalsService vitalsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PrescriptionService prescriptionService;
    private final PatientDetailsService patientDetailsService;
    private final NurseDetailsService nurseDetailsService;
    private final RosterService rosterService;

    private final Faker faker = new Faker();
    private final Random random = new Random();

    private ScheduledExecutorService scheduler;

    public SeederService(
        AppointmentsService appointmentsService, CareNotesService careNotesService,
        ClinicalLogService clinicalLogService, HealthStatusService healthStatusService,
        TaskService taskService, VitalsService vitalsService, UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        PrescriptionService prescriptionService, PatientDetailsService patientDetailsService,
        NurseDetailsService nurseDetailsService, RosterService rosterService
    ) {
        this.appointmentsService = appointmentsService;
        this.careNotesService = careNotesService;
        this.clinicalLogService = clinicalLogService;
        this.healthStatusService = healthStatusService;
        this.taskService = taskService;
        this.vitalsService = vitalsService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.prescriptionService = prescriptionService;
        this.patientDetailsService = patientDetailsService;
        this.nurseDetailsService = nurseDetailsService;
        this.rosterService = rosterService;
    }

    @PostConstruct
    public void initAdminUser() {
        ensureAdminUser();
    }


    public void seedRepository() {
        ensureNursesExist();

        List<User> nurseUsers = getNurseUsers();
        if (nurseUsers.isEmpty()) {
            return;
        }

        for (int i = 0; i < PATIENTS_PER_SEED; i++) {
            User patient = userRepository.save(buildUser(Role.PATIENT));
            User assignedNurse = nurseUsers.get(i % nurseUsers.size());
            createPatientBundle(patient.getId(), assignedNurse.getId());
        }
    }

    public void unseedRepository() {
        appointmentsService.deleteAll();
        careNotesService.deleteAll();
        clinicalLogService.deleteAll();
        healthStatusService.deleteAll();
        taskService.deleteAll();
        vitalsService.deleteAll();
        prescriptionService.deleteAll();
        patientDetailsService.deleteAll();
        nurseDetailsService.deleteAll();
        rosterService.deleteAll();
        userRepository.deleteAll();
        ensureAdminUser();
    }

    public synchronized void startLoop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::generateLiveUpdateForExistingPatient, 0, 5, TimeUnit.SECONDS);
    }

    public synchronized void stopLoop() {
        if (scheduler != null) {
            scheduler.shutdown();
            scheduler = null;
        }
    }

    private void ensureNursesExist() {
        List<User> users = userRepository.findAll();
        long nurseCount = users.stream().filter(user -> user.getRole() == Role.NURSE).count();

        for (int i = (int) nurseCount; i < MIN_NURSE_USERS; i++) {
            userRepository.save(buildUser(Role.NURSE));
        }
    }

    private void generateLiveUpdateForExistingPatient() {
        List<Roster> rosters = rosterService.findAll(PageRequest.of(0, 1000)).getContent();
        if (rosters.isEmpty()) {
            seedRepository();
            rosters = rosterService.findAll(PageRequest.of(0, 1000)).getContent();
        }

        if (rosters.isEmpty()) {
            return;
        }

        Roster target = rosters.get(random.nextInt(rosters.size()));
        UUID patientId = target.getPatientId();
        UUID nurseId = target.getNurseId();

        vitalsService.create(new Vitals(UUID.randomUUID(), LocalDate.now(), random.nextInt(60, 100), random.nextInt(90, 140), random.nextInt(12, 20), random.nextInt(95, 100), patientId));
        taskService.create(new Task(UUID.randomUUID(), faker.lorem().sentence(3), faker.lorem().paragraph(), TaskType.values()[random.nextInt(TaskType.values().length)], LocalDateTime.now().plusHours(random.nextInt(1, 24)), TaskStatus.OPEN, patientId, null));
        appointmentsService.create(new Appointments(UUID.randomUUID(), patientId, nurseId, faker.lorem().sentence(), LocalDateTime.now().plusDays(random.nextInt(1, 7)), AppointmentsStatus.REQUESTED));
        healthStatusService.create(new HealthStatus(UUID.randomUUID(), random.nextInt(0, 10), Mood.values()[random.nextInt(Mood.values().length)], List.of("Fever", "Cough", "Fatigue"), faker.lorem().sentence(), LocalDate.now(), patientId));
        clinicalLogService.create(new ClinicalLog(UUID.randomUUID(), faker.medical().medicineName() + " Report", DocumentType.values()[random.nextInt(DocumentType.values().length)], LocalDate.now(), faker.internet().url(), patientId, nurseId, LocalDateTime.now(), ClinicalLogStatus.ACTIVE));
        careNotesService.create(new CareNotes(UUID.randomUUID(), faker.lorem().paragraph(), patientId, nurseId, LocalDateTime.now()));
        prescriptionService.create(new Prescription(UUID.randomUUID(), faker.medical().medicineName(), random.nextInt(100, 500) + "mg", "Twice a day", patientId, nurseId));
    }

    private void createPatientBundle(UUID patientId, UUID nurseId) {
        vitalsService.create(new Vitals(UUID.randomUUID(), LocalDate.now(), random.nextInt(60, 100), random.nextInt(90, 140), random.nextInt(12, 20), random.nextInt(95, 100), patientId));
        taskService.create(new Task(UUID.randomUUID(), faker.lorem().sentence(3), faker.lorem().paragraph(), TaskType.values()[random.nextInt(TaskType.values().length)], LocalDateTime.now().plusHours(random.nextInt(1, 24)), TaskStatus.OPEN, patientId, null));
        appointmentsService.create(new Appointments(UUID.randomUUID(), patientId, nurseId, faker.lorem().sentence(), LocalDateTime.now().plusDays(random.nextInt(1, 7)), AppointmentsStatus.REQUESTED));
        healthStatusService.create(new HealthStatus(UUID.randomUUID(), random.nextInt(0, 10), Mood.values()[random.nextInt(Mood.values().length)], List.of("Fever", "Cough", "Fatigue"), faker.lorem().sentence(), LocalDate.now(), patientId));
        clinicalLogService.create(new ClinicalLog(UUID.randomUUID(), faker.medical().medicineName() + " Report", DocumentType.values()[random.nextInt(DocumentType.values().length)], LocalDate.now(), faker.internet().url(), patientId, nurseId, LocalDateTime.now(), ClinicalLogStatus.ACTIVE));
        careNotesService.create(new CareNotes(UUID.randomUUID(), faker.lorem().paragraph(), patientId, nurseId, LocalDateTime.now()));
        prescriptionService.create(new Prescription(UUID.randomUUID(), faker.medical().medicineName(), random.nextInt(100, 500) + "mg", "Twice a day", patientId, nurseId));
        rosterService.create(new Roster(UUID.randomUUID(), patientId, nurseId, RosterStatus.ACTIVE));
        patientDetailsService.create(new PatientDetails(UUID.randomUUID(), patientId, faker.medical().diseaseName(), List.of(faker.medical().symptoms()), List.of("scan_1.png"), faker.phoneNumber().phoneNumber(), nurseId));
        ensureNurseDetailsExists(nurseId);
    }

    private void ensureNurseDetailsExists(UUID nurseId) {
        boolean exists = nurseDetailsService.findAll(PageRequest.of(0, 1000)).getContent().stream()
            .anyMatch(details -> details.getUserId().equals(nurseId));
        if (!exists) {
            nurseDetailsService.create(new NurseDetails(UUID.randomUUID(), nurseId, "Emergency", faker.medical().hospitalName(), random.nextInt(1, 20), random.nextBoolean()));
        }
    }

    private List<User> getNurseUsers() {
        return new ArrayList<>(userRepository.findAll().stream().filter(user -> user.getRole() == Role.NURSE).toList());
    }

    private User buildUser(Role role) {
        return new User(
            UUID.randomUUID(),
            faker.idNumber().valid(),
            faker.name().firstName(),
            faker.name().lastName(),
            faker.internet().emailAddress(),
            "password123",
            Integer.parseInt(faker.number().digits(9)),
            role
        );
    }

    private void ensureAdminUser() {
        if (userRepository.findByEmail(ADMIN_EMAIL).isPresent()) {
            return;
        }

        User admin = new User();
        admin.setFirstName(ADMIN_FIRST_NAME);
        admin.setLastName(ADMIN_LAST_NAME);
        admin.setEmail(ADMIN_EMAIL);
        admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setPhoneNumber(1000000000);
        admin.setRole(Role.ADMIN);
        admin.setUserStatus(UserStatus.ACTIVE);
        userRepository.save(admin);
    }
}
