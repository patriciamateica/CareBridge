import { Component, OnInit, inject, signal, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService } from '../services/chat.service';
import { AuthService } from '../../auth-service/auth.service';
import { ChatMessage } from '../cruds/models/chat';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, ButtonModule, InputTextModule],
  templateUrl: './chat.html',
  styleUrl:'./chat.css',
})
export class ChatComponent implements OnInit, AfterViewChecked {
  private chatService = inject(ChatService);
  private authService = inject(AuthService);

  @ViewChild('scrollMe') private myScrollContainer!: ElementRef;

  messages = signal<ChatMessage[]>([]);
  newMessage = '';
  currentUserId = this.authService.currentUserId();

  ngOnInit() {
    this.chatService.getChatHistory().subscribe(history => {
      this.messages.set(history);
      this.scrollToBottom();
    });

    this.chatService.watchMessages().subscribe(msg => {
      this.messages.update(msgs => [...msgs, msg]);
      this.scrollToBottom();
    });
  }

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  send() {
    if (!this.newMessage.trim()) return;

    const msg: ChatMessage = {
      senderId: this.authService.currentUserId(),
      senderName: this.authService.currentUserName(),
      content: this.newMessage,
      type: 'CHAT'
    };

    this.chatService.sendMessage(msg);
    this.newMessage = '';
  }

  private scrollToBottom(): void {
    try {
      this.myScrollContainer.nativeElement.scrollTop = this.myScrollContainer.nativeElement.scrollHeight;
    } catch(err) { }
  }
}
