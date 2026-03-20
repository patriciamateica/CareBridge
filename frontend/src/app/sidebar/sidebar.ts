import {Component} from '@angular/core';
import {RouterLink, RouterLinkActive} from "@angular/router";
import {CommonModule} from "@angular/common";
import {Button} from "primeng/button";
import {Drawer} from "primeng/drawer";

@Component({
  selector: 'app-sidebar',
  imports: [CommonModule, RouterLink, RouterLinkActive, Button, Drawer],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css',
})
export class Sidebar {
  visible: boolean = false;
  userRole: string = 'admin';

  public menuSections = [
    {
      label: 'MAIN',
      roles: ['admin', 'nurse', 'patient'],
      items: [
        { path: '/dashboard/home-nurse', title: 'Home Nurse', roles: ['admin', 'nurse'] },
        { path: '/dashboard/home-patient', title: 'Home Patient', roles: ['admin', 'patient'] },
        { path: '/dashboard/village', title: 'Care Village', roles: ['admin', 'patient'] },
        { path: '/dashboard/appointments', title: 'Appointments', roles: ['admin', 'nurse'] },
        { path: '/dashboard/theme', title: 'Theme', roles: ['admin'] }
      ]
    },
    {
      label: 'MEDICAL',
      roles: ['admin', 'nurse', 'patient'],
      items: [
        { path: '/dashboard/patient', title: 'Patients', roles: ['admin', 'nurse'] },
        { path: '/dashboard/medication', title: 'Medication', roles: ['admin', 'nurse', 'patient'] },
        { path: '/dashboard/records', title: 'Medical Records', roles: ['admin', 'nurse', 'patient'] },
        { path: '/dashboard/requests', title: 'Requests', roles: ['admin', 'nurse'] }
      ]
    }
  ];
}
