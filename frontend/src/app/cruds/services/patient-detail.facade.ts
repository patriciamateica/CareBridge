import { computed, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, firstValueFrom, forkJoin, of, Subscription } from 'rxjs';

import { User } from '../models/user';
import { PatientDetails } from '../models/patientDetails';
import { Vitals } from '../models/vitals';
import { CareNotes } from '../models/careNotes';
import { Prescription } from '../models/prescription';
import { HealthStatus } from '../models/healthStatus';
import { ClinicalLog } from '../models/clinicalLog';
import { MoodStatus, Patient } from '../models/patient-ui-models';

import { UserService } from './userService';
import { PatientDetailsService } from './patientDetailsService';
import { VitalsService } from './vitalsService';
import { CareNotesService } from './careNotesService';
import { PrescriptionService } from './prescriptionService';
import { HealthStatusService } from './healthStatusService';
import { ClinicalLogService } from './clinicalLogService';
import { NetworkService } from '../../offline-support/network.service';
import { OfflineStorageService } from '../../offline-support/offline-storage.service';
import { ToastService } from '../../toast-service/toast-service';
import { AuthService } from '../../../auth-service/auth.service';

@Injectable({ providedIn: 'root' })
export class PatientDetailFacadeService {
  private subs: Subscription[] = [];

  readonly user    = signal<User | null>(null);
  readonly details = signal<PatientDetails | null>(null);
  readonly vitalsRaw      = signal<Vitals[]>([]);
  readonly careNotesRaw   = signal<CareNotes[]>([]);
  readonly prescriptionsRaw = signal<Prescription[]>([]);
  readonly healthStatusesRaw = signal<HealthStatus[]>([]);

  readonly clinicalLogsRaw = signal<ClinicalLog[]>([]);
  readonly showClinicalLogsDialog = signal(false);
  readonly clinicalLogFilter = signal<string>('ALL');

  readonly loading         = signal(false);
  readonly medPage         = signal(0);
  readonly medPageSize     = 5;
  readonly notePage        = signal(0);
  readonly notePageSize    = 5;
  readonly visibleMedCount  = signal(5);
  readonly visibleNoteCount = signal(5);

  readonly checkInPainLevel  = signal(5);
  readonly checkInMood       = signal<MoodStatus>('Calm');
  readonly checkInSymptoms   = signal<string[]>([]);
  readonly checkInComments   = signal('');
  readonly showCheckInHistory = signal(false);

  readonly newNoteContent = signal('');
  readonly editingNoteId  = signal<string | null>(null);

  readonly showMedDialog = signal(false);
  readonly newMedName    = signal('');
  readonly newMedDose    = signal('');
  readonly newMedTiming  = signal('');

   readonly patient = computed<Patient | null>(() => {
     const u = this.user();
     const d = this.details();
     if (!u) return null;

     return {
       id: u.id,
       firstName: u.firstName,
       lastName: u.lastName,
       dateOfBirth: new Date(u.dateOfBirth),
       diagnosis: d?.primaryDiagnosis || 'None Recorded',
       neurologicalStatus: '',
       associatedConditions: d?.diagnostics?.join(', ') || '',
       status: (d?.status as any) || 'Active',
       phone: u.phoneNumber?.toString() || '',
       address: u.residentialAddress,
       assignedNurse: d?.assignedNurseId || '',
       assignedNurseId: d?.assignedNurseId,
       notes: '',
       createdAt: new Date(),
     };
   });

  readonly isInactive = computed(() => this.patient()?.status === 'Inactive');

  readonly vitals = computed(() =>
    this.vitalsRaw().map(v => ({
      timestamp:       new Date(v.timestamp),
      heartRate:       v.heartRate,
      systolic:        v.bloodPressure,
      diastolic:       80,
      respiratoryRate: v.respiratoryRate,
      spo2:            v.spO2,
    }))
  );

  readonly latestVital    = computed(() => { const v = this.vitals(); return v.length ? v[v.length - 1] : null; });
  readonly recentVitalsTable = computed(() => [...this.vitals()].reverse().slice(0, 5));

