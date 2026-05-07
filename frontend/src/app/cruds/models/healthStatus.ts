export interface HealthStatus {
  id: string;
  painScale: number;
  mood: any;
  symptoms: string[];
  notes: string;
  timestamp: string;
  patientId: string;
}
