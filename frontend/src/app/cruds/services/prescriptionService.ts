import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Prescription } from '../models/prescription';
import { WsStompService } from './ws-stomp.service';
import { buildApiUrl } from '../../api-config';

@Injectable({ providedIn: 'root' })
export class PrescriptionService {
  private readonly apiUrl = buildApiUrl('/api/prescriptions');

  constructor(private readonly http: HttpClient, private readonly ws: WsStompService) {}

  getAll(page = 0, size = 200): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}?page=${page}&size=${size}`).pipe(
      catchError(err => { console.error('[PrescriptionService] getAll', err); return throwError(() => err); })
    );
  }

  getByPatientId(patientId: string, page = 0, size = 5): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/patient/${patientId}?page=${page}&size=${size}`).pipe(
      catchError(err => { console.error('[PrescriptionService] getByPatientId', err); return throwError(() => err); })
    );
  }

  getById(id: string): Observable<Prescription> {
    return this.http.get<Prescription>(`${this.apiUrl}/${id}`).pipe(
      catchError(err => { console.error('[PrescriptionService] getById', err); return throwError(() => err); })
    );
  }

  create(data: Prescription): Observable<Prescription> {
    return this.http.post<Prescription>(this.apiUrl, data).pipe(
      catchError(err => { console.error('[PrescriptionService] create', err); return throwError(() => err); })
    );
  }

  update(id: string, data: Prescription): Observable<Prescription> {
    return this.http.put<Prescription>(`${this.apiUrl}/${id}`, data).pipe(
      catchError(err => { console.error('[PrescriptionService] update', err); return throwError(() => err); })
    );
  }

  delete(id: string): Observable<boolean> {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`).pipe(
      catchError(err => { console.error('[PrescriptionService] delete', err); return throwError(() => err); })
    );
  }

  /** @deprecated Use WsStompService.listen() directly or via a facade service. */
  listenToUpdates(topic: string): Observable<any> {
    return this.ws.listen(topic);
  }
}
