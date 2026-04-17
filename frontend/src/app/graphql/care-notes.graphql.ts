import { gql } from 'apollo-angular';

export const GET_CARE_NOTES = gql`
  query GetCareNotesByPatient($patientId: ID!) {
    getCareNotesByPatientId(patientId: $patientId) {
      id
      content
      timestamp
      nurseId
    }
  }
`;

export const CREATE_CARE_NOTE = gql`
  mutation CreateCareNote($patientId: ID!, $nurseId: ID!, $content: String!, $timestamp: String!) {
    createCareNote(patientId: $patientId, nurseId: $nurseId, content: $content, timestamp: $timestamp) {
      id
      content
      timestamp
      nurseId
      patientId
    }
  }
`;

export const CARE_NOTE_SUBSCRIPTION = gql`
  subscription OnCareNoteAdded($patientId: ID) {
    onCareNoteAdded(patientId: $patientId) {
      id
      content
      timestamp
      nurseId
      patientId
    }
  }
`;
