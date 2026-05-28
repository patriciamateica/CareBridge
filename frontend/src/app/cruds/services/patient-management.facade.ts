import { computed, Injectable, signal } from '@angular/core';
import { Subscription, forkJoin } from 'rxjs';

import { User } from '../models/user';
import { PatientDetails } from '../models/patientDetails';
import { Vitals } from '../models/vitals';
import { Task } from '../models/task';
import { HealthStatus } from '../models/healthStatus';
import { Patient, PatientStatus } from '../models/patient-ui-models';

import { UserService } from './userService';
import { PatientDetailsService } from './patientDetailsService';
import { VitalsService } from './vitalsService';
import { TaskService } from './taskService';
import { HealthStatusService } from './healthStatusService';
import { AuthService } from '../../../auth-service/auth.service';

const STATUS_PRIORITY: Record<string, number> = { Critical: 1, Active: 2, Inactive: 3 };

@Injectable({ providedIn: 'root' })
export class PatientManagementFacadeService {
  private subs: Subscription[] = [];

  private readonly _users          = signal<User[]>([]);
  private readonly _details        = signal<PatientDetails[]>([]);
  private readonly _vitals         = signal<Vitals[]>([]);
  private readonly _tasks          = signal<Task[]>([]);
  private readonly _healthStatuses = signal<HealthStatus[]>([]);

  // ─── UI state signals ─────────────────────────────────────────────────────────
  readonly searchQuery       = signal('');
  readonly loading           = signal(false);
  readonly hasMorePatients   = signal(false);

  readonly patients = computed<Patient[]>(() => {
    const details = this._details();
    const users   = this._users();

    return details.map(d => {
      const u = users.find(user => user.id === d.userId);
      return {
        id:                   d.userId,
        firstName:            d.patientFirstName || u?.firstName || 'Unknown',
        lastName:             d.patientLastName || u?.lastName || 'Patient',
        diagnosis:            d.primaryDiagnosis || 'N/A',
        assignedNurse:        d.assignedNurseName || 'Unassigned',
        assignedNurseId:      d.assignedNurseId,
        status:               (d.status ?? 'Active') as PatientStatus,
        neurologicalStatus:   '',
        associatedConditions: d.diagnostics?.join(', ') || '',
        phone:                u?.phoneNumber?.toString() || '',
        address:              u?.residentialAddress || '',
        notes:                '',
        createdAt:            new Date(),
        dateOfBirth:          u?.dateOfBirth ? new Date(u.dateOfBirth) : new Date(),
      } satisfies Patient;
    });
  });

  readonly filteredPatients = computed(() => {
    const q    = this.searchQuery().toLowerCase().trim();
    const role = this.authService.currentRole();
    const nurseId = this.authService.currentUserId();

    let list = [...this.patients()];

    // Secondary in-memory guard for nurse isolation (primary filter is done on backend)
    if (role === 'Nurse') {
      list = list.filter(p => p.assignedNurseId === nurseId);
    }

    if (q) {
      list = list.filter(p =>
        `${p.firstName} ${p.lastName}`.toLowerCase().includes(q) ||
        p.diagnosis.toLowerCase().includes(q) ||
        p.assignedNurse.toLowerCase().includes(q)
      );
    }

    return list.sort((a, b) => this.compareByStatus(a, b));
  });

  // ─── Stats ────────────────────────────────────────────────────────────────────
  readonly activeCount = computed(() => {
    const role = this.authService.currentRole();
    return role === 'Nurse'
      ? this.filteredPatients().length
      : this.filteredPatients().filter(p => p.status === 'Active').length;
  });

  readonly criticalCount     = computed(() => this.filteredPatients().filter(p => p.status === 'Critical').length);
  readonly pendingTasksCount = computed(() => this._tasks().filter(t => t.status === 'REQUESTED').length);

  // ─── Chart data ───────────────────────────────────────────────────────────────
  readonly donutData = computed(() => {
    const total = this.filteredPatients().length;
    const critical = this.criticalCount();
    const normal = total - critical;
    const normalPct   = total ? Math.round((normal   / total) * 100) : 0;
    const criticalPct = 100 - normalPct;
    return {
      labels: ['Normal', 'Critical'],
      datasets: [{ data: [normal, critical], backgroundColor: ['#5f8d5f', '#b92b27'], hoverBackgroundColor: ['#4d7a4d', '#9e2420'], borderWidth: 0 }],
      normalPct,
      criticalPct,
    };
  });

  readonly symptomsData = computed(() => {
    const counts: Record<string, number> = { Fatigue: 0, 'Shortness of Breath': 0, Nausea: 0, Insomnia: 0, Constipation: 0 };
    const today = new Date().toISOString().split('T')[0];
    this.filteredPatients().forEach(p => {
      const checkIn = this._healthStatuses().find(h => h.patientId === p.id && h.timestamp.startsWith(today));
      checkIn?.symptoms?.forEach(s => { if (s in counts) counts[s]++; });
    });
    return {
      labels: Object.keys(counts),
      datasets: [{ data: Object.values(counts), backgroundColor: '#5f8d5f', hoverBackgroundColor: '#4d7a4d', borderRadius: 6, borderWidth: 0 }],
    };
  });

