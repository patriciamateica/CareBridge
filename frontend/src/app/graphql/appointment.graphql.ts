import { gql } from 'apollo-angular';

export const GET_APPOINTMENTS = gql`
  query GetAppointments {
    getAppointments {
      id
      patientId
      nurseId
      description
      timeSlot
      status
    }
  }
`;

export const APPOINTMENT_SUBSCRIPTION = gql`
  subscription OnAppointmentScheduled($patientId: ID) {
    onAppointmentScheduled(patientId: $patientId) {
      id
      description
      timeSlot
      status
    }
  }
`;
