import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Vitals } from '../models/vitals';
import { WsStompService } from './ws-stomp.service';
import { buildApiUrl } from '../../api-config';

@Injectable({ providedIn: 'root' })
export class VitalsService {
  private readonly apiUrl = buildApiUrl('/api/vitals');

  constructor(private readonly http: HttpClient, private readonly ws: WsStompService) {}

  getAll(page = 0, size = 200): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}?page=${page}&size=${size}`).pipe(
      catchError(err => { console.error('[VitalsService] getAll', err); return throwError(() => err); })
    );
  }

  getByPatientId(patientId: string, page = 0, size = 20): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/patient/${patientId}?page=${page}&size=${size}`).pipe(
      catchError(err => { console.error('[VitalsService] getByPatientId', err); return throwError(() => err); })
    );
  }

  getById(id: string): Observable<Vitals> {
    return this.http.get<Vitals>(`${this.apiUrl}/${id}`).pipe(
      catchError(err => { console.error('[VitalsService] getById', err); return throwError(() => err); })
    );
  }

  create(data: Vitals): Observable<Vitals> {
    return this.http.post<Vitals>(this.apiUrl, data).pipe(
      catchError(err => { console.error('[VitalsService] create', err); return throwError(() => err); })
    );
  }

  update(id: string, data: Vitals): Observable<Vitals> {
    return this.http.put<Vitals>(`${this.apiUrl}/${id}`, data).pipe(
      catchError(err => { console.error('[VitalsService] update', err); return throwError(() => err); })
    );
  }

  delete(id: string): Observable<boolean> {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`).pipe(
      catchError(err => { console.error('[VitalsService] delete', err); return throwError(() => err); })
    );
  }

  /** @deprecated Use WsStompService.listen() directly or via a facade service. */
  listenToUpdates(topic: string): Observable<any> {
    return this.ws.listen(topic);
  }
}
