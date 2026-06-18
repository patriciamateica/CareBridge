import { Component, computed, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { DatePipe, NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SelectModule } from 'primeng/select';
import { Subscription } from 'rxjs';

import { AuthService } from '../../auth-service/auth.service';
import { PrescriptionService } from '../cruds/services/prescriptionService';
import { PatientDetailsService } from '../cruds/services/patientDetailsService';
import { AppointmentsService } from '../cruds/services/appointmentsService';
import { UserService } from '../cruds/services/userService';
import { RxStompService } from '../rx-stomp.service';
import { ToastService } from '../toast-service/toast-service';

import { Prescription } from '../cruds/models/prescription';
import { User } from '../cruds/models/user';

@Component({
  selector: 'app-medication',
  standalone: true,
  imports: [DatePipe, NgClass, FormsModule, SelectModule],
  templateUrl: './medication.html',
  styleUrl: './medication.css',
})
export class MedicationComponent implements OnInit, OnDestroy {
  private readonly prescriptionSvc = inject(PrescriptionService);
  private readonly patientDetailsSvc = inject(PatientDetailsService);
  private readonly appointmentsSvc = inject(AppointmentsService);
  private readonly userSvc = inject(UserService);
  private readonly rxStompSvc = inject(RxStompService);
  protected readonly authService = inject(AuthService);
  private readonly toastService = inject(ToastService);

  private readonly _prescriptions = signal<Prescription[]>([]);
  private readonly _patients = signal<User[]>([]);
  private assignedNurseId = signal<string>('');
  private patientDetailsId = signal<string>('');
  private readonly subscriptions: Subscription[] = [];

  searchQuery = signal('');
  showRefillDialog = signal(false);
  refillTarget = signal<Prescription | null>(null);

  readonly isNurseMode = computed(() => ['Nurse', 'Admin'].includes(this.authService.currentRole()));
  readonly selectedPatientId = signal<string>('');
  readonly patientOptions = computed(() =>
    this._patients().map(p => ({ label: `${p.firstName} ${p.lastName}`, value: p.id }))
  );

  ngOnInit() {
    if (this.isNurseMode()) {
      this.userSvc.getByRole('PATIENT').subscribe({
        next: (res: any) => this._patients.set(res.content || []),
        error: () => {}
      });
    }
    this.loadData();

    this.subscriptions.push(
      this.rxStompSvc.watch('/topic/prescriptions').subscribe((msg) => {
        const p: Prescription = JSON.parse(msg.body);
        this._prescriptions.update(list => {
          const idx = list.findIndex(x => x.id === p.id);
          if (idx !== -1) { const n = [...list]; n[idx] = p; return n; }
          return [...list, p];
        });
      }),
      this.rxStompSvc.watch('/topic/prescriptions/deleted').subscribe((msg) => {
        const data = JSON.parse(msg.body);
        const id = data.id || data;
        this._prescriptions.update(list => list.filter(x => x.id !== id));
      })
    );
  }

  ngOnDestroy() { this.subscriptions.forEach(s => s.unsubscribe()); }

  private loadData(patientId?: string) {
    const userId = patientId || this.authService.currentUserId();
    if (!userId) return;
    this.patientDetailsSvc.getByUserId(userId).subscribe({
      next: (details) => {
        this.assignedNurseId.set(details.assignedNurseId || '');
        this.patientDetailsId.set(details.id);
        this.prescriptionSvc.getByPatientId(userId, 0, 200).subscribe({
          next: (res) => this._prescriptions.set(res.content || []),
          error: () => {}
        });
      },
      error: () => {
        this.prescriptionSvc.getByPatientId(userId, 0, 200).subscribe({
          next: (res) => this._prescriptions.set(res.content || []),
          error: () => {}
        });
      }
    });
  }

  onPatientSelect(patientId: string): void {
    this.selectedPatientId.set(patientId);
    this._prescriptions.set([]);
    this.loadData(patientId);
  }

  readonly filteredPrescriptions = computed(() => {
    const q = this.searchQuery().toLowerCase().trim();
    return this._prescriptions().filter(p =>
      !q || p.name.toLowerCase().includes(q) || p.dose.toLowerCase().includes(q) || p.timing.toLowerCase().includes(q)
    );
  });

  readonly activePrescriptionCount = computed(() => this._prescriptions().length);

  readonly needRefillCount = computed(() =>
    this._prescriptions().filter(p => (p.refillsLeft ?? 0) <= 1).length
  );

  readonly nextRefillDate = computed(() => {
    const dates = this._prescriptions()
      .map(p => p.nextRefillDate)
      .filter(Boolean)
      .map(d => new Date(d!))
      .sort((a, b) => a.getTime() - b.getTime());
    return dates[0] || null;
  });

  requestRefill(prescription: Prescription) {
    this.refillTarget.set(prescription);
    this.showRefillDialog.set(true);
  }

  confirmRefill() {
    const p = this.refillTarget();
    if (!p) return;

    const nurseId = this.assignedNurseId();
    const patientId = this.authService.currentUserId();

    if (!nurseId) {
      this.toastService.showError('You need an assigned nurse to request a refill.');
      this.showRefillDialog.set(false);
      return;
    }

    const timeSlot = new Date();
    timeSlot.setDate(timeSlot.getDate() + 1);
    timeSlot.setHours(10, 0, 0, 0);

    const appointment: any = {
      patientId,
      nurseId,
      description: `Refill request: ${p.name} ${p.dose}`,
      timeSlot: timeSlot.toISOString().slice(0, 16),
      status: 'REQUESTED'
    };

    this.appointmentsSvc.create(appointment).subscribe({
      next: () => {
        this.toastService.showSuccess(`Refill request for ${p.name} sent to your nurse.`);
        this.showRefillDialog.set(false);
      },
      error: () => {
        this.toastService.showError('Could not send refill request. Please try again.');
        this.showRefillDialog.set(false);
      }
    });
  }
}
