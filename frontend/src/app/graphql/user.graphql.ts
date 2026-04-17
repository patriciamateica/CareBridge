import { gql } from 'apollo-angular';

export const GET_USERS = gql`
  query GetUsers($page: Int, $size: Int) {
    getUsers(page: $page, size: $size) {
      id
      firstName
      lastName
      email
      role
      userStatus
    }
  }
`;

export const REGISTER_USER = gql`
  mutation RegisterUser($input: RegisterRequestInput!) {
    registerUser(input: $input) {
      id
      firstName
      lastName
      email
      role
      userStatus
    }
  }
`;

export const LOGIN_USER = gql`
  mutation LoginUser($input: LoginRequestInput!) {
    loginUser(input: $input)
  }
`;

export const UPDATE_USER_STATUS = gql`
  mutation UpdateUserStatus($id: ID!, $status: UserStatus!) {
    updateUserStatus(id: $id, status: $status) {
      id
      userStatus
    }
  }
`;

export const USER_SUBSCRIPTION = gql`
  subscription OnUserRegistered {
    onUserRegistered {
      id
      firstName
      lastName
      role
    }
  }
`;
