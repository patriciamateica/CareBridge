import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { PatientDetails } from '../models/patientDetails';
import { WsStompService } from './ws-stomp.service';
import { buildApiUrl } from '../../api-config';

@Injectable({ providedIn: 'root' })
export class PatientDetailsService {

  constructor(private readonly http: HttpClient, private readonly ws: WsStompService) {}

  getAll(page = 0, size = 200): Observable<any> {
    return this.http.get<any>(buildApiUrl(`/api/patient-details?page=${page}&size=${size}`)).pipe(
      catchError(err => { console.error('[PatientDetailsService] getAll', err); return throwError(() => err); })
    );
  }

  getById(id: string): Observable<PatientDetails> {
    return this.http.get<PatientDetails>(buildApiUrl(`/api/patient-details/${id}`)).pipe(
      catchError(err => { console.error('[PatientDetailsService] getById', err); return throwError(() => err); })
    );
  }

  getByUserId(userId: string): Observable<PatientDetails> {
    return this.http.get<PatientDetails>(buildApiUrl(`/api/patient-details/user/${userId}`)).pipe(
      catchError(err => { console.error('[PatientDetailsService] getByUserId', err); return throwError(() => err); })
    );
  }

  getByNurseId(nurseId: string, page = 0, size = 50): Observable<any> {
    return this.http.get<any>(buildApiUrl(`/api/patient-details/nurse/${nurseId}?page=${page}&size=${size}`)).pipe(
      catchError(err => { console.error('[PatientDetailsService] getByNurseId', err); return throwError(() => err); })
    );
  }

  create(data: any): Observable<PatientDetails> {
    return this.http.post<PatientDetails>(buildApiUrl('/api/patient-details'), data).pipe(
      catchError(err => { console.error('[PatientDetailsService] create', err); return throwError(() => err); })
    );
  }

  update(id: string, data: any): Observable<PatientDetails> {
    return this.http.put<PatientDetails>(buildApiUrl(`/api/patient-details/${id}`), data).pipe(
      catchError(err => { console.error('[PatientDetailsService] update', err); return throwError(() => err); })
    );
  }

  delete(id: string): Observable<boolean> {
    return this.http.delete<boolean>(buildApiUrl(`/api/patient-details/${id}`)).pipe(
      catchError(err => { console.error('[PatientDetailsService] delete', err); return throwError(() => err); })
    );
  }

  /** @deprecated Use WsStompService.listen() directly or via a facade service. */
  listenToUpdates(topic: string): Observable<any> {
    return this.ws.listen(topic);
  }
}
