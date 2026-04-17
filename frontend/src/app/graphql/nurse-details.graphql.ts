import { gql } from 'apollo-angular';

export const GET_ALL_NURSE_DETAILS = gql`
  query GetAllNurseDetails($page: Int = 0, $size: Int = 500) {
    getAllNurseDetails(page: $page, size: $size) {
      id
      userId
      specialization
      hospitalAffiliation
      experienceYears
      hireMeStatus
    }
  }
`;

export const NURSE_DETAILS_SUBSCRIPTION = gql`
  subscription OnNurseDetailsCreated($userId: ID) {
    onNurseDetailsCreated(userId: $userId) {
      id
      userId
      specialization
      hireMeStatus
    }
  }
`;
