import { gql } from 'apollo-angular';

export const GET_TASKS = gql`
  query GetTasks($page: Int = 0, $size: Int = 500) {
    getTasks(page: $page, size: $size) {
      id
      title
      description
      taskType
      neededBy
      status
      patientId
    }
  }
`;

export const TASK_SUBSCRIPTION = gql`
  subscription OnTaskCreated($patientId: ID) {
    onTaskCreated(patientId: $patientId) {
      id
      title
      description
      taskType
      status
      neededBy
      patientId
    }
  }
`;