  constructor(
    private readonly userSvc: UserService,
    private readonly detailsSvc: PatientDetailsService,
    private readonly vitalsSvc: VitalsService,
    private readonly taskSvc: TaskService,
    private readonly healthStatusSvc: HealthStatusService,
    private readonly authService: AuthService,
  ) {}

  // ─── Lifecycle ────────────────────────────────────────────────────────────────
  init(): void {
    this.loading.set(true);
    this.loadInitialData();
    this.setupWebSockets();
  }

  destroy(): void {
    this.subs.forEach(s => s.unsubscribe());
    this.subs = [];
  }

  private loadInitialData(): void {
    const role    = this.authService.currentRole();
    const nurseId = this.authService.currentUserId();

    // For nurses: fetch only their patients from the backend (Phase 2 principle).
    const detailsObs = role === 'Nurse' && nurseId
      ? this.detailsSvc.getByNurseId(nurseId)
      : this.detailsSvc.getAll();

    this.subs.push(
      forkJoin({
        users:   this.userSvc.getByRole('PATIENT'),
        details: detailsObs,
        tasks:   this.taskSvc.getAll(),
        vitals:  this.vitalsSvc.getAll(),
        health:  this.healthStatusSvc.getAll(),
      }).subscribe({
        next: (res: any) => {
          this._users.set(res.users.content || []);
          this._details.set(res.details.content || []);
          this._tasks.set(res.tasks.content || []);
          this._vitals.set(res.vitals.content || []);
          this._healthStatuses.set(res.health.content || []);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      })
    );
  }

  private setupWebSockets(): void {
    this.subs.push(
      // Users
      this.userSvc.listenToUpdates('/topic/users').subscribe((u: User) => {
        this._users.update(list => {
          const idx = list.findIndex(i => i.id === u.id);
          return idx >= 0 ? list.map((i, n) => (n === idx ? u : i)) : [...list, u];
        });
      }),
      this.userSvc.listenToUpdates('/topic/users/deleted').subscribe((res: any) => {
        this._users.update(list => list.filter(u => u.id !== res.id));
      }),

      // Patient Details
      this.detailsSvc.listenToUpdates('/topic/patient-details').subscribe((d: PatientDetails) => {
        this._details.update(list => {
          const idx = list.findIndex(i => i.id === d.id);
          return idx >= 0 ? list.map((i, n) => (n === idx ? d : i)) : [...list, d];
        });
      }),
      this.detailsSvc.listenToUpdates('/topic/patient-details/deleted').subscribe((res: any) => {
        this._details.update(list => list.filter(d => d.id !== res.id));
      }),

      // Vitals
      this.vitalsSvc.listenToUpdates('/topic/vitals').subscribe((v: Vitals) => {
        this._vitals.update(list => {
          const idx = list.findIndex(i => i.id === v.id);
          return idx >= 0 ? list.map((i, n) => (n === idx ? v : i)) : [...list, v];
        });
      }),
      this.vitalsSvc.listenToUpdates('/topic/vitals/deleted').subscribe((res: any) => {
        this._vitals.update(list => list.filter(v => v.id !== res.id));
      }),

      // Tasks
      this.taskSvc.listenToUpdates('/topic/tasks').subscribe((t: Task) => {
        this._tasks.update(list => {
          const idx = list.findIndex(i => i.id === t.id);
          return idx >= 0 ? list.map((i, n) => (n === idx ? t : i)) : [...list, t];
        });
      }),
      this.taskSvc.listenToUpdates('/topic/tasks/deleted').subscribe((res: any) => {
        this._tasks.update(list => list.filter(t => t.id !== res.id));
      }),

      // Health Status
      this.healthStatusSvc.listenToUpdates('/topic/health-status').subscribe((h: HealthStatus) => {
        this._healthStatuses.update(list => {
          const idx = list.findIndex(i => i.id === h.id);
          return idx >= 0 ? list.map((i, n) => (n === idx ? h : i)) : [...list, h];
        });
      }),
      this.healthStatusSvc.listenToUpdates('/topic/health-status/deleted').subscribe((res: any) => {
        this._healthStatuses.update(list => list.filter(h => h.id !== res.id));
      }),
    );
  }

  // ─── Helpers ──────────────────────────────────────────────────────────────────
  compareByStatus(a: Patient, b: Patient): number {
    const p1 = STATUS_PRIORITY[a.status] ?? 99;
    const p2 = STATUS_PRIORITY[b.status] ?? 99;
    if (p1 !== p2) return p1 - p2;
    if (a.status === 'Critical') {
      const t1 = this.getLastVitalTime(a.id)?.getTime() ?? 0;
      const t2 = this.getLastVitalTime(b.id)?.getTime() ?? 0;
      return t1 - t2;
    }
    return 0;
  }

  getLastVitalTime(id: string): Date | null {
    const v = this._vitals()
      .filter(v => v.patientId === id)
      .sort((a, b) => b.timestamp.localeCompare(a.timestamp))[0];
    return v ? new Date(v.timestamp) : null;
  }

  getSeverity(status: string): 'success' | 'danger' | 'warn' {
    return ({ Active: 'success', Inactive: 'warn', Critical: 'danger' } as any)[status] ?? 'success';
  }
}
