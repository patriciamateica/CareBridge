import { gql } from 'apollo-angular';

export const GET_CLINICAL_LOGS = gql`
  query GetClinicalLogsByPatient($patientId: ID!) {
    getClinicalLogsByPatientId(patientId: $patientId) {
      id
      documentTitle
      documentType
      datePerformed
      fileUrl
      status
    }
  }
`;

export const CLINICAL_LOG_SUBSCRIPTION = gql`
  subscription OnClinicalLogAdded($patientId: ID) {
    onClinicalLogAdded(patientId: $patientId) {
      id
      documentTitle
      documentType
      datePerformed
    }
  }
`;
