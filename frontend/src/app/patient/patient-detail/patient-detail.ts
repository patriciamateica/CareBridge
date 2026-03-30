import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SliderModule } from 'primeng/slider';
import { TextareaModule } from 'primeng/textarea';
import { InputTextModule } from 'primeng/inputtext';
import { PatientService } from '../patient-crud/patient-service';
import { MoodStatus, Patient, PatientStatus } from '../patient-crud/patient-model';
import { UpdatePatient } from '../ui/update-patient/update-patient';
import { DeletePatient } from '../ui/delete-patient/delete-patient';
import {ToastService} from '../../toast-service/toast-service';

@Component({
  selector: 'app-patient-detail',
  standalone: true,
  imports: [
    DatePipe, FormsModule,
    SliderModule, TextareaModule, InputTextModule,
    UpdatePatient, DeletePatient, RouterLink
  ],
  templateUrl: './patient-detail.html',
  styleUrl: './patient-detail.css',
})
export class PatientDetail implements OnInit {
  protected readonly patientService = inject(PatientService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly toastService = inject(ToastService);

  patient = signal<Patient | null>(null);

  checkInPainLevel = signal(5);
  checkInMood = signal<MoodStatus>('Calm');
  checkInSymptoms = signal<string[]>([]);
  checkInComments = signal('');
  showCheckInHistory = signal(false);
  newNoteContent = signal('');
  showMedDialog = signal(false);
  newMedName = signal('');
  newMedDose = signal('');
  newMedTiming = signal('');
  readonly isInactive = computed(() => this.patient()?.status === 'Inactive');

  readonly moodOptions: MoodStatus[] = ['Calm', 'Anxious', 'Depressed', 'Irritable'];
  readonly symptomOptions = ['Nausea', 'Shortness of Breath', 'Fatigue', 'Insomnia', 'Constipation'];

  readonly vitals = computed(() => {
    const p = this.patient();
    return p ? this.patientService.getVitals(p.id) : [];
  });

  readonly latestVital = computed(() => {
    const v = this.vitals();
    return v.length ? v[v.length - 1] : null;
  });

  readonly recentVitalsTable = computed(() => {
    return [...this.vitals()].reverse().slice(0, 5);
  });

  readonly sparklinePath = computed((): string | null => {
    const v = this.vitals();
    if (v.length < 2) return null;

    const hrs = v.map(r => r.heartRate);
    const min = Math.min(...hrs);
    const max = Math.max(...hrs);
    const range = max - min || 1;

    const W = 300;
    const H = 52;
    const pad = 4;

    const points = hrs.map((hr, i) => {
      const x = (i / (hrs.length - 1)) * W;
      const y = H - pad - ((hr - min) / range) * (H - pad * 2);
      return [x, y] as [number, number];
    });

    let d = `M ${points[0][0]},${points[0][1]}`;
    for (let i = 1; i < points.length; i++) {
      const prev = points[i - 1];
      const curr = points[i];
      const cpx = (prev[0] + curr[0]) / 2;
      d += ` C ${cpx},${prev[1]} ${cpx},${curr[1]} ${curr[0]},${curr[1]}`;
    }
    return d;
  });

  readonly medications = computed(() => {
    const p = this.patient();
    return p ? this.patientService.getMedications(p.id) : [];
  });

  readonly careNotes = computed(() => {
    const p = this.patient();
    return p ? this.patientService.getCareNotes(p.id) : [];
  });

  readonly todayCheckIn = computed(() => {
    const p = this.patient();
    return p ? this.patientService.getTodayCheckIn(p.id) : null;
  });

  readonly checkInHistory = computed(() => {
    const p = this.patient();
    return p ? this.patientService.getCheckIns(p.id) : [];
  });

  isAnyCritical(): boolean {
    return this.patient()?.status === 'Critical';
  }

  getSeverity(status: PatientStatus): 'success' | 'danger' | 'warn' {
    return ({ Active: 'success', Inactive: 'warn', Critical: 'danger' } as const)[status];
  }

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) { this.router.navigate(['/dashboard/patient-management']); return; }
    const p = this.patientService.getById(id);
    if (!p) { this.router.navigate(['/dashboard/patient-management']); return; }
    this.patient.set(p);

    const existing = this.patientService.getTodayCheckIn(id);
    if (existing) {
      this.checkInPainLevel.set(existing.painLevel);
      this.checkInMood.set(existing.mood);
      this.checkInSymptoms.set([...existing.symptoms]);
      this.checkInComments.set(existing.comments);
    }
  }

  goBack() { this.router.navigate(['/dashboard/patient-management']); }

  onPatientUpdated() {
    const id = this.patient()?.id;
    if (id) this.patient.set(this.patientService.getById(id) ?? null);
  }

  onPatientDeleted() { this.router.navigate(['/dashboard/patient-management']); }

  submitCheckIn() {
    if (this.isInactive()) return;
    const p = this.patient();
    if (!p) return;
    this.patientService.upsertCheckIn(p.id, {
      painLevel: this.checkInPainLevel(),
      mood: this.checkInMood(),
      symptoms: this.checkInSymptoms(),
      comments: this.checkInComments(),
    });
    return this.toastService.showSuccess("Daily Check-In message saved.");
  }

  toggleSymptom(symptom: string) {
    if (this.isInactive()) return;
    const current = this.checkInSymptoms();
    if (current.includes(symptom)) {
      this.checkInSymptoms.set(current.filter(s => s !== symptom));
    } else {
      this.checkInSymptoms.set([...current, symptom]);
    }
  }

  submitNote() {
    if (this.isInactive()) return;
    const content = this.newNoteContent().trim();
    if (!content || !this.patient()) return;
    this.patientService.addCareNote(this.patient()!.id, content, 'Ana Pop');
    this.newNoteContent.set('');
    return this.toastService.showSuccess("Care Note saved.");
  }

  deleteNote(id: string) {
    if (this.isInactive()) return;
    this.patientService.deleteCareNote(id); }

  openMedDialog() {
    if (this.isInactive()) return;
    this.newMedName.set('');
    this.newMedDose.set('');
    this.newMedTiming.set('');
    this.showMedDialog.set(true);
  }

  submitMedication() {
    if (this.isInactive()) return;
    const name = this.newMedName().trim();
    const dose = this.newMedDose().trim();
    const timing = this.newMedTiming().trim();
    if (!name || !dose || !timing || !this.patient()) return;
    this.patientService.addMedication(this.patient()!.id, name, dose, timing, 'Ana Pop');
    this.showMedDialog.set(false);
    return this.toastService.showSuccess("Medication Added.");
  }

  deleteMedication(id: string) {
    if (this.isInactive()) return;
    this.patientService.deleteMedication(id); }

  getPainColor(level: number): string {
    if (level <= 3) return '#4ade80';
    if (level <= 6) return '#facc15';
    return '#ef4444';
  }
}
