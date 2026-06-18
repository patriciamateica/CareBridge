import { Component, computed, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { forkJoin, Subscription } from 'rxjs';

import { AuthService } from '../../auth-service/auth.service';
import { AppointmentsService } from '../cruds/services/appointmentsService';
import { UserService } from '../cruds/services/userService';
import { RxStompService } from '../rx-stomp.service';
import { ToastService } from '../toast-service/toast-service';

import { Appointments } from '../cruds/models/appointments';
import { User } from '../cruds/models/user';

@Component({
  selector: 'app-nurse-requests',
  standalone: true,
  imports: [CommonModule, FormsModule, InputTextModule],
  templateUrl: './nurse-requests.html',
  styleUrl: './nurse-requests.css'
})
export class NurseRequestsComponent implements OnInit, OnDestroy {
  private readonly authService = inject(AuthService);
  private readonly aptSvc = inject(AppointmentsService);
  private readonly userSvc = inject(UserService);
  private readonly rxStompSvc = inject(RxStompService);
  private readonly toastSvc = inject(ToastService);

  private subs: Subscription[] = [];

  private readonly _requests = signal<Appointments[]>([]);
  private readonly _patients = signal<User[]>([]);
  readonly loading = signal(true);

  readonly searchQuery = signal('');

  readonly filteredRequests = computed(() => {
    const q = this.searchQuery().toLowerCase().trim();
    const reqs = this._requests();
    if (!q) return reqs;
    return reqs.filter(r =>
      this.getPatientName(r.patientId).toLowerCase().includes(q) ||
      (r.description || '').toLowerCase().includes(q)
    );
  });

  ngOnInit(): void {
    this.loadData();
    this.subs.push(
      this.rxStompSvc.watch('/topic/appointments').subscribe(msg => {
        const apt: Appointments = JSON.parse(msg.body);
        if (apt.status === 'REQUESTED') {
          this._requests.update(list => {
            const idx = list.findIndex(r => r.id === apt.id);
            return idx >= 0 ? list.map((r, i) => (i === idx ? apt : r)) : [apt, ...list];
          });
        } else {
          this._requests.update(list => list.filter(r => r.id !== apt.id));
        }
      }),
      this.rxStompSvc.watch('/topic/appointments/deleted').subscribe(msg => {
        const data = JSON.parse(msg.body);
        const id = data.id || data;
        this._requests.update(list => list.filter(r => r.id !== id));
      })
    );
  }

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
  }

  private loadData(): void {
    const nurseId = this.authService.currentUserId();
    forkJoin({
      apts: this.aptSvc.getByNurseId(nurseId, 0, 200),
      patients: this.userSvc.getByRole('PATIENT')
    }).subscribe({
      next: (res: any) => {
        const all: Appointments[] = res.apts.content || [];
        this._requests.set(all.filter(a => a.status === 'REQUESTED'));
        this._patients.set(res.patients.content || []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  getPatientName(patientId: string): string {
    const p = this._patients().find(x => x.id === patientId);
    return p ? `${p.firstName} ${p.lastName}` : 'Patient';
  }

  acceptRequest(apt: Appointments): void {
    this._requests.update(list => list.filter(r => r.id !== apt.id));
    this.aptSvc.update(apt.id, { ...apt, status: 'SCHEDULED' }).subscribe({
      next: () => this.toastSvc.showSuccess('Request accepted'),
      error: () => {
        this._requests.update(list => [apt, ...list]);
        this.toastSvc.showError('Failed to accept request');
      }
    });
  }

  declineRequest(apt: Appointments): void {
    this._requests.update(list => list.filter(r => r.id !== apt.id));
    this.aptSvc.update(apt.id, { ...apt, status: 'CANCELLED' }).subscribe({
      next: () => this.toastSvc.showSuccess('Request declined'),
      error: () => {
        this._requests.update(list => [apt, ...list]);
        this.toastSvc.showError('Failed to decline request');
      }
    });
  }
}
