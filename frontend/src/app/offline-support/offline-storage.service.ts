import { Injectable } from '@angular/core';

export type OfflineOperationType =
  | 'PATIENT_CREATE'
  | 'PATIENT_UPDATE'
  | 'PATIENT_DELETE'
  | 'CHECKIN_UPSERT'
  | 'CARE_NOTE_ADD'
  | 'CARE_NOTE_UPDATE'
  | 'CARE_NOTE_DELETE'
  | 'MEDICATION_ADD'
  | 'MEDICATION_DELETE';

export interface PendingOperation {
  id: string;
  type: OfflineOperationType;
  payload: Record<string, unknown>;
  timestamp: number;
  retryCount: number;
  lastError?: string;
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

  queueOperation(op: Omit<PendingOperation, 'id' | 'timestamp' | 'retryCount' | 'lastError'>): string {
    const queue = this.getQueue();
    const newOp: PendingOperation = {
      ...op,
      id: this.generateOperationId(),
      timestamp: Date.now(),
      retryCount: 0,
    };
    queue.push(newOp);
    this.saveQueue(queue);
    return newOp.id;
  }

  getQueue(): PendingOperation[] {
    const queue = localStorage.getItem(this.QUEUE_KEY);
    const parsed = queue ? JSON.parse(queue) as PendingOperation[] : [];
    return parsed.sort((a, b) => a.timestamp - b.timestamp);
  }

  clearQueue(): void {
    localStorage.removeItem(this.QUEUE_KEY);
  }

  removeFromQueue(id: string): void {
    const queue = this.getQueue().filter(op => op.id !== id);
    this.saveQueue(queue);
  }

  markOperationFailed(id: string, error: unknown): void {
    const message = error instanceof Error ? error.message : String(error);
    const queue = this.getQueue().map((op) => {
      if (op.id !== id) return op;
      return {
        ...op,
        retryCount: op.retryCount + 1,
        lastError: message,
      };
    });
    this.saveQueue(queue);
  }

  remapPatientId(oldPatientId: string, newPatientId: string): void {
    const queue = this.getQueue().map((op) => {
      if (op.payload['patientId'] !== oldPatientId) return op;
      return {
        ...op,
        payload: {
          ...op.payload,
          patientId: newPatientId,
        },
      };
    });
    this.saveQueue(queue);
  }

  private generateOperationId(): string {
    return `op-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
  }

  private saveQueue(queue: PendingOperation[]): void {
    localStorage.setItem(this.QUEUE_KEY, JSON.stringify(queue));
  }
}
