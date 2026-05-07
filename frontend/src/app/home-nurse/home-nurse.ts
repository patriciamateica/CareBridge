import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ChartModule } from 'primeng/chart';
import { forkJoin } from 'rxjs';

import { AuthService } from '../../auth-service/auth.service';
import { UserService } from '../cruds/services/userService';
import { PatientDetailsService } from '../cruds/services/patientDetailsService';
import { TaskService } from '../cruds/services/taskService';

import { User } from '../cruds/models/user';
import { PatientDetails } from '../cruds/models/patientDetails';
import { Task } from '../cruds/models/task';

@Component({
  selector: 'app-home-nurse',
  standalone: true,
  imports: [CommonModule, ChartModule, DatePipe],
  templateUrl: './home-nurse.html',
  styleUrl: './home-nurse.css'
})
export class HomeNurse implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly userSvc = inject(UserService);
  private readonly detailsSvc = inject(PatientDetailsService);
  private readonly taskSvc = inject(TaskService);

  private readonly _localUsers = signal<User[]>([]);
  private readonly _localDetails = signal<PatientDetails[]>([]);
  private readonly _localTasks = signal<Task[]>([]);

  readonly nurseName = computed(() => this.authService.currentUserName());
  readonly currentDate = new Date();

  ngOnInit() {
    forkJoin({
      users: this.userSvc.getAll(),
      details: this.detailsSvc.getAll(),
      tasks: this.taskSvc.getAll()
    }).subscribe((res: any) => {
      this._localUsers.set(res.users.content || []);
      this._localDetails.set(res.details.content || []);
      this._localTasks.set(res.tasks.content || []);
    });

    this.taskSvc.listenToUpdates('/topic/tasks').subscribe((t: Task) => {
        this._localTasks.update(list => [...list, t]);
    });
  }

  readonly tasks = computed(() => this._localTasks().slice(0, 10));

  readonly patients = computed(() => {
    const users = this._localUsers().filter(u => u.roles?.includes('PATIENT'));
    const details = this._localDetails();
    return users.map(u => {
      const d = details.find(det => det.userId === u.id);
      return {
        id: u.id,
        firstName: u.firstName,
        lastName: u.lastName,
        status: 'Active'
      };
    });
  });

  readonly criticalCount = computed(() =>
    this.patients().filter((p) => p.status === 'Critical').length
  );

  readonly totalCount = computed(() => this.patients().length);
  readonly normalCount = computed(() => this.totalCount() - this.criticalCount());

  readonly alerts = computed(() =>
    this.patients()
      .filter((p) => p.status === 'Critical')
      .map((p) => ({
        id: p.id,
        patientName: `${p.firstName} ${p.lastName}`,
        description: 'Realtime alert: elevated pain score or vitals threshold exceeded',
      }))
  );

  readonly chartData = computed(() => ({
    labels: ['Critical', 'Normal'],
    datasets: [
      {
        data: [this.criticalCount(), this.normalCount()],
        backgroundColor: ['#b32d2d', '#6a966a'],
        hoverBackgroundColor: ['#9e2424', '#5a825a'],
        borderWidth: 0,
      }
    ]
  }));

  readonly chartOptions = {
    cutout: '80%',
    plugins: { legend: { display: false }, tooltip: { enabled: true } },
    maintainAspectRatio: false,
    responsive: true
  };

  readonly criticalPercent = computed(() =>
    this.totalCount() ? Math.round((this.criticalCount() / this.totalCount()) * 100) : 0
  );

  readonly normalPercent = computed(() => 100 - this.criticalPercent());
}
