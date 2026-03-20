import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';

@Component({
  selector: 'app-landing-page',
  standalone: true,
  imports: [CommonModule, ButtonModule, CardModule],
  templateUrl: './landing-page.html',
  styleUrl: './landing-page.css'
})
export class LandingPage {
  private router = inject(Router);

  features = [
    { title: 'Care Team Access', icon: 'pi pi-users', color: 'bg-emerald-100 text-emerald-800' },
    { title: 'HIPAA Compliant', icon: 'pi pi-shield', color: 'bg-ocean-100 text-ocean-800' },
    { title: '24/7 Support', icon: 'pi pi-phone', color: 'bg-sky-100 text-sky-800' }
  ];

  onLearnMore() {
    this.router.navigate(['/dashboard/home-nurse']);
  }
}
