import { PatientStatus } from './patient-ui-models';

export interface PatientDetails {
  id: string;
  userId: string;
  primaryDiagnosis: string;
  diagnostics: string[];
  scans: string[];
  emergencyContact: string;
  assignedNurseId: string;
  assignedNurseName?: string;
  status?: PatientStatus;
}
