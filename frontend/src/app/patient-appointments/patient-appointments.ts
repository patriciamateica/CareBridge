import { Component, computed, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { DatePipe, NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { DialogModule } from 'primeng/dialog';
import { TagModule } from 'primeng/tag';
import { forkJoin, Subscription } from 'rxjs';

import { AuthService } from '../../auth-service/auth.service';

import { AppointmentsService } from '../cruds/services/appointmentsService';
import { UserService } from '../cruds/services/userService';
import { RxStompService } from '../rx-stomp.service';

import { Appointments } from '../cruds/models/appointments';
import { User } from '../cruds/models/user';

interface CalendarDay {
  date: Date;
  isCurrentMonth: boolean;
  isToday: boolean;
  appointments: any[];
}

@Component({
  selector: 'app-patient-appointments',
  standalone: true,
  imports: [
    DatePipe, NgClass, FormsModule, InputTextModule,
    DialogModule, TagModule
  ],
  templateUrl: './patient-appointments.html',
  styleUrl: './patient-appointments.css',
})
export class PatientAppointmentsComponent implements OnInit, OnDestroy {
  private readonly aptSvc = inject(AppointmentsService);
  private readonly userSvc = inject(UserService);
  private readonly rxStompSvc = inject(RxStompService);
  protected readonly authService = inject(AuthService);

  private readonly _allApts = signal<Appointments[]>([]);
  private readonly _users = signal<User[]>([]);
  private readonly subscriptions: Subscription[] = [];

  searchQuery = signal('');
  currentDate = signal(new Date());
  selectedDate = signal<Date | null>(null);
  weekDays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

  showAppointmentForm = signal(false);
  newApt = { date: null, time: null, description: '' };

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
      apts: this.aptSvc.getByPatientId(currentUserId, 0, 200),
      users: this.userSvc.getByRole('NURSE')
    }).subscribe((res: any) => {
      this._allApts.set(res.apts.content || []);
      this._users.set(res.users.content || []);
    });
  }

  readonly allMyAppointments = computed(() => {
    return this._allApts()
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

  readonly sidebarAppointments = computed(() => {
    let apts = this.allMyAppointments();
    const sd = this.selectedDate();
    const q = this.searchQuery().toLowerCase().trim();
    if (sd) {
      apts = apts.filter(a => new Date(a.timeSlot).toDateString() === sd.toDateString());
    }
    if (q) {
      apts = apts.filter(a =>
        (a.description || '').toLowerCase().includes(q) ||
        this.getNurseName(a.nurseId).toLowerCase().includes(q)
      );
    }
    return apts;
  });

  getNurseName(nurseId: string): string {
    const u = this._users().find(x => x.id === nurseId);
    return u ? `${u.firstName} ${u.lastName}` : 'Unassigned';
  }

  getSeverity(status: string): 'success' | 'info' | 'warn' | 'danger' {
    switch(status.toUpperCase()) {
      case 'COMPLETED': return 'success';
      case 'SCHEDULED': return 'info';
      case 'REQUESTED': return 'warn';
      case 'URGENT':
      case 'CANCELLED': return 'danger';
      default: return 'info';
    }
  }

  getCardStatusClass(status: string): string {
    switch(status.toUpperCase()) {
      case 'SCHEDULED': return 'border-scheduled';
      case 'REQUESTED': return 'border-requested';
      case 'URGENT': return 'border-urgent';
      case 'COMPLETED': return 'border-completed';
      case 'CANCELLED': return 'border-cancelled';
      default: return 'border-default';
    }
  }

  previousMonth() { this.currentDate.set(new Date(this.currentDate().getFullYear(), this.currentDate().getMonth() - 1, 1)); }
  nextMonth() { this.currentDate.set(new Date(this.currentDate().getFullYear(), this.currentDate().getMonth() + 1, 1)); }

  selectDay(date: Date) {
    if (this.selectedDate()?.toDateString() === date.toDateString()) {
      this.selectedDate.set(null);
    } else {
      this.selectedDate.set(date);
    }
  }

  submitNewAppointment() {
    console.log("Submitting new requested appointment to Nurse:", this.newApt);
    this.showAppointmentForm.set(false);
  }

  reschedule(id: string) { console.log("Reschedule", id); }
  cancel(id: string) { console.log("Cancel", id); }
}