  readonly sparklinePath = computed((): string | null => {
    const v = this.vitals();
    if (v.length < 2) return null;
    const hrs = v.map(r => r.heartRate);
    const min = Math.min(...hrs), max = Math.max(...hrs), range = max - min || 1;
    const W = 300, H = 52, pad = 4;
    const points = hrs.map((hr, i) => {
      const x = (i / (hrs.length - 1)) * W;
      const y = H - pad - ((hr - min) / range) * (H - pad * 2);
      return [x, y] as [number, number];
    });
    let d = `M ${points[0][0]},${points[0][1]}`;
    for (let i = 1; i < points.length; i++) {
      const prev = points[i - 1], curr = points[i], cpx = (prev[0] + curr[0]) / 2;
      d += ` C ${cpx},${prev[1]} ${cpx},${curr[1]} ${curr[0]},${curr[1]}`;
    }
    return d;
  });

  readonly medications       = computed(() => this.prescriptionsRaw());
  readonly medicationTotal   = computed(() => this.prescriptionsRaw().length);
  readonly visibleMedications = computed(() => this.prescriptionsRaw().slice(0, this.visibleMedCount()));

  readonly careNotes         = computed(() => this.careNotesRaw());
  readonly careNotesTotal    = computed(() => this.careNotesRaw().length);
  readonly visibleCareNotes  = computed(() => this.careNotesRaw().slice(0, this.visibleNoteCount()));

  readonly careNotesStats = computed(() => {
    const notes = this.careNotesRaw();
    const now = Date.now(), weekMs = 7 * 24 * 60 * 60 * 1000;
    const last7Days = notes.filter(n => (now - new Date(n.timestamp).getTime()) <= weekMs).length;
    return { total: notes.length, last7Days, latestDate: notes.length ? new Date(notes[0].timestamp) : null };
  });

  readonly isAnyCritical = computed(() => this.patient()?.status === 'Critical');

  readonly filteredClinicalLogs = computed(() => {
    const filter = this.clinicalLogFilter();
    const logs = this.clinicalLogsRaw();
    return (filter === 'ALL' ? logs : logs.filter(l => l.documentType === filter))
      .sort((a, b) => new Date(b.datePerformed).getTime() - new Date(a.datePerformed).getTime());
  });

  readonly docTypeIcon: Record<string, string> = {
    SCAN: 'pi-image', REPORT: 'pi-file-pdf', LAB_RESULT: 'pi-chart-bar',
    REFERRAL: 'pi-send', PRESCRIPTION: 'pi-pills',
  };

  getDocIcon(type: string): string { return this.docTypeIcon[type] || 'pi-file'; }

  getPainColor(level: number): string {
    if (level <= 3) return '#4ade80';
    if (level <= 6) return '#facc15';
    return '#ef4444';
  }

  constructor(
    private readonly userSvc: UserService,
    private readonly detailsSvc: PatientDetailsService,
    private readonly vitalsSvc: VitalsService,
    private readonly careNotesSvc: CareNotesService,
    private readonly prescriptionSvc: PrescriptionService,
    private readonly healthStatusSvc: HealthStatusService,
    private readonly clinicalLogSvc: ClinicalLogService,
    private readonly networkSvc: NetworkService,
    private readonly offlineStorage: OfflineStorageService,
    private readonly toastService: ToastService,
    private readonly authService: AuthService,
    private readonly router: Router,
  ) {}

  init(patientId: string): void {
    this.reset();
    this.loading.set(true);
    this.loadData(patientId);
    this.setupWebSockets(patientId);
  }

  destroy(): void {
    this.subs.forEach(s => s.unsubscribe());
    this.subs = [];
  }

  private reset(): void {
    this.user.set(null);
    this.details.set(null);
    this.vitalsRaw.set([]);
    this.careNotesRaw.set([]);
    this.prescriptionsRaw.set([]);
    this.healthStatusesRaw.set([]);
    this.clinicalLogsRaw.set([]);
    this.showClinicalLogsDialog.set(false);
    this.clinicalLogFilter.set('ALL');
    this.medPage.set(0);
    this.notePage.set(0);
    this.visibleMedCount.set(this.medPageSize);
    this.visibleNoteCount.set(this.notePageSize);
    this.checkInPainLevel.set(5);
    this.checkInMood.set('Calm');
    this.checkInSymptoms.set([]);
    this.checkInComments.set('');
    this.newNoteContent.set('');
    this.editingNoteId.set(null);
    this.showMedDialog.set(false);
    this.newMedName.set('');
    this.newMedDose.set('');
    this.newMedTiming.set('');
  }

