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
import com.carebridge.backend.task.TaskService;
import com.carebridge.backend.task.model.Task;
import com.carebridge.backend.task.model.TaskStatus;
import com.carebridge.backend.task.model.TaskType;
import com.carebridge.backend.user.UserRepository;
import com.carebridge.backend.user.Role;
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
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class SeederService {

    private final AppointmentsService appointmentsService;
    private final CareNotesService careNotesService;
    private final ClinicalLogService clinicalLogService;
    private final HealthStatusService healthStatusService;
    private final TaskService taskService;
    private final VitalsService vitalsService;
    private final UserRepository userRepository;
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
        this.prescriptionService = prescriptionService;
        this.patientDetailsService = patientDetailsService;
        this.nurseDetailsService = nurseDetailsService;
        this.rosterService = rosterService;
    }


    public void seedRepository() {
        ensureUsersExist();
        for (int i = 0; i < 10; i++) {
            generateSingleBatch();
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
    }

    public synchronized void startLoop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::generateSingleBatch, 0, 5, TimeUnit.SECONDS);
    }

    public synchronized void stopLoop() {
        if (scheduler != null) {
            scheduler.shutdown();
            scheduler = null;
        }
    }

    private void ensureUsersExist() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            User patient = new User(UUID.randomUUID(), faker.idNumber().valid(), faker.name().firstName(), faker.name().lastName(), faker.internet().emailAddress(), "password123", 1234567890, Role.PATIENT);
            User nurse = new User(UUID.randomUUID(), faker.idNumber().valid(), faker.name().firstName(), faker.name().lastName(), faker.internet().emailAddress(), "password123", 1234567890, Role.NURSE);
            userRepository.save(patient);
            userRepository.save(nurse);
        }
    }

    private void generateSingleBatch() {
        ensureUsersExist();
        List<User> users = userRepository.findAll();
        UUID patientId = users.get(random.nextInt(users.size())).getId();
        UUID nurseId = users.get(random.nextInt(users.size())).getId();

        vitalsService.create(new Vitals(UUID.randomUUID(), LocalDate.now(), random.nextInt(60, 100), random.nextInt(90, 140), random.nextInt(12, 20), random.nextInt(95, 100), patientId));
        taskService.create(new Task(UUID.randomUUID(), faker.lorem().sentence(3), faker.lorem().paragraph(), TaskType.values()[random.nextInt(TaskType.values().length)], LocalDateTime.now().plusHours(random.nextInt(1, 24)), TaskStatus.OPEN, patientId, null));
        appointmentsService.create(new Appointments(UUID.randomUUID(), patientId, nurseId, faker.lorem().sentence(), LocalDateTime.now().plusDays(random.nextInt(1, 7)), AppointmentsStatus.REQUESTED));
        healthStatusService.create(new HealthStatus(UUID.randomUUID(), random.nextInt(0, 10), Mood.values()[random.nextInt(Mood.values().length)], List.of("Fever", "Cough", "Fatigue"), faker.lorem().sentence(), LocalDate.now(), patientId));
        clinicalLogService.create(new ClinicalLog(UUID.randomUUID(), faker.medical().medicineName() + " Report", DocumentType.values()[random.nextInt(DocumentType.values().length)], LocalDate.now(), faker.internet().url(), patientId, nurseId, LocalDateTime.now(), ClinicalLogStatus.ACTIVE));
        careNotesService.create(new CareNotes(UUID.randomUUID(), faker.lorem().paragraph(), patientId, nurseId, LocalDateTime.now()));
        prescriptionService.create(new Prescription(UUID.randomUUID(), faker.medical().medicineName(), random.nextInt(100, 500) + "mg", "Twice a day", patientId, nurseId));
        rosterService.create(new Roster(UUID.randomUUID(), patientId, nurseId, RosterStatus.ACTIVE));
        patientDetailsService.create(new PatientDetails(UUID.randomUUID(), patientId, faker.medical().diseaseName(), List.of(faker.medical().symptoms()), List.of("scan_1.png"), faker.phoneNumber().phoneNumber(), nurseId));
        nurseDetailsService.create(new NurseDetails(UUID.randomUUID(), nurseId, "Emergency", faker.medical().hospitalName(), random.nextInt(1, 20), random.nextBoolean()));
    }
}
