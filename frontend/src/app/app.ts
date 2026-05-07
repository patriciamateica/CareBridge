import {Component, inject, signal} from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {Toast} from 'primeng/toast';
import {CookiesService} from '../cookies/cookieservice';
import { ConnectivityNotifierService } from './offline-support/connectivity-notifier.service';
import { OfflineSyncService } from './offline-support/offline-sync.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, Toast],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('frontend');
  public cookieService = inject(CookiesService);
  private connectivityNotifier = inject(ConnectivityNotifierService);
  private offlineSync = inject(OfflineSyncService);

  constructor() {
    setTimeout(() => {
      this.offlineSync.syncNow().catch(err => console.error('Initial sync failed', err));
    }, 500);
  }
}