  private loadData(id: string): void {
    this.subs.push(
      forkJoin({
        user:    this.userSvc.getById(id),
        details: this.detailsSvc.getByUserId(id).pipe(catchError(() => of(null))),
        vitals:  this.vitalsSvc.getByPatientId(id, 0, 20).pipe(catchError(() => of({ content: [] }))),
        notes:   this.careNotesSvc.getByPatientId(id, 0, this.notePageSize).pipe(catchError(() => of({ content: [] }))),
        meds:    this.prescriptionSvc.getByPatientId(id, 0, this.medPageSize).pipe(catchError(() => of({ content: [] }))),
        health:  this.healthStatusSvc.getByPatientId(id, 0, 10).pipe(catchError(() => of({ content: [] }))),
        logs:    this.clinicalLogSvc.getByPatientId(id).pipe(catchError(() => of([]))),
      }).subscribe({
        next: (res: any) => {
          this.user.set(res.user);
          this.details.set(res.details ?? null);
          this.vitalsRaw.set(res.vitals?.content ?? []);
          this.careNotesRaw.set(res.notes?.content ?? []);
          this.prescriptionsRaw.set(res.meds?.content ?? []);
          this.healthStatusesRaw.set(res.health?.content ?? []);
          this.clinicalLogsRaw.set(Array.isArray(res.logs) ? res.logs : []);
          this.syncCheckIn();
          this.loading.set(false);
        },
        error: err => {
          console.error('[PatientDetailFacade] loadData error', err);
          this.toastService.showError('Could not load patient data.');
          this.user.set(null);
          this.details.set(null);
          this.loading.set(false);
        },
      })
    );
  }

  private setupWebSockets(id: string): void {
    this.subs.push(
      this.userSvc.listenToUpdates('/topic/users').subscribe((u: User) => {
        if (u.id === id) this.user.set(u);
      }),
      this.userSvc.listenToUpdates('/topic/users/deleted').subscribe((res: any) => {
        if (res.id === id) {
          this.toastService.showError('This patient record has been deleted.');
          this.router.navigate(['/dashboard/patient-management']);
        }
      }),

      this.detailsSvc.listenToUpdates('/topic/patient-details').subscribe((d: PatientDetails) => {
        if (d.userId === id) this.details.set(d);
      }),
      this.detailsSvc.listenToUpdates('/topic/patient-details/deleted').subscribe((res: any) => {
        if (this.details()?.id === res.id) {
          this.toastService.showError('Patient details record has been deleted.');
          this.router.navigate(['/dashboard/patient-management']);
        }
      }),

      this.vitalsSvc.listenToUpdates('/topic/vitals').subscribe((v: Vitals) => {
        if (v.patientId !== id) return;
        this.vitalsRaw.update(list => {
          const idx = list.findIndex(i => i.id === v.id);
          return idx >= 0 ? list.map((i, n) => (n === idx ? v : i)) : [...list, v];
        });
      }),
      this.vitalsSvc.listenToUpdates('/topic/vitals/deleted').subscribe((res: any) => {
        this.vitalsRaw.update(list => list.filter(v => v.id !== res.id));
      }),

      this.careNotesSvc.listenToUpdates('/topic/care-notes').subscribe((n: CareNotes) => {
        if (n.patientId !== id) return;
        this.careNotesRaw.update(list => {
          const idx = list.findIndex(i => i.id === n.id);
          return idx >= 0 ? list.map((i, k) => (k === idx ? n : i)) : [n, ...list];
        });
      }),
      this.careNotesSvc.listenToUpdates('/topic/care-notes/deleted').subscribe((res: any) => {
        if (res?.id) {
          this.careNotesRaw.update(list => list.filter(n => n.id !== res.id));
          this.toastService.showInfo('Care note was deleted by another user.');
        }
      }),

      this.prescriptionSvc.listenToUpdates('/topic/prescriptions').subscribe((p: Prescription) => {
        if (p.patientId !== id) return;
        this.prescriptionsRaw.update(list => {
          const idx = list.findIndex(i => i.id === p.id);
          return idx >= 0 ? list.map((i, k) => (k === idx ? p : i)) : [...list, p];
        });
      }),
      this.prescriptionSvc.listenToUpdates('/topic/prescriptions/deleted').subscribe((res: any) => {
        if (res?.id) {
          this.prescriptionsRaw.update(list => list.filter(p => p.id !== res.id));
          this.toastService.showInfo('Medication was deleted by another user.');
        }
      }),

      this.healthStatusSvc.listenToUpdates('/topic/health-status').subscribe((h: HealthStatus) => {
        if (h.patientId !== id) return;
        this.healthStatusesRaw.update(list => {
          const idx = list.findIndex(i => i.id === h.id);
          return idx >= 0 ? list.map((i, k) => (k === idx ? h : i)) : [...list, h];
        });
        this.syncCheckIn();
      }),
      this.healthStatusSvc.listenToUpdates('/topic/health-status/deleted').subscribe(() => {
        this.syncCheckIn();
      }),
    );
  }

