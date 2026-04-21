import { inject, Injectable } from '@angular/core';
import { NetworkService } from './network.service';
import { ToastService } from '../toast-service/toast-service';

@Injectable({ providedIn: 'root' })
export class ConnectivityNotifierService {
  private readonly network = inject(NetworkService);
  private readonly toast = inject(ToastService);
  private lastOnlineState: boolean | null = null;

  constructor() {
    this.network.isOnline$.subscribe((isOnline) => {
      if (this.lastOnlineState === isOnline) {
        return;
      }

      this.lastOnlineState = isOnline;

      if (!isOnline) {
        if (this.network.offlineReason === 'NETWORK_DOWN') {
          this.toast.showError('Internet connection lost. You are working offline.');
          return;
        }

        this.toast.showError('Server is unreachable. You are working offline.');
        return;
      }

      this.toast.showSuccess('Connection restored. Data sync is active again.');
    });
  }
}

