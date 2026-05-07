import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject, throwError } from 'rxjs';
import { Client } from '@stomp/stompjs';
import { isPlatformBrowser } from '@angular/common';
// @ts-ignore
import * as SockJS_ from 'sockjs-client';
const SockJS = (SockJS_ as any).default || SockJS_;
import { Roster } from '../models/roster';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class RosterService {
  private apiUrl = 'http://localhost:8080/api/rosters';
  private stompClient: Client | null = null;
  private isBrowser: boolean;

  constructor(private http: HttpClient, @Inject(PLATFORM_ID) platformId: Object) {
    this.isBrowser = isPlatformBrowser(platformId);
    if (this.isBrowser) {
        this.stompClient = new Client({
          webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
          debug: (str) => {
            console.log(str);
          },
          reconnectDelay: 5000,
          heartbeatIncoming: 4000,
          heartbeatOutgoing: 4000,
        });
        this.stompClient.activate();
    }
  }

  getAll(): Observable<any> {
    return this.http.get<any>(this.apiUrl).pipe(
      catchError(err => {
        console.error('Error fetching all rosters:', err);
        return throwError(() => err);
      })
    );
  }

  getById(id: string): Observable<Roster> {
    return this.http.get<Roster>(`${this.apiUrl}/${id}`).pipe(
      catchError(err => {
        console.error('Error fetching roster:', err);
        return throwError(() => err);
      })
    );
  }

  getByNurseId(nurseId: string, page = 0, size = 5): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/nurse/${nurseId}?page=${page}&size=${size}`).pipe(
      catchError(err => {
        console.error('Error fetching rosters for nurse:', err);
        return throwError(() => err);
      })
    );
  }

  create(data: Roster): Observable<Roster> {
    return this.http.post<Roster>(this.apiUrl, data).pipe(
      catchError(err => {
        console.error('Error creating roster:', err);
        return throwError(() => err);
      })
    );
  }

  update(id: string, data: Roster): Observable<Roster> {
    return this.http.put<Roster>(`${this.apiUrl}/${id}`, data).pipe(
      catchError(err => {
        console.error('Error updating roster:', err);
        return throwError(() => err);
      })
    );
  }

  delete(id: string): Observable<boolean> {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`).pipe(
      catchError(err => {
        console.error('Error deleting roster:', err);
        return throwError(() => err);
      })
    );
  }

  listenToUpdates(topic: string): Observable<any> {
    const subject = new Subject<any>();
    if (!this.isBrowser || !this.stompClient) return subject.asObservable();

    const subscribe = () => {
      this.stompClient!.subscribe(topic, (message) => {
        if (message.body) {
          try {
            subject.next(JSON.parse(message.body));
          } catch (e) {
            console.error('Error parsing WebSocket message:', e);
          }
        }
      });
    };

    if (this.stompClient.connected) {
      subscribe();
    } else {
      this.stompClient.onConnect = () => {
        subscribe();
      };
    }
    return subject.asObservable();
  }
}
