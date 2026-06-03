import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { CareNotes } from '../models/careNotes';
import { WsStompService } from './ws-stomp.service';
import { buildApiUrl } from '../../api-config';

@Injectable({ providedIn: 'root' })
export class CareNotesService {
  private readonly apiUrl = buildApiUrl('/api/care-notes');

  constructor(private readonly http: HttpClient, private readonly ws: WsStompService) {}

  getAll(page = 0, size = 200): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}?page=${page}&size=${size}`).pipe(
      catchError(err => { console.error('[CareNotesService] getAll', err); return throwError(() => err); })
    );
  }

  getByPatientId(patientId: string, page = 0, size = 5): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/patient/${patientId}?page=${page}&size=${size}`).pipe(
      catchError(err => { console.error('[CareNotesService] getByPatientId', err); return throwError(() => err); })
    );
  }

  getById(id: string): Observable<CareNotes> {
    return this.http.get<CareNotes>(`${this.apiUrl}/${id}`).pipe(
      catchError(err => { console.error('[CareNotesService] getById', err); return throwError(() => err); })
    );
  }

  create(data: CareNotes): Observable<CareNotes> {
    return this.http.post<CareNotes>(this.apiUrl, data).pipe(
      catchError(err => { console.error('[CareNotesService] create', err); return throwError(() => err); })
    );
  }

  update(id: string, data: CareNotes): Observable<CareNotes> {
    return this.http.put<CareNotes>(`${this.apiUrl}/${id}`, data).pipe(
      catchError(err => { console.error('[CareNotesService] update', err); return throwError(() => err); })
    );
  }

  delete(id: string): Observable<boolean> {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`).pipe(
      catchError(err => { console.error('[CareNotesService] delete', err); return throwError(() => err); })
    );
  }

  /** @deprecated Use WsStompService.listen() directly or via a facade service. */
  listenToUpdates(topic: string): Observable<any> {
    return this.ws.listen(topic);
  }
}
