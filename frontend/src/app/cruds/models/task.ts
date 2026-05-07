export interface Task {
  id: string;
  title: string;
  description: string;
  taskType: any;
  neededBy: string;
  status: any;
  patientId: string;
  claimerId: string;
}
