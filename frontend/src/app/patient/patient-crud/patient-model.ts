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
  notes: string;
  createdAt: Date;
}

export interface VitalReading {
  timestamp: Date;
  heartRate: number;
  systolic: number;
  diastolic: number;
  respiratoryRate: number;
  spo2: number;
}

export interface DailyCheckIn {
  id: string;
  patientId: string;
  date: Date;
  painLevel: number;        // 1-10
  mood: MoodStatus;
  symptoms: string[];       // 'Nausea' | 'Shortness of Breath' | 'Fatigue' | 'Insomnia' | 'Constipation'
  comments: string;
  lastModified: Date;
}

export interface Medication {
  id: string;
  patientId: string;
  name: string;
  dose: string;
  timing: string;
  addedBy: string;
  addedAt: Date;
}

export interface CareNote {
  id: string;
  patientId: string;
  content: string;
  createdAt: Date;
  nurseName: string;
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
