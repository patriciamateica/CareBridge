import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { buildApiUrl } from '../api-config';

export interface SuspiciousUser {
  id: string;
  userId: string;
  username: string;
  reason: string;
  flaggedAt: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH';
  resolved: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AuditService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = buildApiUrl('/api/audit');

  getSuspiciousUsers(): Observable<SuspiciousUser[]> {
    return this.http.get<SuspiciousUser[]>(`${this.apiUrl}/suspicious`);
  }

  resolveUser(id: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/suspicious/${id}/resolve`, {});
  }

  getLogs(page: number = 0, size: number = 20): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}?page=${page}&size=${size}`);
  }
}
