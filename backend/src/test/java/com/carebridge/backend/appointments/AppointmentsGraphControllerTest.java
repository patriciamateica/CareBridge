package com.carebridge.backend.appointments;

import com.carebridge.backend.appointments.model.Appointments;
import com.carebridge.backend.appointments.model.AppointmentsStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@GraphQlTest(AppointmentsGraphController.class)
class AppointmentsGraphControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private AppointmentsService appointmentsService;

    @Test
    void getAppointments_ShouldReturnAppointments() {
        Appointments appointment = new Appointments(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "Desc", LocalDateTime.now(), AppointmentsStatus.REQUESTED);
        when(appointmentsService.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(Collections.singletonList(appointment)));

        String query = """
                query {
                  getAppointments(page: 0, size: 10) {
                    id
                    description
                    status
                  }
                }
                """;

        graphQlTester.document(query)
                .execute()
                .errors()
                .verify()
                .path("getAppointments")
                .entityList(Appointments.class)
                .hasSize(1);
    }

    @Test
    void getAppointmentById_ShouldReturnAppointment() {
        UUID id = UUID.randomUUID();
        Appointments appointment = new Appointments(id, UUID.randomUUID(), UUID.randomUUID(), "Desc", LocalDateTime.now(), AppointmentsStatus.REQUESTED);
        when(appointmentsService.getById(id)).thenReturn(appointment);

        String query = """
                query($id: ID!) {
                  getAppointmentById(id: $id) {
                    id
                    description
                  }
                }
                """;

        graphQlTester.document(query)
                .variable("id", id)
                .execute()
                .errors()
                .verify()
                .path("getAppointmentById")
                .entity(Appointments.class)
                .matches(a -> a.getId().equals(id));
    }

    @Test
    void createAppointment_ShouldReturnCreatedAppointment() {
        UUID patientId = UUID.randomUUID();
        UUID nurseId = UUID.randomUUID();
        Appointments appointment = new Appointments(UUID.randomUUID(), patientId, nurseId, "Desc", LocalDateTime.of(2023, 10, 27, 10, 0), AppointmentsStatus.REQUESTED);
        when(appointmentsService.create(any(Appointments.class))).thenReturn(appointment);

        String mutation = """
                mutation($patientId: ID!, $nurseId: ID!, $description: String!, $timeSlot: String!, $status: AppointmentsStatus!) {
                  createAppointment(patientId: $patientId, nurseId: $nurseId, description: $description, timeSlot: $timeSlot, status: $status) {
                    id
                    description
                  }
                }
                """;

        graphQlTester.document(mutation)
                .variable("patientId", patientId)
                .variable("nurseId", nurseId)
                .variable("description", "Desc")
                .variable("timeSlot", "2023-10-27T10:00:00")
                .variable("status", "REQUESTED")
                .execute()
                .errors()
                .verify()
                .path("createAppointment")
                .entity(Appointments.class)
                .matches(a -> a.getDescription().equals("Desc"));
    }
}
