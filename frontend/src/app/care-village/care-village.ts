import { Component, computed, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { forkJoin, Subscription } from 'rxjs';

import { AuthService } from '../../auth-service/auth.service';
import { TaskService } from '../cruds/services/taskService';
import { UserService } from '../cruds/services/userService';
import { PatientDetailsService } from '../cruds/services/patientDetailsService';
import { ToastService } from '../toast-service/toast-service';

import { Task } from '../cruds/models/task';
import { User } from '../cruds/models/user';

const TASK_TYPES = ['MEDICATION', 'CHECKUP', 'THERAPY', 'ERRAND', 'COMPANIONSHIP', 'OTHER'];

@Component({
  selector: 'app-care-village',
  standalone: true,
  imports: [CommonModule, FormsModule, DialogModule, ButtonModule, SelectModule, InputTextModule, TextareaModule],
  templateUrl: './care-village.html',
  styleUrl: './care-village.css'
})
export class CareVillageComponent implements OnInit, OnDestroy {
  private readonly authService = inject(AuthService);
  private readonly taskSvc = inject(TaskService);
  private readonly userSvc = inject(UserService);
  private readonly patientDetailsSvc = inject(PatientDetailsService);
  private readonly toastSvc = inject(ToastService);

  private subs: Subscription[] = [];

  private readonly _tasks = signal<Task[]>([]);
  private readonly _patients = signal<User[]>([]);
  readonly loading = signal(true);

  readonly activeFilter = signal<string>('ALL');
  readonly taskTypes = ['ALL', ...TASK_TYPES];
  readonly taskTypeOptions = TASK_TYPES.map(t => ({ label: t, value: t }));

  showCreateDialog = signal(false);
  newTask = { title: '', description: '', taskType: 'MEDICATION', neededBy: '', patientId: '', remote: false };

  readonly userRole = computed(() => this.authService.currentRole());
  readonly userId = computed(() => this.authService.currentUserId());
  readonly canManage = computed(() => ['Admin', 'Nurse'].includes(this.userRole()));

  readonly patientOptions = computed(() =>
    this._patients().map(p => ({ label: `${p.firstName} ${p.lastName}`, value: p.id }))
  );

  readonly filteredTasks = computed(() => {
    const filter = this.activeFilter();
    const tasks = this._tasks();
    return filter === 'ALL' ? tasks : tasks.filter(t => t.taskType === filter);
  });

  readonly openTasks = computed(() => this.filteredTasks().filter(t => t.status === 'OPEN'));
  readonly claimedTasks = computed(() => this.filteredTasks().filter(t => t.status === 'CLAIMED'));
  readonly completedTasks = computed(() => this.filteredTasks().filter(t => t.status === 'COMPLETED'));

  ngOnInit(): void {
    this.loadData();
    this.subs.push(
      this.taskSvc.listenToUpdates('/topic/tasks').subscribe((t: Task) => {
        this._tasks.update(list => {
          const idx = list.findIndex(x => x.id === t.id);
          return idx >= 0 ? list.map((x, i) => (i === idx ? t : x)) : [t, ...list];
        });
      })
    );
  }

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
  }

  private loadData(): void {
    const role = this.userRole();
    const id = this.userId();

    if (role === 'Patient') {
      forkJoin({
        tasks: this.taskSvc.getByPatientId(id, 0, 200),
        patients: this.userSvc.getByRole('PATIENT')
      }).subscribe({
        next: (res: any) => {
          this._tasks.set(res.tasks.content || []);
          this._patients.set(res.patients.content || []);
          this.loading.set(false);
        },
        error: () => this.loading.set(false)
      });
    } else if (role === 'Family') {
      this.patientDetailsSvc.getByUserId(id).subscribe({
        next: (details: any) => {
          const patientId = details?.userId;
          if (patientId) {
            this.taskSvc.getByPatientId(patientId, 0, 200).subscribe({
              next: (res: any) => { this._tasks.set(res.content || []); this.loading.set(false); },
              error: () => this.loading.set(false)
            });
          } else {
            this.loading.set(false);
          }
        },
        error: () => this.loading.set(false)
      });
    } else {
      forkJoin({
        tasks: this.taskSvc.getAll(0, 200),
        patients: this.userSvc.getByRole('PATIENT')
      }).subscribe({
        next: (res: any) => {
          this._tasks.set(res.tasks.content || []);
          this._patients.set(res.patients.content || []);
          this.loading.set(false);
        },
        error: () => this.loading.set(false)
      });
    }
  }

  getPatientName(patientId: string): string {
    const p = this._patients().find(x => x.id === patientId);
    return p ? `${p.firstName} ${p.lastName}` : 'Patient';
  }

  claimTask(task: Task): void {
    const updated: Task = { ...task, status: 'CLAIMED', claimerId: this.userId() };
    this.taskSvc.update(task.id, updated).subscribe({
      next: () => this.toastSvc.showSuccess('Task claimed'),
      error: () => this.toastSvc.showError('Failed to claim task')
    });
  }

  completeTask(task: Task): void {
    const updated: Task = { ...task, status: 'COMPLETED' };
    this.taskSvc.update(task.id, updated).subscribe({
      next: () => this.toastSvc.showSuccess('Task completed'),
      error: () => this.toastSvc.showError('Failed to complete task')
    });
  }

  openCreateDialog(): void {
    this.newTask = { title: '', description: '', taskType: 'MEDICATION', neededBy: '', patientId: '', remote: false };
    this.showCreateDialog.set(true);
  }

  submitCreateTask(): void {
    if (!this.newTask.title || !this.newTask.patientId) {
      this.toastSvc.showError('Title and patient are required');
      return;
    }
    const payload: Task = {
      id: '',
      title: this.newTask.title,
      description: this.newTask.description,
      taskType: this.newTask.taskType,
      neededBy: this.newTask.neededBy ? new Date(this.newTask.neededBy).toISOString() : new Date().toISOString(),
      status: 'OPEN',
      patientId: this.newTask.patientId,
      claimerId: '',
      remote: this.newTask.remote
    };
    this.taskSvc.create(payload).subscribe({
      next: () => { this.toastSvc.showSuccess('Task created'); this.showCreateDialog.set(false); },
      error: () => this.toastSvc.showError('Failed to create task')
    });
  }
}
