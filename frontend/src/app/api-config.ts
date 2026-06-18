import { environment } from '../environments/environment';

export const buildApiUrl = (path: string): string => `${environment.apiBaseUrl}${path}`;

