import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { ButtonModule } from 'primeng/button';
import { AuditService, SuspiciousUser } from '../audit.service';
import { ToastService } from '../../toast-service/toast-service';

@Component({
  selector: 'app-observation-list',
  standalone: true,
  imports: [CommonModule, TableModule, TagModule, ButtonModule],
  templateUrl: './observation-list.html',
  styleUrl: './observation-list.css'
})
export class ObservationList implements OnInit {
  private readonly auditService = inject(AuditService);
  private readonly toastService = inject(ToastService);

  suspiciousUsers = signal<SuspiciousUser[]>([]);

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.auditService.getSuspiciousUsers().subscribe({
      next: (data) => this.suspiciousUsers.set(data),
      error: () => this.toastService.showError('Failed to load observation list')
    });
  }

  resolve(id: string) {
    this.auditService.resolveUser(id).subscribe({
      next: () => {
        this.toastService.showSuccess('User risk flagged as resolved');
        this.loadData();
      },
      error: () => this.toastService.showError('Failed to resolve flag')
    });
  }

  getSeverity(severity: string): 'danger' | 'warn' | 'info' | 'success' {
    switch (severity) {
      case 'HIGH': return 'danger';
      case 'MEDIUM': return 'warn';
      case 'LOW': return 'info';
      default: return 'info';
    }
  }
}