  private syncCheckIn(): void {
    const today = new Date().toISOString().split('T')[0];
    const existing = this.healthStatusesRaw().find(h => h.timestamp.startsWith(today));
    if (existing) {
      this.checkInPainLevel.set(existing.painScale);
      this.checkInMood.set(existing.mood as MoodStatus);
      this.checkInSymptoms.set([...(existing.symptoms || [])]);
      this.checkInComments.set(existing.notes);
    }
  }

  toggleSymptom(symptom: string): void {
    if (this.isInactive()) return;
    const current = this.checkInSymptoms();
    this.checkInSymptoms.set(
      current.includes(symptom) ? current.filter(s => s !== symptom) : [...current, symptom]
    );
  }

  async submitCheckIn(patientId: string): Promise<void> {
    if (!patientId || this.isInactive()) return;
    const payload = {
      painScale: this.checkInPainLevel(),
      mood: this.checkInMood(),
      symptoms: this.checkInSymptoms(),
      notes: this.checkInComments(),
      patientId,
      timestamp: new Date().toISOString(),
    };
    if (this.networkSvc.isOnline) {
      await firstValueFrom(this.healthStatusSvc.create(payload as any));
    } else {
      this.offlineStorage.queueOperation({ type: 'CHECKIN_UPSERT', payload });
    }
    this.toastService.showSuccess('Daily Check-In saved.');
  }

  async submitNote(patientId: string): Promise<void> {
    const content = this.newNoteContent().trim();
    if (!patientId || !content || this.isInactive()) {
      this.toastService.showError('Please enter a note.');
      return;
    }
    try {
      const payload: any = {
        patientId,
        nurseId: this.authService.currentUserId(),
        content,
        timestamp: new Date().toISOString(),
      };
      if (this.editingNoteId()) {
        const noteId = this.editingNoteId()!;
        if (this.networkSvc.isOnline) {
          const updated = await firstValueFrom(this.careNotesSvc.update(noteId, payload));
          this.careNotesRaw.update(list => list.map(n => n.id === noteId ? updated : n));
          this.toastService.showSuccess('Care Note updated.');
        } else {
          this.offlineStorage.queueOperation({ type: 'CARE_NOTE_UPDATE', payload: { noteId, ...payload } });
          this.toastService.showSuccess('Care Note update queued for sync.');
        }
        this.editingNoteId.set(null);
      } else {
        if (this.networkSvc.isOnline) {
          const created = await firstValueFrom(this.careNotesSvc.create(payload));
          this.careNotesRaw.update(list => [created, ...list]);
          this.toastService.showSuccess('Care Note added.');
        } else {
          this.offlineStorage.queueOperation({ type: 'CARE_NOTE_ADD', payload });
          this.toastService.showSuccess('Care Note queued for sync.');
        }
      }
      this.newNoteContent.set('');
    } catch (err) {
      console.error('[PatientDetailFacade] submitNote', err);
      this.toastService.showError('Failed to save note. Please try again.');
    }
  }

  startEditNote(noteId: string, content: string): void {
    if (this.isInactive()) return;
    this.editingNoteId.set(noteId);
    this.newNoteContent.set(content);
  }

  cancelEditNote(): void {
    this.editingNoteId.set(null);
    this.newNoteContent.set('');
  }

