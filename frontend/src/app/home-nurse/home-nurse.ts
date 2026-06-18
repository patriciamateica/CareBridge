import { Component, computed, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ChartModule } from 'primeng/chart';
import { Subscription } from 'rxjs';

import { AuthService } from '../../auth-service/auth.service';
import { UserService } from '../cruds/services/userService';
import { PatientDetailsService } from '../cruds/services/patientDetailsService';
import { TaskService } from '../cruds/services/taskService';
import { NurseDetailsService } from '../cruds/services/nurseDetailsService';
import { AppointmentsService } from '../cruds/services/appointmentsService';

import { User } from '../cruds/models/user';
import { PatientDetails } from '../cruds/models/patientDetails';
import { Task } from '../cruds/models/task';
import { Appointments } from '../cruds/models/appointments';
import { NurseProfile } from '../nurse-profile/nurse-profile';

@Component({
  selector: 'app-home-nurse',
  standalone: true,
  imports: [CommonModule, ChartModule, DatePipe, NurseProfile],
  templateUrl: './home-nurse.html',
  styleUrl: './home-nurse.css',
})
export class HomeNurse implements OnInit, OnDestroy {
  private readonly authService      = inject(AuthService);
  private readonly userSvc          = inject(UserService);
  private readonly detailsSvc       = inject(PatientDetailsService);
  private readonly taskSvc          = inject(TaskService);
  private readonly nurseDetailsSvc  = inject(NurseDetailsService);
  private readonly aptSvc           = inject(AppointmentsService);

  private subs: Subscription[] = [];

  private readonly _users   = signal<User[]>([]);
  private readonly _details = signal<PatientDetails[]>([]);
  private readonly _tasks   = signal<Task[]>([]);
  private readonly _appointments = signal<Appointments[]>([]);

  private readonly _taskPage     = signal(0);
  private readonly _hasMoreTasks = signal(false);
  readonly loadingTasks          = signal(false);

  readonly currentDate = signal(new Date());

  readonly nurseName = computed(() => this.authService.currentUserName());

  readonly profileDialogVisible = signal(false);
  readonly profileData = signal<any>(null);

  readonly tasks = computed(() => this._tasks());

  readonly todayItems = computed(() => {
    const todayStr = new Date().toDateString();
    const taskItems = this._tasks()
      .filter(t => t.status !== 'COMPLETED' && t.neededBy && new Date(t.neededBy).toDateString() === todayStr)
      .map(t => ({ kind: 'task' as const, time: t.neededBy, label: t.title, sub: t.taskType, id: t.id }));
    const aptItems = this._appointments()
      .filter(a => a.status === 'SCHEDULED' && new Date(a.timeSlot).toDateString() === todayStr)
      .map(a => ({ kind: 'apt' as const, time: a.timeSlot, label: a.description || 'Appointment', sub: 'APPOINTMENT', id: a.id }));
    return [...taskItems, ...aptItems].sort((a, b) => new Date(a.time).getTime() - new Date(b.time).getTime());
  });

  readonly patients = computed(() => {
    const details = this._details();
    const users   = this._users();
    return details.map(d => {
      const u = users.find(user => user.id === d.userId);
      return {
        id: d.userId,
        firstName: d.patientFirstName || u?.firstName || 'Unknown',
        lastName: d.patientLastName || u?.lastName || 'Patient',
        status: d.status ?? 'Active'
      };
    });
  });

  readonly criticalCount  = computed(() => this.patients().filter(p => p.status === 'Critical').length);
  readonly totalCount     = computed(() => this.patients().length);
  readonly normalCount    = computed(() => this.totalCount() - this.criticalCount());

  readonly alerts = computed(() =>
    this.patients()
      .filter(p => p.status === 'Critical')
      .map(p => ({
        id: p.id,
        patientName: `${p.firstName} ${p.lastName}`,
        description: 'Realtime alert: elevated pain score or vitals threshold exceeded',
      }))
  );

  readonly criticalPercent = computed(() =>
    this.totalCount() ? Math.round((this.criticalCount() / this.totalCount()) * 100) : 0
  );
  readonly normalPercent = computed(() => 100 - this.criticalPercent());

  readonly chartData = computed(() => ({
    labels: ['Critical', 'Normal'],
    datasets: [{
      data: [this.criticalCount(), this.normalCount()],
      backgroundColor: ['#b32d2d', '#6a966a'],
      hoverBackgroundColor: ['#9e2424', '#5a825a'],
      borderWidth: 0,
    }],
  }));

  readonly chartOptions = {
    cutout: '80%',
    plugins: { legend: { display: false }, tooltip: { enabled: true } },
    maintainAspectRatio: false,
    responsive: true,
  };

  ngOnInit(): void {
    this.loadInitialData();
    this.setupWebSockets();
  }

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
  }

  private loadInitialData(): void {
    this.loadingTasks.set(true);
    const nurseId = this.authService.currentUserId();
    this.subs.push(
      this.userSvc.getByRole('PATIENT').subscribe((res: any) => this._users.set(res.content || [])),
      this.detailsSvc.getByNurseId(nurseId).subscribe((res: any) => this._details.set(res.content || [])),
      this.nurseDetailsSvc.getByUserId(nurseId).subscribe({
        next: (details) => this.profileData.set(details),
        error: () => {}
      }),
      this.aptSvc.getByNurseId(nurseId, 0, 200).subscribe({
        next: (res: any) => this._appointments.set(res.content || []),
        error: () => {}
      })
    );
    this.loadTaskPage(0);
  }

  private setupWebSockets(): void {
    this.subs.push(
      this.taskSvc.listenToUpdates('/topic/tasks').subscribe((t: Task) => {
        this._tasks.update(list => {
          const idx = list.findIndex(i => i.id === t.id);
          return idx >= 0 ? list.map((i, n) => (n === idx ? t : i)) : [t, ...list];
        });
      }),
      this.taskSvc.listenToUpdates('/topic/tasks/deleted').subscribe((res: any) => {
        this._tasks.update(list => list.filter(t => t.id !== res.id));
      }),
      this.taskSvc.listenToUpdates('/topic/appointments').subscribe((a: Appointments) => {
        this._appointments.update(list => {
          const idx = list.findIndex(i => i.id === a.id);
          return idx >= 0 ? list.map((i, n) => (n === idx ? a : i)) : [a, ...list];
        });
      }),
    );
  }

  private readonly PAGE_SIZE = 10;

  private loadTaskPage(page: number): void {
    this.loadingTasks.set(true);
    const nurseId = this.authService.currentUserId();
    this.subs.push(
      this.taskSvc.getByNurseId(nurseId, page, this.PAGE_SIZE).subscribe({
        next: (res: any) => {
          const content: Task[] = res.content || [];
          if (page === 0) {
            this._tasks.set(content);
          } else {
            this._tasks.update(prev => [...prev, ...content]);
          }
          this._taskPage.set(page);
          this._hasMoreTasks.set(!res.last);
          this.loadingTasks.set(false);
        },
        error: () => this.loadingTasks.set(false),
      })
    );
  }

  onTaskListScroll(event: Event): void {
    if (!this._hasMoreTasks() || this.loadingTasks()) return;
    const el = event.target as HTMLElement;
    if (el.scrollTop + el.clientHeight >= el.scrollHeight - 40) {
      this.loadTaskPage(this._taskPage() + 1);
    }
  }
}
