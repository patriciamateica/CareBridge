export type PatientStatus = 'Active' | 'Inactive' | 'Critical';
export type MoodStatus = 'Calm' | 'Anxious' | 'Depressed' | 'Irritable';

export interface Patient {
  id: string;
  firstName: string;
  lastName: string;
  dateOfBirth: Date;
  diagnosis: string;
  neurologicalStatus: string;
  associatedConditions: string;
  status: PatientStatus;
  phone: string;
  address: string;
  assignedNurse: string;
  assignedNurseId?: string;
  notes: string;
  createdAt: Date;
}

export interface CreatePatientDto {
  firstName: string;
  lastName: string;
  dateOfBirth: Date;
  diagnosis: string;
  neurologicalStatus: string;
  associatedConditions: string;
  status: PatientStatus;
  phone: string;
  address: string;
  assignedNurse: string;
  notes: string;
}

export type UpdatePatientDto = CreatePatientDto;
