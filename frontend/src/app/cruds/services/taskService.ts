import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Task } from '../models/task';
import { WsStompService } from './ws-stomp.service';

@Injectable({ providedIn: 'root' })
export class TaskService {
  private readonly apiUrl = '/api/tasks';

  constructor(private readonly http: HttpClient, private readonly ws: WsStompService) {}

  getAll(page = 0, size = 10): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}?page=${page}&size=${size}`).pipe(
      catchError(err => { console.error('[TaskService] getAll', err); return throwError(() => err); })
    );
  }

  getByNurseId(nurseId: string, page = 0, size = 10): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/nurse/${nurseId}?page=${page}&size=${size}`).pipe(
      catchError(err => { console.error('[TaskService] getByNurseId', err); return throwError(() => err); })
    );
  }

  getById(id: string): Observable<Task> {
    return this.http.get<Task>(`${this.apiUrl}/${id}`).pipe(
      catchError(err => { console.error('[TaskService] getById', err); return throwError(() => err); })
    );
  }

  create(data: Task): Observable<Task> {
    return this.http.post<Task>(this.apiUrl, data).pipe(
      catchError(err => { console.error('[TaskService] create', err); return throwError(() => err); })
    );
  }

  update(id: string, data: Task): Observable<Task> {
    return this.http.put<Task>(`${this.apiUrl}/${id}`, data).pipe(
      catchError(err => { console.error('[TaskService] update', err); return throwError(() => err); })
    );
  }

  delete(id: string): Observable<boolean> {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`).pipe(
      catchError(err => { console.error('[TaskService] delete', err); return throwError(() => err); })
    );
  }

  /** @deprecated Use WsStompService.listen() directly or via a facade service. */
  listenToUpdates(topic: string): Observable<any> {
    return this.ws.listen(topic);
  }
}
