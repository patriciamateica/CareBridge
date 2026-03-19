import {Component, inject} from '@angular/core';
import {Router, RouterLink, RouterLinkActive} from "@angular/router";
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
  private router = inject(Router);
  visible: boolean = false;
  userRole: string = 'admin';

  public menuSections = [
    {
      label: 'Main',
      roles: ['admin', 'nurse', 'patient'],
      items: [
        { path: '/home', title: 'Home', roles: ['admin', 'nurse', 'patient'] },
        { path: '/village', title: 'Care Village', roles: ['admin', 'patient'] },
        { path: '/appointments', title: 'Appointments', roles: ['admin', 'nurse'] },
        { path: '/theme', title: 'Theme', roles: ['admin'] }

      ]
    },
    {
      label: 'Medical',
      roles: ['admin', 'nurse', 'patient'],
      items: [
        { path: '/patients', title: 'Patients', roles: ['admin', 'nurse'] },
        { path: '/medication', title: 'Medication', roles: ['admin', 'nurse', 'patient'] },
        { path: '/records', title: 'Medical Records', roles: ['admin', 'nurse', 'patient'] },
        { path: '/requests', title: 'Requests', roles: ['admin', 'nurse'] }
      ]
    }
  ];
}
