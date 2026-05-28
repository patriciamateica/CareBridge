import {Component, HostListener, inject} from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Button } from 'primeng/button';
import { Drawer } from 'primeng/drawer';
import {AuthService} from '../../auth-service/auth.service';
import {CookiesService} from '../../cookies/cookieservice';

@Component({
  selector: 'app-sidebar',
  imports: [CommonModule, RouterLink, RouterLinkActive, Button, Drawer],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css',
})
export class Sidebar {
  visible: boolean = false;
  private readonly router = inject(Router);
  protected readonly authService = inject(AuthService);
  private cookiesService = inject(CookiesService);

  get userRole(): string {
    return this.authService.currentRole();
  }

  menuSections = [
    {
      label: 'MAIN',
      roles: ['Admin', 'Nurse', 'Patient', 'Family'],
      items: [
        { path: '/dashboard/home-nurse', title: 'Home Nurse', roles: ['Admin', 'Nurse'] },
        { path: '/dashboard/home-patient', title: 'Home Patient', roles: ['Patient'] },
        { path: '/dashboard/village',      title: 'Care Village', roles: ['Admin', 'Nurse', 'Patient', 'Family'] },
        { path: '/dashboard/patient-appointments', title: 'Care Schedule', roles: ['Admin', 'Patient'] },
        { path: '/dashboard/nurse-appointments', title: 'Appointments', roles: ['Admin', 'Nurse'] },
        { path: '/dashboard/chat', title: 'Community Chat', roles: ['Admin', 'Nurse', 'Patient'] }
      ]
    },
    {
      label: 'MEDICAL',
      roles: ['Admin', 'Nurse', 'Patient'],
      items: [
        { path: '/dashboard/patient-management', title: 'Patients',  roles: ['Admin', 'Nurse'] },
        { path: '/dashboard/medication',         title: 'Medication',      roles: ['Admin', 'Nurse', 'Patient'] },
        { path: '/dashboard/records',            title: 'Medical Records', roles: ['Admin', 'Nurse', 'Patient'] },
        { path: '/dashboard/requests',           title: 'Requests',        roles: ['Admin', 'Nurse'] }
      ]
    },
    {
      label: 'SECURITY',
      roles: ['Admin'],
      items: [
        { path: '/dashboard/observation-list', title: 'Observation List', roles: ['Admin'] },
        { path: '/dashboard/audit-logs', title: 'Audit Logs', roles: ['Admin'] }
      ]
    },
  ];

  @HostListener('window:resize')
  onResize() {
    if (window.innerWidth >= 1024) {
      this.visible = false;
    }
  }

  logout() {
    this.cookiesService.logActivity('logout', this.authService.currentUserName());
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