  async deleteNote(noteId: string): Promise<void> {
    if (this.isInactive()) {
      this.toastService.showError('Cannot delete notes for inactive patients.');
      return;
    }
    try {
      if (this.networkSvc.isOnline) {
        await firstValueFrom(this.careNotesSvc.delete(noteId));
        this.careNotesRaw.update(list => list.filter(n => n.id !== noteId));
        this.toastService.showSuccess('Care note deleted successfully.');
      } else {
        this.offlineStorage.queueOperation({ type: 'CARE_NOTE_DELETE', payload: { noteId } });
        this.toastService.showSuccess('Delete queued for sync.');
      }
    } catch (err) {
      console.error('[PatientDetailFacade] deleteNote', err);
      this.toastService.showError('Failed to delete note. Please try again.');
    }
  }

  openMedDialog(): void {
    this.newMedName.set('');
    this.newMedDose.set('');
    this.newMedTiming.set('');
    this.showMedDialog.set(true);
  }

  async submitMedication(patientId: string): Promise<void> {
    const name = this.newMedName().trim();
    if (!patientId || !name || this.isInactive()) {
      this.toastService.showError('Please enter medication name.');
      return;
    }
    try {
      const payload: any = {
        patientId,
        nurseId: this.authService.currentUserId(),
        name,
        dose: this.newMedDose(),
        timing: this.newMedTiming(),
        patientDetailsId: '',
      };
      if (this.networkSvc.isOnline) {
        const created = await firstValueFrom(this.prescriptionSvc.create(payload));
        this.prescriptionsRaw.update(list => [created, ...list]);
        this.toastService.showSuccess('Medication added successfully.');
      } else {
        this.offlineStorage.queueOperation({ type: 'MEDICATION_ADD', payload });
        this.toastService.showSuccess('Medication queued for sync.');
      }
      this.showMedDialog.set(false);
      this.newMedName.set('');
      this.newMedDose.set('');
      this.newMedTiming.set('');
    } catch (err) {
      console.error('[PatientDetailFacade] submitMedication', err);
      this.toastService.showError('Failed to add medication. Please try again.');
    }
  }

  async deleteMedication(medId: string): Promise<void> {
    if (this.isInactive()) {
      this.toastService.showError('Cannot delete medications for inactive patients.');
      return;
    }
    try {
      if (this.networkSvc.isOnline) {
        await firstValueFrom(this.prescriptionSvc.delete(medId));
        this.prescriptionsRaw.update(list => list.filter(p => p.id !== medId));
        this.toastService.showSuccess('Medication deleted successfully.');
      } else {
        this.offlineStorage.queueOperation({ type: 'MEDICATION_DELETE', payload: { medId } });
        this.toastService.showSuccess('Delete queued for sync.');
      }
    } catch (err) {
      console.error('[PatientDetailFacade] deleteMedication', err);
      this.toastService.showError('Failed to delete medication. Please try again.');
    }
  }

  async loadMoreMedications(patientId: string): Promise<void> {
    if (!patientId) return;
    try {
      const page = this.medPage();
      const res: any = await firstValueFrom(this.prescriptionSvc.getByPatientId(patientId, page + 1, this.medPageSize));
      if (res?.content?.length > 0) {
        this.prescriptionsRaw.update(prev => [...prev, ...res.content]);
        this.medPage.set(page + 1);
        this.visibleMedCount.update(c => c + this.medPageSize);
      }
    } catch (err) {
      console.error('[PatientDetailFacade] loadMoreMedications', err);
      this.toastService.showError('Failed to load more medications.');
    }
  }

  async loadMoreCareNotes(patientId: string): Promise<void> {
    if (!patientId) return;
    try {
      const page = this.notePage();
      const res: any = await firstValueFrom(this.careNotesSvc.getByPatientId(patientId, page + 1, this.notePageSize));
      if (res?.content?.length > 0) {
        this.careNotesRaw.update(prev => [...prev, ...res.content]);
        this.notePage.set(page + 1);
        this.visibleNoteCount.update(c => c + this.notePageSize);
      }
    } catch (err) {
      console.error('[PatientDetailFacade] loadMoreCareNotes', err);
      this.toastService.showError('Failed to load more care notes.');
    }
  }
}
