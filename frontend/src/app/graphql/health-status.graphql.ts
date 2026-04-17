import { gql } from 'apollo-angular';

export const GET_HEALTH_STATUSES = gql`
  query GetHealthStatusesByPatient($patientId: ID!) {
    getHealthStatusesByPatientId(patientId: $patientId) {
      id
      painScale
      mood
      symptoms
      notes
      timestamp
    }
  }
`;

export const CREATE_HEALTH_STATUS = gql`
  mutation CreateHealthStatus($patientId: ID!, $painScale: Int!, $mood: Mood!, $symptoms: [String], $notes: String, $timestamp: String!) {
    createHealthStatus(patientId: $patientId, painScale: $painScale, mood: $mood, symptoms: $symptoms, notes: $notes, timestamp: $timestamp) {
      id
      painScale
      mood
      symptoms
      notes
      timestamp
      patientId
    }
  }
`;

export const HEALTH_STATUS_SUBSCRIPTION = gql`
  subscription OnHealthStatusAdded($patientId: ID) {
    onHealthStatusAdded(patientId: $patientId) {
      id
      painScale
      mood
      symptoms
      notes
      timestamp
      patientId
    }
  }
`;
