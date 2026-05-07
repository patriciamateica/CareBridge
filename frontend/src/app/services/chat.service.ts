import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RxStompService } from '../rx-stomp.service';
import { ChatMessage } from '../cruds/models/chat';
import { Observable, map } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private readonly http = inject(HttpClient);
  private readonly rxStompService = inject(RxStompService);
  private readonly apiUrl = 'http://localhost:8080/api/chat';

  getChatHistory(): Observable<ChatMessage[]> {
    return this.http.get<ChatMessage[]>(`${this.apiUrl}/history`);
  }

  sendMessage(message: ChatMessage): void {
    this.rxStompService.publish({
      destination: '/app/chat.sendMessage',
      body: JSON.stringify(message)
    });
  }

  watchMessages(): Observable<ChatMessage> {
    return this.rxStompService.watch('/topic/public').pipe(
      map(message => JSON.parse(message.body) as ChatMessage)
    );
  }
}
