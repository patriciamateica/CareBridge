import { Component, computed, inject, signal, OnInit } from '@angular/core';
import { NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';

import { AuthService } from '../../auth-service/auth.service';
import { UserService } from '../cruds/services/userService';
import { NurseDetailsService } from '../cruds/services/nurseDetailsService';
import { PatientDetailsService } from '../cruds/services/patientDetailsService';
import { AppointmentsService } from '../cruds/services/appointmentsService';
import { ToastService } from '../toast-service/toast-service';

import { User } from '../cruds/models/user';
import { NurseDetails } from '../cruds/models/nurseDetails';

export interface NurseCard {
  user: User;
  details: NurseDetails | null;
}

@Component({
  selector: 'app-find-nurse',
  standalone: true,
  imports: [NgClass, FormsModule],
  templateUrl: './find-nurse.html',
  styleUrl: './find-nurse.css',
})
export class FindNurseComponent implements OnInit {
  private readonly userSvc = inject(UserService);
  private readonly nurseDetailsSvc = inject(NurseDetailsService);
  private readonly patientDetailsSvc = inject(PatientDetailsService);
  private readonly appointmentsSvc = inject(AppointmentsService);
  protected readonly authService = inject(AuthService);
  private readonly toastService = inject(ToastService);

  private readonly _nurses = signal<NurseCard[]>([]);
  private assignedNurseId = signal<string>('');
  private patientDetailsId = signal<string>('');

  searchQuery = signal('');
  activeSpec = signal('All');
  requestingNurseId = signal<string | null>(null);

  ngOnInit() {
    const userId = this.authService.currentUserId();

    this.patientDetailsSvc.getByUserId(userId).subscribe({
      next: (d) => {
        this.assignedNurseId.set(d.assignedNurseId || '');
        this.patientDetailsId.set(d.id);
      },
      error: () => {}
    });

    forkJoin({
      users: this.userSvc.getByRole('NURSE', 0, 100),
      details: this.nurseDetailsSvc.getAll()
    }).subscribe({
      next: ({ users, details }: any) => {
        const nurseUsers: User[] = users.content || [];
        const nurseDetails: NurseDetails[] = details.content || details || [];
        const cards: NurseCard[] = nurseUsers.map(u => ({
          user: u,
          details: nurseDetails.find((d: NurseDetails) => d.userId === u.id) || null
        }));
        this._nurses.set(cards);
      },
      error: () => {}
    });
  }

  readonly specializations = computed(() => {
    const specs = this._nurses()
      .map(n => n.details?.specialization)
      .filter(Boolean) as string[];
    return ['All', ...Array.from(new Set(specs))];
  });

  readonly filteredNurses = computed(() => {
    const q = this.searchQuery().toLowerCase().trim();
    const spec = this.activeSpec();
    return this._nurses()
      .filter(n => spec === 'All' || n.details?.specialization === spec)
      .filter(n => {
        if (!q) return true;
        const name = `${n.user.firstName} ${n.user.lastName}`.toLowerCase();
        const s = (n.details?.specialization || '').toLowerCase();
        const h = (n.details?.hospitalAffiliation || '').toLowerCase();
        return name.includes(q) || s.includes(q) || h.includes(q);
      });
  });

  isAssignedNurse(nurseId: string): boolean {
    return this.assignedNurseId() === nurseId;
  }

  requestVisit(nurse: NurseCard) {
    const patientId = this.authService.currentUserId();
    const nurseId = nurse.user.id;

    if (!nurseId) return;

    this.requestingNurseId.set(nurseId);

    const timeSlot = new Date();
    timeSlot.setDate(timeSlot.getDate() + 3);
    timeSlot.setHours(10, 0, 0, 0);

    const appointment: any = {
      patientId,
      nurseId,
      description: `Visit request for ${nurse.user.firstName} ${nurse.user.lastName}`,
      timeSlot: timeSlot.toISOString().slice(0, 16),
      status: 'REQUESTED'
    };

    this.appointmentsSvc.create(appointment).subscribe({
      next: () => {
        this.toastService.showSuccess(`Visit request sent to ${nurse.user.firstName} ${nurse.user.lastName}.`);
        this.requestingNurseId.set(null);
      },
      error: () => {
        this.toastService.showError('Could not send visit request. Please try again.');
        this.requestingNurseId.set(null);
      }
    });
  }

  getInitials(user: User): string {
    return `${user.firstName?.[0] ?? ''}${user.lastName?.[0] ?? ''}`.toUpperCase();
  }
}
