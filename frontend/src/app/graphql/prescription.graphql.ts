import { gql } from 'apollo-angular';

export const GET_PRESCRIPTIONS = gql`
  query GetPrescriptionsByPatient($patientId: ID!) {
    getPrescriptionsByPatientId(patientId: $patientId) {
      id
      name
      dose
      timing
      nurseId
    }
  }
`;

export const CREATE_PRESCRIPTION = gql`
  mutation CreatePrescription($name: String!, $dose: String!, $timing: String!, $patientId: ID!, $nurseId: ID!) {
    createPrescription(name: $name, dose: $dose, timing: $timing, patientId: $patientId, nurseId: $nurseId) {
      id
      name
      dose
      timing
      nurseId
      patientId
    }
  }
`;

export const PRESCRIPTION_SUBSCRIPTION = gql`
  subscription OnPrescriptionCreated($patientId: ID) {
    onPrescriptionCreated(patientId: $patientId) {
      id
      name
      dose
      timing
      nurseId
      patientId
    }
  }
`;
