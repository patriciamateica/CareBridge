import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { PaginatorModule } from 'primeng/paginator';
import { Subscription } from 'rxjs';
import { signal } from '@angular/core';
import './audit-logs.css';

@Component({
  selector: 'app-audit-logs',
  standalone: true,
  imports: [CommonModule, TableModule, TagModule, PaginatorModule],
  templateUrl: './audit-logs.html',
  styleUrl: './audit-logs.css',
})
export class AuditLogsComponent implements OnInit, OnDestroy {
  private subs: Subscription[] = [];

  readonly auditLogs = signal<any[]>([]);
  readonly loading = signal(false);
  readonly selectedRows = signal<any[]>([]);

  rows = 10;
  totalRecords = 0;
  first = 0;


  ngOnInit(): void {
    this.loadAuditLogs(0, this.rows);
  }

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
  }

  private loadAuditLogs(page: number, pageSize: number): void {
    this.loading.set(true);

    const mockLogs = this.generateMockAuditLogs(page, pageSize);

    setTimeout(() => {
      this.auditLogs.set(mockLogs);
      this.totalRecords = 50;
      this.loading.set(false);
    }, 500);
  }

  onPageChange(event: any): void {
    this.first = event.first;
    const page = event.first / event.rows;
    this.loadAuditLogs(page, event.rows);
  }

  onSelectionChange(event: any): void {
    this.selectedRows.set(event.value);
  }

  private generateMockAuditLogs(page: number, pageSize: number): any[] {
    const actions = ['User Login', 'Create Patient', 'Update Patient', 'Delete Patient', 'View Report', 'Export Data', 'Change Password'];
    const resources = ['Patient', 'Task', 'Report', 'Account', 'Prescription'];
    const statuses = ['SUCCESS', 'FAILURE'];
    const usernames = ['admin@carebridge.local', 'nurse@carebridge.local', 'patient1@carebridge.local'];
    const roles = ['ADMIN', 'NURSE', 'PATIENT'];

    const logs = [];
    const startIdx = page * pageSize;

    for (let i = startIdx; i < startIdx + pageSize && i < 50; i++) {
      const timestamp = new Date(Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000);
      logs.push({
        id: `log-${i}`,
        timestamp: timestamp.toISOString(),
        username: usernames[i % usernames.length],
        role: roles[i % roles.length],
        action: actions[i % actions.length],
        resource: resources[i % resources.length],
        status: statuses[Math.floor(Math.random() * statuses.length)],
        ipAddress: `192.168.${Math.floor(Math.random() * 255)}.${Math.floor(Math.random() * 255)}`,
        details: 'System audit log entry',
      });
    }

    return logs;
  }

  getStatusSeverity(status: string): 'success' | 'danger' | 'info' {
    switch (status) {
      case 'SUCCESS': return 'success';
      case 'FAILURE': return 'danger';
      default: return 'info';
    }
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString();
  }
}

