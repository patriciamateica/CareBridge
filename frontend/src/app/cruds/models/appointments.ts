export interface Appointments {
  id: string;
  patientId: string;
  nurseId: string;
  description: string;
  timeSlot: string;
  status: "REQUESTED" | "VALIDATED" | "CANCELLED" | "COMPLETED" | "SCHEDULED";
}
