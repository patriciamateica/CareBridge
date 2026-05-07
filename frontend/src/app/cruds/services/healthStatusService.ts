import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject, throwError } from 'rxjs';
import { Client } from '@stomp/stompjs';
import { isPlatformBrowser } from '@angular/common';
// @ts-ignore
import * as SockJS_ from 'sockjs-client';
const SockJS = (SockJS_ as any).default || SockJS_;
import { HealthStatus } from '../models/healthStatus';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class HealthStatusService {
  private apiUrl = 'http://localhost:8080/api/health-status';
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
        console.error('Error fetching all health statuses:', err);
        return throwError(() => err);
      })
    );
  }

  getById(id: string): Observable<HealthStatus> {
    return this.http.get<HealthStatus>(`${this.apiUrl}/${id}`).pipe(
      catchError(err => {
        console.error('Error fetching health status:', err);
        return throwError(() => err);
      })
    );
  }

  getByPatientId(patientId: string, page = 0, size = 5): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/patient/${patientId}?page=${page}&size=${size}`).pipe(
      catchError(err => {
        console.error('Error fetching health statuses for patient:', err);
        return throwError(() => err);
      })
    );
  }

  create(data: HealthStatus): Observable<HealthStatus> {
    return this.http.post<HealthStatus>(this.apiUrl, data).pipe(
      catchError(err => {
        console.error('Error creating health status:', err);
        return throwError(() => err);
      })
    );
  }

  update(id: string, data: HealthStatus): Observable<HealthStatus> {
    return this.http.put<HealthStatus>(`${this.apiUrl}/${id}`, data).pipe(
      catchError(err => {
        console.error('Error updating health status:', err);
        return throwError(() => err);
      })
    );
  }

  delete(id: string): Observable<boolean> {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`).pipe(
      catchError(err => {
        console.error('Error deleting health status:', err);
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
