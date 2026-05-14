package com.carebridge.backend.seeder;

import com.carebridge.backend.appointments.AppointmentsService;
import com.carebridge.backend.appointments.model.Appointments;
import com.carebridge.backend.appointments.model.AppointmentsStatus;
import com.carebridge.backend.audit.AuditLogRepository;
import com.carebridge.backend.audit.SuspiciousUserRepository;
import com.carebridge.backend.audit.model.AuditLog;
import com.carebridge.backend.audit.model.SuspiciousUser;
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
import com.carebridge.backend.user.model.Permission;
import com.carebridge.backend.user.model.Role;
import com.carebridge.backend.user.PermissionRepository;
import com.carebridge.backend.user.RoleRepository;
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
import java.util.ArrayList;
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
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AuditLogRepository auditLogRepository;
    private final SuspiciousUserRepository suspiciousUserRepository;

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
        NurseDetailsService nurseDetailsService, RosterService rosterService,
        RoleRepository roleRepository, PermissionRepository permissionRepository,
        AuditLogRepository auditLogRepository, SuspiciousUserRepository suspiciousUserRepository
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
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.auditLogRepository = auditLogRepository;
        this.suspiciousUserRepository = suspiciousUserRepository;
    }

    public synchronized void seedDatabase() {
        if (!userRepository.findAll().isEmpty()) {
            return;
        }

        seedRolesAndPermissions();
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

        seedAuditLogsTable();
        seedSuspiciousUsersTable();
    }

    public synchronized void seedRolesAndPermissionsOnly() {
        List<User> allUsers = userRepository.findAll();
        allUsers.forEach(user -> {
            user.getRoles().clear();
            userRepository.save(user);
        });
        userRepository.flush();

        roleRepository.deleteAll();
        roleRepository.flush();
        permissionRepository.deleteAll();
        permissionRepository.flush();

        seedRolesAndPermissions();

        allUsers.forEach(user -> {
            String email = user.getEmail().toLowerCase();
            String roleName = "PATIENT";
            if (email.contains("admin")) roleName = "ADMIN";
            else if (email.contains("nurse")) roleName = "NURSE";
            else if (email.contains("family")) roleName = "FAMILY";
            roleRepository.findByName(roleName).ifPresent(user::addRole);
            userRepository.save(user);
        });
    }

    public synchronized void clearDatabase() {
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
        auditLogRepository.deleteAll();
        suspiciousUserRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
    }

    private void seedUsersTable() {
        createUser("admin@carebridge.local", "ADMIN");
        createUser("nurse@carebridge.local", "NURSE");
        createUser("family@carebridge.local", "FAMILY");

        for (int i = 1; i <= 20; i++) {
            createUser("patient" + i + "@carebridge.local", "PATIENT");
        }

        createUser("test-patient1@carebridge.local", "PATIENT");
        createUser("test-patient2@carebridge.local", "PATIENT");
        createUser("test-patient3@carebridge.local", "PATIENT");
    }

    private void seedRolesAndPermissions() {
        Permission viewHomeNurse = permissionRepository.save(new Permission("VIEW_HOME_NURSE"));
        Permission viewHomePatient = permissionRepository.save(new Permission("VIEW_HOME_PATIENT"));
        Permission viewAppointments = permissionRepository.save(new Permission("VIEW_APPOINTMENTS"));
        Permission viewChat = permissionRepository.save(new Permission("VIEW_CHAT"));
        Permission viewPatients = permissionRepository.save(new Permission("VIEW_PATIENTS"));
        Permission viewMedicalRecords = permissionRepository.save(new Permission("VIEW_MEDICAL_RECORDS"));
        Permission viewMedication = permissionRepository.save(new Permission("VIEW_MEDICATION"));
        Permission viewRequests = permissionRepository.save(new Permission("VIEW_REQUESTS"));
        Permission viewCareVillage = permissionRepository.save(new Permission("VIEW_CARE_VILLAGE"));
        Permission viewCareSchedule = permissionRepository.save(new Permission("VIEW_CARE_SCHEDULE"));
        Permission viewAccount = permissionRepository.save(new Permission("VIEW_ACCOUNT"));

        Permission addCrud = permissionRepository.save(new Permission("ADD_ANY_CRUD"));
        Permission updateCrud = permissionRepository.save(new Permission("UPDATE_ANY_CRUD"));

        Permission deletePrescription = permissionRepository.save(new Permission("DELETE_PRESCRIPTION"));
        Permission deleteCareNote = permissionRepository.save(new Permission("DELETE_CARE_NOTE"));
        Permission deleteVillageTask = permissionRepository.save(new Permission("DELETE_VILLAGE_TASK"));
        Permission deleteAppointment = permissionRepository.save(new Permission("DELETE_APPOINTMENT"));

        Permission manageHealthStatus = permissionRepository.save(new Permission("MANAGE_HEALTH_STATUS"));
        Permission manageAppointments = permissionRepository.save(new Permission("MANAGE_APPOINTMENTS"));

        Permission claimTask = permissionRepository.save(new Permission("CLAIM_TASK"));
        Permission manageVillageTasks = permissionRepository.save(new Permission("MANAGE_VILLAGE_TASKS"));

        Role admin = new Role("ADMIN");
        admin.getPermissions().addAll(permissionRepository.findAll());
        roleRepository.save(admin);

        Role nurse = new Role("NURSE");
        nurse.getPermissions().addAll(List.of(
            viewHomeNurse, viewAppointments, viewChat, viewPatients,
            viewMedicalRecords, viewMedication, viewRequests, viewCareVillage,
            addCrud, updateCrud,
            deletePrescription, deleteCareNote, deleteVillageTask, deleteAppointment
        ));
        roleRepository.save(nurse);

        Role patient = new Role("PATIENT");
        patient.getPermissions().addAll(List.of(
            viewHomePatient, viewCareSchedule, viewChat, viewMedicalRecords,
            viewMedication, viewCareVillage, viewAccount,
            manageHealthStatus, manageAppointments
        ));
        roleRepository.save(patient);

        Role family = new Role("FAMILY");
        family.getPermissions().addAll(List.of(
            viewHomePatient, viewCareVillage,
            manageVillageTasks, claimTask
        ));
        roleRepository.save(family);
    }

    private void seedNurseDetailsTable() {
        User nurse = userRepository.findByEmailIgnoreCase("nurse@carebridge.local").orElseThrow();

        NurseDetails details = new NurseDetails();
        details.setUser(nurse);
        details.setSpecialization("Geriatrics");
        details.setHospitalAffiliation(faker.medical().hospitalName());
        details.setExperienceYears(random.nextInt(5, 20));
        details.setHireMeStatus(true);
        nurseDetailsService.create(details);
    }

    private void seedPatientDetailsTable() {
        List<User> allPatients = getByRole("PATIENT");
        User nurse = userRepository.findByEmailIgnoreCase("nurse@carebridge.local").orElseThrow();

        String[] statuses = {"Active", "Active", "Active", "Critical", "Inactive"};
        int i = 0;
        for (User patient : allPatients) {
            PatientDetails pd = new PatientDetails();
            pd.setUser(patient);
            pd.setPrimaryDiagnosis(faker.medical().diseaseName());

            pd.setDiagnostics(new ArrayList<>(List.of(faker.medical().symptoms())));
            pd.setScans(new ArrayList<>(List.of("scan_baseline.png")));

            pd.setEmergencyContact(faker.phoneNumber().phoneNumber());
            pd.setAssignedNurseId(nurse.getId());
            pd.setStatus(statuses[i % statuses.length]);

            patientDetailsService.create(pd);
            i++;
        }
    }

    private void seedRosterTable() {
        List<User> patients = getByRole("PATIENT");
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
        List<User> patients = getByRole("PATIENT");
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
        List<User> patients = getByRole("PATIENT");
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
        List<User> patients = getByRole("PATIENT");
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
        List<User> patients = getByRole("PATIENT");
        for (int i = 0; i < 15; i++) {
            User patient = patients.get(random.nextInt(patients.size()));
            healthStatusService.create(new HealthStatus(null,
                random.nextInt(0, 10),
                Mood.values()[random.nextInt(Mood.values().length)],

                new ArrayList<>(List.of("Fatigue", "Headache")),

                truncate(faker.lorem().sentence()),
                LocalDate.now().minusDays(random.nextInt(5)),
                patient));
        }
    }

    private void seedClinicalLogTable() {
        List<User> patients = getByRole("PATIENT");
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
        List<User> patients = getByRole("PATIENT");
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
        List<User> patients = getByRole("PATIENT");
        User nurse = userRepository.findByEmailIgnoreCase("nurse@carebridge.local").orElseThrow();
        for (int i = 0; i < 15; i++) {
            User patient = patients.get(random.nextInt(patients.size()));
            prescriptionService.create(new Prescription(null, truncate(faker.medical().medicineName()),
                random.nextInt(100, 500) + "mg", "Twice daily", patient, nurse));
        }
    }

    private void createUser(String email, String roleName) {
        User user = new User();
        user.setFirstName(faker.name().firstName());
        user.setLastName(faker.name().lastName());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("Password123!"));
        user.setPhoneNumber(faker.number().digits(9));

        Role role = roleRepository.findByName(roleName).orElseThrow();
        user.addRole(role);

        user.setUserStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);
        userService.emitRegistration(savedUser);
    }

    private List<User> getByRole(String roleName) {
        return userRepository.findAll().stream()
            .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals(roleName)))
            .toList();
    }

    private String truncate(String text) {
        if (text == null) return null;
        return text.length() > 250 ? text.substring(0, 245) + "..." : text;
    }

    // Phase Debugging: Seed audit logs with mock data
    private void seedAuditLogsTable() {
        List<User> allUsers = userRepository.findAll();
        String[] actions = {"Login", "Create Patient", "Update Patient", "Delete Patient", "Create Task", "View Report", "Export Data", "Change Password"};
        String[] resources = {"Patient", "Task", "Report", "Account", "Prescription", "Schedule"};
        String[] statuses = {"SUCCESS", "SUCCESS", "SUCCESS", "FAILURE"};
        String[] ips = {"192.168.1.1", "10.0.0.1", "172.16.0.1", "203.0.113.5", "198.51.100.42"};

        LocalDateTime baseTime = LocalDateTime.now().minusDays(7);
        for (int i = 0; i < 15; i++) {
            User user = allUsers.get(random.nextInt(allUsers.size()));
            AuditLog log = AuditLog.builder()
                .userId(user.getId())
                .username(user.getEmail())
                .userRole(user.getRoles().isEmpty() ? "UNKNOWN" : user.getRoles().iterator().next().getName())
                .action(actions[random.nextInt(actions.length)])
                .resource(resources[random.nextInt(resources.length)])
                .details("Mock audit log entry for debugging purposes")
                .timestamp(baseTime.plusHours(random.nextInt(168)))
                .status(statuses[random.nextInt(statuses.length)])
                .ipAddress(ips[random.nextInt(ips.length)])
                .build();
            auditLogRepository.save(log);
        }
    }

    // Phase Debugging: Seed suspicious users with mock data
    private void seedSuspiciousUsersTable() {
        List<User> allUsers = userRepository.findAll();
        String[] reasons = {"Brute Force Detected", "Mass Data Modification", "Bot Activity Detected", "Unusual Login Pattern"};
        String[] severities = {"LOW", "MEDIUM", "HIGH"};

        if (allUsers.size() > 0) {
            User suspiciousUser1 = allUsers.get(random.nextInt(allUsers.size()));
            SuspiciousUser sus1 = SuspiciousUser.builder()
                .userId(suspiciousUser1.getId())
                .username(suspiciousUser1.getEmail())
                .reason(reasons[random.nextInt(reasons.length)])
                .flaggedAt(LocalDateTime.now().minusDays(2))
                .severity(severities[random.nextInt(severities.length)])
                .resolved(false)
                .build();
            suspiciousUserRepository.save(sus1);
        }

        if (allUsers.size() > 1) {
            User suspiciousUser2 = allUsers.get(random.nextInt(allUsers.size()));
            SuspiciousUser sus2 = SuspiciousUser.builder()
                .userId(suspiciousUser2.getId())
                .username(suspiciousUser2.getEmail())
                .reason(reasons[random.nextInt(reasons.length)])
                .flaggedAt(LocalDateTime.now().minusDays(1))
                .severity(severities[random.nextInt(severities.length)])
                .resolved(false)
                .build();
            suspiciousUserRepository.save(sus2);
        }
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
        List<User> patients = getByRole("PATIENT");
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
