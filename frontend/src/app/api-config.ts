export function getApiBaseUrl(): string {
  if (typeof window === 'undefined') {
    return 'https://localhost:8443';
  }
  // Use relative URL to leverage the dev server proxy with "secure: false"
  // This bypasses certificate validation for self-signed certs in development
  return '';
}

import { environment } from '../environments/environment';

export const buildApiUrl = (path: string): string => {
  return `${environment.apiBaseUrl}${path}`;
};

