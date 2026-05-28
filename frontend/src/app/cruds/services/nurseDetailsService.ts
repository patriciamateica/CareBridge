import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { Client } from '@stomp/stompjs';
import { isPlatformBrowser } from '@angular/common';
import { environment } from '../../../environments/environment';
// @ts-ignore
import * as SockJS_ from 'sockjs-client';
const SockJS = (SockJS_ as any).default || SockJS_;
import { NurseDetails } from '../models/nurseDetails';

@Injectable({
  providedIn: 'root'
})
export class NurseDetailsService {
  private apiUrl = 'api/nurse-details';
  private stompClient: Client | null = null;
  private isBrowser: boolean;

  constructor(private http: HttpClient, @Inject(PLATFORM_ID) platformId: Object) {
    this.isBrowser = isPlatformBrowser(platformId);
    if (this.isBrowser) {
      const wsUrl = '/ws';
      this.stompClient = new Client({
        webSocketFactory: () => new SockJS(wsUrl),
          reconnectDelay: 5000,
          heartbeatIncoming: 4000,
          heartbeatOutgoing: 4000,
        });
        this.stompClient.activate();
    }
  }

  getAll(): Observable<any> {
    return this.http.get<any>(this.apiUrl);
  }

  getById(id: string): Observable<NurseDetails> {
    return this.http.get<NurseDetails>(`${this.apiUrl}/${id}`);
  }

  create(data: NurseDetails): Observable<NurseDetails> {
    return this.http.post<NurseDetails>(this.apiUrl, data);
  }

  update(id: string, data: NurseDetails): Observable<NurseDetails> {
    return this.http.put<NurseDetails>(`${this.apiUrl}/${id}`, data);
  }

  delete(id: string): Observable<boolean> {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }

  listenToUpdates(topic: string): Observable<any> {
    const subject = new Subject<any>();
    if (!this.isBrowser || !this.stompClient) return subject.asObservable();

    const subscribe = () => {
      this.stompClient!.subscribe(topic, (message) => {
        if (message.body) {
          subject.next(JSON.parse(message.body));
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
