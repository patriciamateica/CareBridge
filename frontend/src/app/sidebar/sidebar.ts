import {Component, HostBinding, HostListener, inject} from '@angular/core';
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

  public menuSections = [
    {
      label: 'MAIN',
      roles: ['admin', 'nurse', 'patient'],
      items: [
        { path: '/dashboard/home-nurse',   title: 'Home Nurse',   roles: ['admin', 'nurse'] },
        { path: '/dashboard/home-patient', title: 'Home Patient', roles: ['admin', 'patient'] },
        { path: '/dashboard/village',      title: 'Care Village', roles: ['admin', 'patient'] },
        { path: '/dashboard/appointments', title: 'Appointments', roles: ['admin', 'nurse'] }
      ]
    },
    {
      label: 'MEDICAL',
      roles: ['admin', 'nurse', 'patient'],
      items: [
        { path: '/dashboard/patient-management', title: 'Patients',        roles: ['admin', 'nurse'] },
        { path: '/dashboard/medication',         title: 'Medication',      roles: ['admin', 'nurse', 'patient'] },
        { path: '/dashboard/records',            title: 'Medical Records', roles: ['admin', 'nurse', 'patient'] },
        { path: '/dashboard/requests',           title: 'Requests',        roles: ['admin', 'nurse'] }
      ]
    }
  ];

  @HostListener('window:resize')
  onResize() {
    if (window.innerWidth >= 1024) {
      this.visible = false;
    }
  }

  logout() {
    this.cookiesService.logActivity('logout', this.authService.currentUserName());
    this.router.navigate(['/']);
  }
}
