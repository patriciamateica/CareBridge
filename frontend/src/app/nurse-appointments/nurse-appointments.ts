import { Component, computed, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { DatePipe, NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { DialogModule } from 'primeng/dialog';
import { forkJoin, Subscription } from 'rxjs';

import { AuthService } from '../../auth-service/auth.service';

import { AppointmentsService } from '../cruds/services/appointmentsService';
import { UserService } from '../cruds/services/userService';
import { RxStompService } from '../rx-stomp.service';
import { ToastService } from '../toast-service/toast-service';

import { Appointments } from '../cruds/models/appointments';
import { User } from '../cruds/models/user';

interface CalendarDay {
  date: Date;
  isCurrentMonth: boolean;
  isToday: boolean;
  appointments: any[];
}

@Component({
  selector: 'app-nurse-appointments',
  standalone: true,
  imports: [
    DatePipe, NgClass, FormsModule, InputTextModule,
    DialogModule
  ],
  templateUrl: './nurse-appointments.html',
  styleUrl: './nurse-appointments.css',
})
export class NurseAppointmentsComponent implements OnInit, OnDestroy {
  private readonly aptSvc = inject(AppointmentsService);
  private readonly userSvc = inject(UserService);
  private readonly rxStompSvc = inject(RxStompService);
  protected readonly authService = inject(AuthService);
  private readonly toastSvc = inject(ToastService);

  private readonly _allApts = signal<Appointments[]>([]);
  private readonly _users = signal<User[]>([]);
  private readonly subscriptions: Subscription[] = [];

  searchQuery = signal('');
  currentDate = signal(new Date());
  weekDays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

  showAppointmentForm = signal(false);
  showBlockTimeForm = signal(false);

  newApt = { patientId: '', date: null, time: null, description: '' };
  blockTimeData = { date: null, startTime: null, endTime: null, reason: 'Lunch Break' };

  ngOnInit() {
    this.loadData();

    // Listen for creates and updates
    this.subscriptions.push(
      this.rxStompSvc.watch('/topic/appointments').subscribe((message) => {
        const apt: Appointments = JSON.parse(message.body);
        this._allApts.update(list => {
          const index = list.findIndex(a => a.id === apt.id);
          if (index !== -1) {
            const newList = [...list];
            newList[index] = apt;
            return newList;
          }
          return [...list, apt];
        });
      })
    );

    // Listen for deletions
    this.subscriptions.push(
      this.rxStompSvc.watch('/topic/appointments/deleted').subscribe((message) => {
        const data = JSON.parse(message.body);
        const id = data.id || data; // Handle both {id: "..."} and raw string
        this._allApts.update(list => list.filter(a => a.id !== id));
      })
    );
  }

  ngOnDestroy() {
    this.subscriptions.forEach(s => s.unsubscribe());
  }

  private loadData() {
    const currentUserId = this.authService.currentUserId();
    forkJoin({
      apts: this.aptSvc.getByNurseId(currentUserId, 0, 200),
      users: this.userSvc.getByRole('PATIENT')
    }).subscribe((res: any) => {
      this._allApts.set(res.apts.content || []);
      this._users.set(res.users.content || []);
    });
  }

  readonly allMyAppointments = computed(() => {
    const q = this.searchQuery().toLowerCase().trim();

    let apts = this._allApts();

    if (q) {
      apts = apts.filter(a =>
        (a.description || '').toLowerCase().includes(q) ||
        this.getPatientName(a.patientId).toLowerCase().includes(q)
      );
    }
    return apts;
  });

  readonly pendingRequests = computed(() => {
    return this.allMyAppointments()
      .filter(a => a.status === 'REQUESTED')
      .sort((a, b) => a.timeSlot.localeCompare(b.timeSlot));
  });

  readonly todaysAgenda = computed(() => {
    const todayStr = new Date().toDateString();
    return this.allMyAppointments()
      .filter(a => (a.status === 'SCHEDULED') && new Date(a.timeSlot).toDateString() === todayStr)
      .sort((a, b) => a.timeSlot.localeCompare(b.timeSlot));
  });

  readonly calendarGrid = computed<CalendarDay[]>(() => {
    const year = this.currentDate().getFullYear();
    const month = this.currentDate().getMonth();
    const today = new Date();
    const apts = this.allMyAppointments();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const days: CalendarDay[] = [];
    for (let i = firstDay.getDay() - 1; i >= 0; i--) {
      days.push(this.createDay(new Date(year, month - 1, new Date(year, month, 0).getDate() - i), false, today, apts));
    }
    for (let i = 1; i <= lastDay.getDate(); i++) {
      days.push(this.createDay(new Date(year, month, i), true, today, apts));
    }
    const remaining = (days.length % 7 === 0) ? 0 : 7 - (days.length % 7);
    for (let i = 1; i <= remaining; i++) {
      days.push(this.createDay(new Date(year, month + 1, i), false, today, apts));
    }
    return days;
  });

  private createDay(date: Date, isCurrentMonth: boolean, today: Date, allApts: any[]): CalendarDay {
    const isToday = date.toDateString() === today.toDateString();
    const dayApts = allApts
      .filter(a => new Date(a.timeSlot).toDateString() === date.toDateString())
      .sort((a, b) => a.timeSlot.localeCompare(b.timeSlot));
    return { date, isCurrentMonth, isToday, appointments: dayApts };
  }

  getPatientName(patientId: string): string {
    const u = this._users().find(x => x.id === patientId);
    return u ? `${u.firstName} ${u.lastName}` : 'Blocked Time';
  }

  getPatientsList() {
    return this._users().filter(u => u.roles?.includes('PATIENT')).map(p => ({ label: `${p.firstName} ${p.lastName}`, value: p.id }));
  }

  previousMonth() { this.currentDate.set(new Date(this.currentDate().getFullYear(), this.currentDate().getMonth() - 1, 1)); }
  nextMonth() { this.currentDate.set(new Date(this.currentDate().getFullYear(), this.currentDate().getMonth() + 1, 1)); }

  acceptRequest(id: string) { this.aptSvc.update(id, { status: 'SCHEDULED' } as any).subscribe(); }
  rescheduleRequest(id: string) { console.log("Open reschedule dialog for", id); }
  cancelRequest(id: string) { this.aptSvc.delete(id).subscribe(); }

  submitNewAppointment() {
    const { patientId, date, time, description } = this.newApt;
    if (!patientId || !date || !time) {
      this.toastSvc.showError('Patient, date and time are required');
      return;
    }
    const [h, m] = (time as any as string).split(':').map(Number);
    const dt = new Date(date as any);
    dt.setHours(h, m, 0, 0);
    const nurseId = this.authService.currentUserId();
    this.aptSvc.create({
      id: '',
      patientId,
      nurseId,
      description: description || '',
      timeSlot: dt.toISOString(),
      status: 'SCHEDULED'
    }).subscribe({
      next: () => { this.toastSvc.showSuccess('Appointment scheduled'); this.showAppointmentForm.set(false); },
      error: () => this.toastSvc.showError('Failed to create appointment')
    });
  }

  submitBlockedTime() {
    const { date, startTime, reason } = this.blockTimeData;
    if (!date || !startTime) {
      this.toastSvc.showError('Date and start time are required');
      return;
    }
    const [h, m] = (startTime as any as string).split(':').map(Number);
    const dt = new Date(date as any);
    dt.setHours(h, m, 0, 0);
    const nurseId = this.authService.currentUserId();
    this.aptSvc.create({
      id: '',
      patientId: nurseId,
      nurseId,
      description: reason || 'Blocked time',
      timeSlot: dt.toISOString(),
      status: 'SCHEDULED'
    }).subscribe({
      next: () => { this.toastSvc.showSuccess('Time blocked'); this.showBlockTimeForm.set(false); },
      error: () => this.toastSvc.showError('Failed to block time')
    });
  }
}
