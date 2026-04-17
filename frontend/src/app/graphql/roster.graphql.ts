import { gql } from 'apollo-angular';

export const GET_ROSTERS = gql`
  query GetRosters($page: Int = 0, $size: Int = 500) {
    getRosters(page: $page, size: $size) {
      id
      patientId
      nurseId
      status
    }
  }
`;

export const CREATE_ROSTER = gql`
  mutation CreateRoster($patientId: ID!, $nurseId: ID!, $status: RosterStatus!) {
    createRoster(patientId: $patientId, nurseId: $nurseId, status: $status) {
      id
      patientId
      nurseId
      status
    }
  }
`;

export const UPDATE_ROSTER_STATUS = gql`
  mutation UpdateRosterStatus($id: ID!, $status: RosterStatus!) {
    updateRosterStatus(id: $id, status: $status) {
      id
      patientId
      nurseId
      status
    }
  }
`;

export const DELETE_ROSTER = gql`
  mutation DeleteRoster($id: ID!) {
    deleteRoster(id: $id)
  }
`;

export const ROSTER_SUBSCRIPTION = gql`
  subscription OnRosterUpdated($nurseId: ID) {
    onRosterUpdated(nurseId: $nurseId) {
      id
      patientId
      nurseId
      status
    }
  }
`;
