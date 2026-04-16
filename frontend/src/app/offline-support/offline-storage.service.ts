import { Injectable } from '@angular/core';

export interface PendingOperation {
  id: string;
  type: 'CREATE' | 'UPDATE' | 'DELETE';
  entity: string;
  data: any;
  timestamp: number;
}

@Injectable({
  providedIn: 'root'
})
export class OfflineStorageService {
  private readonly QUEUE_KEY = 'pending_operations';
  private readonly DATA_PREFIX = 'offline_data_';

  saveData(key: string, data: any): void {
    localStorage.setItem(this.DATA_PREFIX + key, JSON.stringify(data));
  }

  getData<T>(key: string): T | null {
    const data = localStorage.getItem(this.DATA_PREFIX + key);
    return data ? JSON.parse(data) : null;
  }

  queueOperation(op: Omit<PendingOperation, 'id' | 'timestamp'>): void {
    const queue = this.getQueue();
    const newOp: PendingOperation = {
      ...op,
      id: Math.random().toString(36).substring(2, 9),
      timestamp: Date.now()
    };
    queue.push(newOp);
    this.saveQueue(queue);
  }

  getQueue(): PendingOperation[] {
    const queue = localStorage.getItem(this.QUEUE_KEY);
    return queue ? JSON.parse(queue) : [];
  }

  clearQueue(): void {
    localStorage.removeItem(this.QUEUE_KEY);
  }

  removeFromQueue(id: string): void {
    const queue = this.getQueue().filter(op => op.id !== id);
    this.saveQueue(queue);
  }

  private saveQueue(queue: PendingOperation[]): void {
    localStorage.setItem(this.QUEUE_KEY, JSON.stringify(queue));
  }
}
