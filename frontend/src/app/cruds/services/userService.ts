import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { User } from '../models/user';
import { WsStompService } from './ws-stomp.service';
import { buildApiUrl } from '../../api-config';

@Injectable({ providedIn: 'root' })
export class UserService {

  constructor(private readonly http: HttpClient, private readonly ws: WsStompService) {}

  getAll(page = 0, size = 200): Observable<any> {
    return this.http.get<any>(buildApiUrl(`/api/users?page=${page}&size=${size}`)).pipe(
      catchError(err => { console.error('[UserService] getAll', err); return throwError(() => err); })
    );
  }

  getByRole(role: string, page = 0, size = 200): Observable<any> {
    return this.http.get<any>(buildApiUrl(`/api/users/by-role?role=${role}&page=${page}&size=${size}`)).pipe(
      catchError(err => { console.error('[UserService] getByRole', err); return throwError(() => err); })
    );
  }

  getById(id: string): Observable<User> {
    return this.http.get<User>(buildApiUrl(`/api/users/${id}`)).pipe(
      catchError(err => { console.error('[UserService] getById', err); return throwError(() => err); })
    );
  }

  create(data: any): Observable<User> {
    return this.http.post<User>(buildApiUrl('/api/users/register'), data).pipe(
      catchError(err => { console.error('[UserService] create', err); return throwError(() => err); })
    );
  }

  registerPatient(data: any): Observable<User> {
    return this.http.post<User>(buildApiUrl('/api/users/register-patient'), data).pipe(
      catchError(err => { console.error('[UserService] registerPatient', err); return throwError(() => err); })
    );
  }

  update(id: string, data: Partial<User>): Observable<User> {
    return this.http.put<User>(buildApiUrl(`/api/users/${id}`), data).pipe(
      catchError(err => { console.error('[UserService] update', err); return throwError(() => err); })
    );
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(buildApiUrl(`/api/users/${id}`)).pipe(
      catchError(err => { console.error('[UserService] delete', err); return throwError(() => err); })
    );
  }

  /** @deprecated Use WsStompService.listen() directly or via a facade service. */
  listenToUpdates(topic: string): Observable<any> {
    return this.ws.listen(topic);
  }
}
