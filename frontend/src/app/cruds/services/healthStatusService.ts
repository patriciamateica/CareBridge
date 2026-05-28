import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { HealthStatus } from '../models/healthStatus';
import { WsStompService } from './ws-stomp.service';

@Injectable({ providedIn: 'root' })
export class HealthStatusService {
  private readonly apiUrl = '/api/health-status';

  constructor(private readonly http: HttpClient, private readonly ws: WsStompService) {}

  getAll(page = 0, size = 200): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}?page=${page}&size=${size}`).pipe(
      catchError(err => { console.error('[HealthStatusService] getAll', err); return throwError(() => err); })
    );
  }

  getByPatientId(patientId: string, page = 0, size = 10): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/patient/${patientId}?page=${page}&size=${size}`).pipe(
      catchError(err => { console.error('[HealthStatusService] getByPatientId', err); return throwError(() => err); })
    );
  }

  getById(id: string): Observable<HealthStatus> {
    return this.http.get<HealthStatus>(`${this.apiUrl}/${id}`).pipe(
      catchError(err => { console.error('[HealthStatusService] getById', err); return throwError(() => err); })
    );
  }

  create(data: HealthStatus): Observable<HealthStatus> {
    return this.http.post<HealthStatus>(this.apiUrl, data).pipe(
      catchError(err => { console.error('[HealthStatusService] create', err); return throwError(() => err); })
    );
  }

  update(id: string, data: HealthStatus): Observable<HealthStatus> {
    return this.http.put<HealthStatus>(`${this.apiUrl}/${id}`, data).pipe(
      catchError(err => { console.error('[HealthStatusService] update', err); return throwError(() => err); })
    );
  }

  delete(id: string): Observable<boolean> {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`).pipe(
      catchError(err => { console.error('[HealthStatusService] delete', err); return throwError(() => err); })
    );
  }

  /** @deprecated Use WsStompService.listen() directly or via a facade service. */
  listenToUpdates(topic: string): Observable<any> {
    return this.ws.listen(topic);
  }
}
