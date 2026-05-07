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
  private readonly QUEUE_KEY = 'offline_sync_queue';
  private readonly LEGACY_QUEUE_KEY = 'pending_operations';
  private readonly DATA_PREFIX = 'offline_data_';

  private get isBrowser(): boolean {
    return typeof window !== 'undefined' && typeof localStorage !== 'undefined';
  }

  saveData<T>(key: string, data: T): void {
    if (!this.isBrowser) return;
    try {
      localStorage.setItem(this.DATA_PREFIX + key, JSON.stringify(data));
    } catch (error) {
      console.error(`Failed to save ${key} to localStorage`, error);
    }
  }

  getData<T>(key: string): T | null {
    if (!this.isBrowser) return null;

    const prefixed = this.safeParse<T>(localStorage.getItem(this.DATA_PREFIX + key));
    if (prefixed !== null) {
      return prefixed;
    }

    return this.safeParse<T>(localStorage.getItem(key));
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
    if (!this.isBrowser) return [];

    const current = this.safeParse<PendingOperation[]>(localStorage.getItem(this.QUEUE_KEY));
    if (Array.isArray(current)) {
      return this.sortQueue(current);
    }

    const legacy = this.safeParse<PendingOperation[]>(localStorage.getItem(this.LEGACY_QUEUE_KEY));
    if (Array.isArray(legacy)) {
      const normalized = this.sortQueue(legacy);
      this.saveQueue(normalized);
      localStorage.removeItem(this.LEGACY_QUEUE_KEY);
      return normalized;
    }

    return [];
  }

  clearQueue(): void {
    if (!this.isBrowser) return;
    localStorage.removeItem(this.QUEUE_KEY);
    localStorage.removeItem(this.LEGACY_QUEUE_KEY);
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
    let changed = false;
    const queue = this.getQueue().map((op) => {
      if (op.payload['patientId'] !== oldPatientId) {
        return op;
      }
      changed = true;
      return {
        ...op,
        payload: {
          ...op.payload,
          patientId: newPatientId,
        },
      };
    });
    if (changed) {
      this.saveQueue(queue);
    }
  }

  removeData(key: string): void {
    if (!this.isBrowser) return;
    localStorage.removeItem(this.DATA_PREFIX + key);
    localStorage.removeItem(key);
  }

  clearAll(): void {
    if (!this.isBrowser) return;
    localStorage.clear();
  }

  private generateOperationId(): string {
    return `op-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
  }

  private saveQueue(queue: PendingOperation[]): void {
    if (!this.isBrowser) return;
    localStorage.setItem(this.QUEUE_KEY, JSON.stringify(queue));
  }

  private sortQueue(queue: PendingOperation[]): PendingOperation[] {
    return [...queue].sort((a, b) => a.timestamp - b.timestamp);
  }

  private safeParse<T>(raw: string | null): T | null {
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as T;
    } catch {
      return null;
    }
  }
}
