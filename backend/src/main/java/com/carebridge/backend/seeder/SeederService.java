package com.carebridge.backend.seeder;

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
import com.carebridge.backend.nursedetails.NurseDetailsService;
import com.carebridge.backend.nursedetails.model.NurseDetails;
import com.carebridge.backend.patientDetails.PatientDetailsService;
import com.carebridge.backend.patientDetails.model.PatientDetails;
import com.carebridge.backend.prescription.PrescriptionService;
import com.carebridge.backend.prescription.model.Prescription;
import com.carebridge.backend.roster.RosterService;
import com.carebridge.backend.roster.model.Roster;
import com.carebridge.backend.roster.model.RosterStatus;
import com.carebridge.backend.task.TaskService;
import com.carebridge.backend.task.model.Task;
import com.carebridge.backend.task.model.TaskStatus;
import com.carebridge.backend.task.model.TaskType;
import com.carebridge.backend.user.Role;
import com.carebridge.backend.user.UserRepository;
import com.carebridge.backend.user.UserService;
import com.carebridge.backend.user.UserStatus;
import com.carebridge.backend.user.model.User;
import com.carebridge.backend.vitals.VitalsService;
import com.carebridge.backend.vitals.model.Vitals;
import com.github.javafaker.Faker;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class SeederService {

    private final AppointmentsService appointmentsService;
    private final CareNotesService careNotesService;
    private final ClinicalLogService clinicalLogService;
    private final HealthStatusService healthStatusService;
    private final TaskService taskService;
    private final VitalsService vitalsService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final PrescriptionService prescriptionService;
    private final PatientDetailsService patientDetailsService;
    private final NurseDetailsService nurseDetailsService;
    private final RosterService rosterService;

    private final Faker faker = new Faker(new Random(123));
    private final Random random = new Random(123);

    private ScheduledExecutorService scheduler;
    private static final int LIVE_UPDATE_INTERVAL_SECONDS = 5;

    public SeederService(
        AppointmentsService appointmentsService, CareNotesService careNotesService,
        ClinicalLogService clinicalLogService, HealthStatusService healthStatusService,
        TaskService taskService, VitalsService vitalsService, UserRepository userRepository,
        UserService userService, PasswordEncoder passwordEncoder,
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
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.prescriptionService = prescriptionService;
        this.patientDetailsService = patientDetailsService;
        this.nurseDetailsService = nurseDetailsService;
        this.rosterService = rosterService;
    }

    public void seedDatabase() {
        if (!userRepository.findAll().isEmpty()) {
            return;
        }

        seedUsersTable();
        seedNurseDetailsTable();
        seedPatientDetailsTable();
        seedRosterTable();

        seedVitalsTable();
        seedTasksTable();
        seedAppointmentsTable();
        seedHealthStatusTable();
        seedClinicalLogTable();
        seedCareNotesTable();
        seedPrescriptionsTable();
    }

    public void clearDatabase() {
        appointmentsService.deleteAll();
        careNotesService.deleteAll();
        clinicalLogService.deleteAll();
        healthStatusService.deleteAll();
        taskService.deleteAll();
        vitalsService.deleteAll();
        prescriptionService.deleteAll();
        rosterService.deleteAll();
        patientDetailsService.deleteAll();
        nurseDetailsService.deleteAll();
        userRepository.deleteAll();
    }

    private void seedUsersTable() {
        createUser("admin@carebridge.local", Role.ADMIN);
        createUser("nurse@carebridge.local", Role.NURSE);
        createUser("family@carebridge.local", Role.FAMILY);

        for (int i = 1; i <= 10; i++) {
            createUser("patient" + i + "@carebridge.local", Role.PATIENT);
        }
    }

    private void seedNurseDetailsTable() {
        User nurse = userRepository.findByEmailIgnoreCase("nurse@carebridge.local").orElseThrow();

        NurseDetails details = new NurseDetails();
        details.setUser(nurse); // Updated to pass User object
        details.setSpecialization("Geriatrics");
        details.setHospitalAffiliation(faker.medical().hospitalName());
        details.setExperienceYears(random.nextInt(5, 20));
        details.setHireMeStatus(true);
        nurseDetailsService.create(details);
    }

    private void seedPatientDetailsTable() {
        List<User> patients = getByRole(Role.PATIENT);
        User nurse = userRepository.findByEmailIgnoreCase("nurse@carebridge.local").orElseThrow();

        for (User patient : patients) {
            PatientDetails pd = new PatientDetails();
            pd.setUser(patient);
            pd.setPrimaryDiagnosis(faker.medical().diseaseName());
            pd.setDiagnostics(List.of(faker.medical().symptoms()));
            pd.setScans(List.of("scan_baseline.png"));
            pd.setEmergencyContact(faker.phoneNumber().phoneNumber());
            pd.setAssignedNurseId(nurse.getId());

            patientDetailsService.create(pd);
        }
    }

    private void seedRosterTable() {
        List<User> patients = getByRole(Role.PATIENT);
        User nurse = userRepository.findByEmailIgnoreCase("nurse@carebridge.local").orElseThrow();

        for (User patient : patients) {
            Roster roster = new Roster();
            roster.setPatient(patient);
            roster.setNurse(nurse);
            roster.setStatus(RosterStatus.ACTIVE);
            rosterService.create(roster);
        }
    }

    private void seedVitalsTable() {
        List<User> patients = getByRole(Role.PATIENT);
        for (int i = 0; i < 15; i++) {
            User patient = patients.get(random.nextInt(patients.size()));
            vitalsService.create(new Vitals(null,
                LocalDate.now().minusDays(random.nextInt(10)),
                random.nextInt(60, 100),
                random.nextInt(90, 140),
                random.nextInt(12, 20),
                random.nextInt(95, 100),
                patient));
        }
    }

    private void seedTasksTable() {
        List<User> patients = getByRole(Role.PATIENT);
        for (int i = 0; i < 15; i++) {
            User patient = patients.get(random.nextInt(patients.size()));
            taskService.create(new Task(null,
                truncate(faker.lorem().sentence(3)),
                truncate(faker.lorem().paragraph()),
                TaskType.values()[random.nextInt(TaskType.values().length)],
                LocalDateTime.now().plusHours(random.nextInt(1, 48)),
                TaskStatus.OPEN,
                patient,
                null));
        }
    }

    private void seedAppointmentsTable() {
        List<User> patients = getByRole(Role.PATIENT);
        User nurse = userRepository.findByEmailIgnoreCase("nurse@carebridge.local").orElseThrow();
        for (int i = 0; i < 10; i++) {
            User patient = patients.get(random.nextInt(patients.size()));
            appointmentsService.create(new Appointments(null,
                patient,
                nurse,
                truncate(faker.lorem().sentence()),
                LocalDateTime.now().plusDays(random.nextInt(1, 14)),
                AppointmentsStatus.REQUESTED));
        }
    }

    private void seedHealthStatusTable() {
        List<User> patients = getByRole(Role.PATIENT);
        for (int i = 0; i < 15; i++) {
            User patient = patients.get(random.nextInt(patients.size()));
            healthStatusService.create(new HealthStatus(null,
                random.nextInt(0, 10),
                Mood.values()[random.nextInt(Mood.values().length)],
                List.of("Fatigue", "Headache"),
                truncate(faker.lorem().sentence()),
                LocalDate.now().minusDays(random.nextInt(5)),
                patient));
        }
    }

    private void seedClinicalLogTable() {
        List<User> patients = getByRole(Role.PATIENT);
        User nurse = userRepository.findByEmailIgnoreCase("nurse@carebridge.local").orElseThrow();
        for (int i = 0; i < 10; i++) {
            User patient = patients.get(random.nextInt(patients.size()));
            clinicalLogService.create(new ClinicalLog(null,
                truncate(faker.medical().medicineName() + " Report"),
                DocumentType.values()[random.nextInt(DocumentType.values().length)],
                LocalDate.now(),
                "https://example.com/log",
                patient,
                nurse,
                LocalDateTime.now(),
                ClinicalLogStatus.ACTIVE));
        }
    }

    private void seedCareNotesTable() {
        List<User> patients = getByRole(Role.PATIENT);
        User nurse = userRepository.findByEmailIgnoreCase("nurse@carebridge.local").orElseThrow();
        for (int i = 0; i < 15; i++) {
            User patient = patients.get(random.nextInt(patients.size()));
            careNotesService.create(new CareNotes(null,
                truncate(faker.lorem().paragraph()),
                patient,
                nurse,
                LocalDateTime.now().minusHours(random.nextInt(48))));
        }
    }

    private void seedPrescriptionsTable() {
        List<User> patients = getByRole(Role.PATIENT);
        User nurse = userRepository.findByEmailIgnoreCase("nurse@carebridge.local").orElseThrow();
        for (int i = 0; i < 15; i++) {
            User patient = patients.get(random.nextInt(patients.size()));
            prescriptionService.create(new Prescription(null, truncate(faker.medical().medicineName()),
                random.nextInt(100, 500) + "mg", "Twice daily", patient, nurse));
        }
    }

    private void createUser(String email, Role role) {
        User user = new User();
        user.setFirstName(faker.name().firstName());
        user.setLastName(faker.name().lastName());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("Password123!"));
        user.setPhoneNumber(Integer.parseInt(faker.number().digits(9)));
        user.setRole(role);
        user.setUserStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);
        userService.emitRegistration(savedUser);
    }

    private List<User> getByRole(Role role) {
        return userRepository.findAll().stream().filter(u -> u.getRole() == role).toList();
    }

    private String truncate(String text) {
        if (text == null) return null;
        return text.length() > 250 ? text.substring(0, 245) + "..." : text;
    }


    public synchronized void startLoop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::runLiveUpdateTick, LIVE_UPDATE_INTERVAL_SECONDS,
            LIVE_UPDATE_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    public synchronized void stopLoop() {
        if (scheduler != null) {
            scheduler.shutdown();
            scheduler = null;
        }
    }

    private void runLiveUpdateTick() {
        List<User> patients = getByRole(Role.PATIENT);
        User nurse = userRepository.findByEmailIgnoreCase("nurse@carebridge.local").orElse(null);

        if (patients.isEmpty() || nurse == null) return;

        User randomPatient = patients.get(random.nextInt(patients.size()));

        vitalsService.create(new Vitals(null,
            LocalDate.now(),
            random.nextInt(60, 100),
            random.nextInt(90, 140),
            random.nextInt(12, 20),
            random.nextInt(95, 100),
            randomPatient));
        taskService.create(new Task(null,
            truncate(faker.lorem().sentence(3)),
            truncate(faker.lorem().paragraph()),
            TaskType.values()[random.nextInt(TaskType.values().length)],
            LocalDateTime.now().plusHours(24),
            TaskStatus.OPEN,
            randomPatient,
            null));
        careNotesService.create(new CareNotes(null,
            truncate(faker.lorem().paragraph()),
            randomPatient,
            nurse,
            LocalDateTime.now()));
    }
}
