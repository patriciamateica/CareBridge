import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Observable, Subject } from 'rxjs';
import { Client, IMessage } from '@stomp/stompjs';
import { environment } from '../../../environments/environment';
// @ts-ignore
import * as SockJS_ from 'sockjs-client';
const SockJS = (SockJS_ as any).default || SockJS_;

@Injectable({ providedIn: 'root' })
export class WsStompService {
  private readonly client: Client | null = null;
  private readonly isBrowser: boolean;
  private readonly pendingSubscriptions: Array<() => void> = [];

  constructor(@Inject(PLATFORM_ID) platformId: Object) {
    this.isBrowser = isPlatformBrowser(platformId);
    if (this.isBrowser) {
      const wsUrl = environment.wsUrl;
      this.client = new Client({
        webSocketFactory: () => new SockJS(wsUrl),
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: () => {
          this.pendingSubscriptions.forEach(fn => fn());
          this.pendingSubscriptions.length = 0;
        },
      });
      this.client.activate();
    }
  }

  listen<T = any>(topic: string): Observable<T> {
    const subject = new Subject<T>();

    if (!this.isBrowser || !this.client) {
      return subject.asObservable();
    }

    const subscribe = () => {
      this.client!.subscribe(topic, (message: IMessage) => {
        if (message.body) {
          try {
            subject.next(JSON.parse(message.body) as T);
          } catch (e) {
            console.error(`[WsStompService] Failed to parse message on ${topic}:`, e);
          }
        }
      });
    };

    if (this.client.connected) {
      subscribe();
    } else {
      this.pendingSubscriptions.push(subscribe);
    }

    return subject.asObservable();
  }
}
