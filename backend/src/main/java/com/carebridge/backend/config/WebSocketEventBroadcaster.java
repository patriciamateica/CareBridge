package com.carebridge.backend.config;

import com.carebridge.backend.appointments.AppointmentsService;
import com.carebridge.backend.carenotes.CareNotesService;
import com.carebridge.backend.clinicalLog.ClinicalLogService;
import com.carebridge.backend.healthStatus.HealthStatusService;
import com.carebridge.backend.nursedetails.NurseDetailsService;
import com.carebridge.backend.patientDetails.PatientDetailsService;
import com.carebridge.backend.prescription.PrescriptionService;
import com.carebridge.backend.roster.RosterService;
import com.carebridge.backend.task.TaskService;
import com.carebridge.backend.user.UserService;
import com.carebridge.backend.vitals.VitalsService;
import jakarta.annotation.PostConstruct;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketEventBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    private final VitalsService vitalsService;
    private final UserService userService;
    private final TaskService taskService;
    private final RosterService rosterService;
    private final PrescriptionService prescriptionService;
    private final PatientDetailsService patientDetailsService;
    private final NurseDetailsService nurseDetailsService;
    private final HealthStatusService healthStatusService;
    private final ClinicalLogService clinicalLogService;
    private final CareNotesService careNotesService;
    private final AppointmentsService appointmentsService;

    public WebSocketEventBroadcaster(
            SimpMessagingTemplate messagingTemplate,
            VitalsService vitalsService,
            UserService userService,
            TaskService taskService,
            RosterService rosterService,
            PrescriptionService prescriptionService,
            PatientDetailsService patientDetailsService,
            NurseDetailsService nurseDetailsService,
            HealthStatusService healthStatusService,
            ClinicalLogService clinicalLogService,
            CareNotesService careNotesService,
            AppointmentsService appointmentsService) {
        this.messagingTemplate = messagingTemplate;
        this.vitalsService = vitalsService;
        this.userService = userService;
        this.taskService = taskService;
        this.rosterService = rosterService;
        this.prescriptionService = prescriptionService;
        this.patientDetailsService = patientDetailsService;
        this.nurseDetailsService = nurseDetailsService;
        this.healthStatusService = healthStatusService;
        this.clinicalLogService = clinicalLogService;
        this.careNotesService = careNotesService;
        this.appointmentsService = appointmentsService;
    }

    @PostConstruct
    public void init() {
        vitalsService.getVitalsStream(null).subscribe(vitals -> {
            messagingTemplate.convertAndSend("/topic/vitals", vitals);
        });

        userService.getUserRegistrationStream().subscribe(user -> {
            messagingTemplate.convertAndSend("/topic/users", user);
        });
        userService.getUserStatusStream(null).subscribe(user -> {
            messagingTemplate.convertAndSend("/topic/users/status", user);
        });

        taskService.getNewTaskStream(null).subscribe(task -> {
            messagingTemplate.convertAndSend("/topic/tasks", task);
        });

        rosterService.getUpdateStream(null).subscribe(roster -> {
            messagingTemplate.convertAndSend("/topic/rosters", roster);
        });

        prescriptionService.getCreatedStream(null).subscribe(prescription -> {
            messagingTemplate.convertAndSend("/topic/prescriptions/created", prescription);
        });
        prescriptionService.getDeletedStream(null).subscribe(uuid -> {
            messagingTemplate.convertAndSend("/topic/prescriptions/deleted", uuid);
        });

        patientDetailsService.getCreatedStream(null).subscribe(details -> {
            messagingTemplate.convertAndSend("/topic/patient-details", details);
        });
        patientDetailsService.getDiagnosisStream(null).subscribe(details -> {
            messagingTemplate.convertAndSend("/topic/patient-details/diagnosis", details);
        });

        nurseDetailsService.getCreatedStream(null).subscribe(details -> {
            messagingTemplate.convertAndSend("/topic/nurse-details/created", details);
        });
        nurseDetailsService.getHireStatusStream(null).subscribe(details -> {
            messagingTemplate.convertAndSend("/topic/nurse-details/status", details);
        });

        healthStatusService.getStatusStream(null).subscribe(status -> {
            messagingTemplate.convertAndSend("/topic/health-status", status);
        });

        clinicalLogService.getLogStream(null).subscribe(log -> {
            messagingTemplate.convertAndSend("/topic/clinical-logs", log);
        });

        careNotesService.getNoteStream(null).subscribe(note -> {
            messagingTemplate.convertAndSend("/topic/care-notes", note);
        });

        appointmentsService.getScheduledStream(null, null).subscribe(appointment -> {
            messagingTemplate.convertAndSend("/topic/appointments/scheduled", appointment);
        });
        appointmentsService.getStatusChangedStream(null).subscribe(appointment -> {
            messagingTemplate.convertAndSend("/topic/appointments/status", appointment);
        });
    }
}
