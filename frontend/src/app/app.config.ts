import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { providePrimeNG } from 'primeng/config';
import { MessageService } from 'primeng/api';

import { routes } from './app.routes';
import { CarebridgeTheme } from './theme/theme';
import { RxStompService } from './rx-stomp.service';
import { rxStompServiceFactory } from './rx-stomp.config';
import { credentialsInterceptor } from '../auth-service/credentials.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([credentialsInterceptor])),
    provideAnimations(),
    MessageService,
    providePrimeNG({
      theme: {
        preset: CarebridgeTheme,
        options: { darkModeSelector: '.my-app-dark' }
      }
    }),
    {
      provide: RxStompService,
      useFactory: rxStompServiceFactory,
    }
  ]
};
