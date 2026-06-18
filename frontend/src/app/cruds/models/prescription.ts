export interface Prescription {
  id: string;
  name: string;
  dose: string;
  timing: string;
  patientId: string;
  nurseId: string;
  refillsLeft?: number;
  nextRefillDate?: string;
}
