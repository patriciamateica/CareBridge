import { gql } from 'apollo-angular';

export const GET_VITALS_BY_PATIENT = gql`
  query GetVitalsByPatient($patientId: ID!) {
    getVitalsByPatientId(patientId: $patientId) {
      id
      heartRate
      bloodPressure
      respiratoryRate
      spO2
      timestamp
    }
  }
`;

export const CREATE_VITALS = gql`
  mutation CreateVitals($patientId: ID!, $heartRate: Int!, $bloodPressure: Int!, $respiratoryRate: Int!, $spO2: Int!, $timestamp: String!) {
    createVitals(patientId: $patientId, heartRate: $heartRate, bloodPressure: $bloodPressure, respiratoryRate: $respiratoryRate, spO2: $spO2, timestamp: $timestamp) {
      id
    }
  }
`;

export const VITALS_SUBSCRIPTION = gql`
  subscription OnVitalsAdded($patientId: ID) {
    onVitalsAdded(patientId: $patientId) {
      id
      heartRate
      bloodPressure
      respiratoryRate
      spO2
      timestamp
      patientId
    }
  }
`;
