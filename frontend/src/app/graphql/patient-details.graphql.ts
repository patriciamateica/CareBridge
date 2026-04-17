import { gql } from 'apollo-angular';

export const GET_ALL_PATIENT_DETAILS = gql`
  query GetAllPatientDetails($page: Int = 0, $size: Int = 500) {
    getAllPatientDetails(page: $page, size: $size) {
      id
      userId
      primaryDiagnosis
      diagnostics
      scans
      emergencyContact
      assignedNurseId
    }
  }
`;

export const CREATE_PATIENT_DETAILS = gql`
  mutation CreatePatientDetails(
    $userId: ID!
    $primaryDiagnosis: String!
    $diagnostics: [String]
    $scans: [String]
    $emergencyContact: String
    $assignedNurseId: ID
  ) {
    createPatientDetails(
      userId: $userId
      primaryDiagnosis: $primaryDiagnosis
      diagnostics: $diagnostics
      scans: $scans
      emergencyContact: $emergencyContact
      assignedNurseId: $assignedNurseId
    ) {
      id
      userId
      primaryDiagnosis
      diagnostics
      emergencyContact
      assignedNurseId
    }
  }
`;

export const UPDATE_PATIENT_DIAGNOSIS = gql`
  mutation UpdatePatientDiagnosis($id: ID!, $primaryDiagnosis: String!) {
    updatePatientDiagnosis(id: $id, primaryDiagnosis: $primaryDiagnosis) {
      id
      userId
      primaryDiagnosis
    }
  }
`;

export const PATIENT_DETAILS_SUBSCRIPTION = gql`
  subscription OnPatientDetailsCreated($userId: ID) {
    onPatientDetailsCreated(userId: $userId) {
      id
      userId
      primaryDiagnosis
    }
  }
`;
