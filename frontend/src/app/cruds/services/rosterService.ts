import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Roster } from '../models/roster';
import { WsStompService } from './ws-stomp.service';

@Injectable({ providedIn: 'root' })
export class RosterService {
  private readonly apiUrl = '/api/rosters';

  constructor(private readonly http: HttpClient, private readonly ws: WsStompService) {}

  getAll(page = 0, size = 50): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}?page=${page}&size=${size}`).pipe(
      catchError(err => { console.error('[RosterService] getAll', err); return throwError(() => err); })
    );
  }

  getById(id: string): Observable<Roster> {
    return this.http.get<Roster>(`${this.apiUrl}/${id}`).pipe(
      catchError(err => { console.error('[RosterService] getById', err); return throwError(() => err); })
    );
  }

  getByNurseId(nurseId: string, page = 0, size = 50): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/nurse/${nurseId}?page=${page}&size=${size}`).pipe(
      catchError(err => { console.error('[RosterService] getByNurseId', err); return throwError(() => err); })
    );
  }

  create(data: Roster): Observable<Roster> {
    return this.http.post<Roster>(this.apiUrl, data).pipe(
      catchError(err => { console.error('[RosterService] create', err); return throwError(() => err); })
    );
  }

  update(id: string, data: Roster): Observable<Roster> {
    return this.http.put<Roster>(`${this.apiUrl}/${id}`, data).pipe(
      catchError(err => { console.error('[RosterService] update', err); return throwError(() => err); })
    );
  }

  delete(id: string): Observable<boolean> {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`).pipe(
      catchError(err => { console.error('[RosterService] delete', err); return throwError(() => err); })
    );
  }

  /** @deprecated Use WsStompService.listen() directly or via a facade service. */
  listenToUpdates(topic: string): Observable<any> {
    return this.ws.listen(topic);
  }
}
