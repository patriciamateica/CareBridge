//package com.carebridge.backend.oldgraphqls;
//
//import com.carebridge.backend.appointments.AppointmentsService;
//import com.carebridge.backend.appointments.model.Appointments;
//import com.carebridge.backend.appointments.model.AppointmentsStatus;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.graphql.data.method.annotation.Argument;
//import org.springframework.graphql.data.method.annotation.MutationMapping;
//import org.springframework.graphql.data.method.annotation.QueryMapping;
//import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
//import org.springframework.stereotype.Controller;
//import reactor.core.publisher.Flux;
//
//import java.time.LocalDateTime;
//import java.time.OffsetDateTime;
//import java.util.List;
//import java.util.UUID;
//
//@Controller
//public class AppointmentsGraphController {
//
//    private final AppointmentsService appointmentsService;
//
//    public AppointmentsGraphController(AppointmentsService appointmentsService) {
//        this.appointmentsService = appointmentsService;
//    }
//
//    @QueryMapping
//    public List<Appointments> getAppointments(@Argument int page, @Argument int size) {
//        return appointmentsService.findAll(PageRequest.of(page, size)).getContent();
//    }
//
//    @QueryMapping
//    public Appointments getAppointmentById(@Argument UUID id) {
//        return appointmentsService.getById(id);
//    }
//
//    @QueryMapping
//    public List<Appointments> getAppointmentsByPatientId(@Argument UUID patientId) {
//        return appointmentsService.getByPatientId(patientId);
//    }
//
//    @MutationMapping
//    public Appointments createAppointment(
//            @Argument UUID patientId, @Argument UUID nurseId,
//            @Argument String description, @Argument String timeSlot,
//            @Argument AppointmentsStatus status) {
//
//        Appointments appointment = new Appointments(
//                UUID.randomUUID(), patientId, nurseId, description,
//                parseDateTime(timeSlot), status);
//        return appointmentsService.create(appointment);
//    }
//
//    @MutationMapping
//    public Appointments updateAppointmentStatus(@Argument UUID id, @Argument AppointmentsStatus status) {
//        return appointmentsService.updateStatus(id, status);
//    }
//
//    @MutationMapping
//    public Boolean deleteAppointment(@Argument UUID id) {
//        return appointmentsService.delete(id);
//    }
//
//    @SubscriptionMapping
//    public Flux<Appointments> onAppointmentScheduled(@Argument UUID patientId, @Argument UUID nurseId) {
//        return appointmentsService.getScheduledStream(patientId, nurseId);
//    }
//
//    @SubscriptionMapping
//    public Flux<Appointments> onAppointmentStatusChanged(@Argument UUID id) {
//        return appointmentsService.getStatusChangedStream(id);
//    }
//
//    private LocalDateTime parseDateTime(String rawDateTime) {
//        try {
//            return LocalDateTime.parse(rawDateTime);
//        } catch (Exception ignored) {
//            return OffsetDateTime.parse(rawDateTime).toLocalDateTime();
//        }
//    }
//}
