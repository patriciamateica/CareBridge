export function getApiBaseUrl(): string {
  if (typeof window === 'undefined') {
    return 'https://localhost:8443';
  }
  // Use relative URL to leverage the dev server proxy with "secure: false"
  // This bypasses certificate validation for self-signed certs in development
  return '';
}

export function buildApiUrl(endpoint: string): string {
  const baseUrl = getApiBaseUrl();
  const path = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;
  // Return relative URL to use Angular dev server proxy (proxy.conf.json)
  // The proxy will forward to https://localhost:8443 with secure: false
  return `${baseUrl}${path}`;
}

