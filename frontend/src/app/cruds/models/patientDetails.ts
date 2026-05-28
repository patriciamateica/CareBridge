import { PatientStatus } from './patient-ui-models';

export interface PatientDetails {
  id: string;
  userId: string;
  patientFirstName?: string;
  patientLastName?: string;
  primaryDiagnosis: string;
  diagnostics: string[];
  scans: string[];
  emergencyContact: string;
  assignedNurseId: string;
  assignedNurseName?: string;
  status?: PatientStatus;
}
