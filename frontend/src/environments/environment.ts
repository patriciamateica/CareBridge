const getApiBaseUrl = (): string => {
  if (typeof window === 'undefined') {
    return 'https://localhost:8443';
  }
  // Use relative URL to leverage the dev server proxy with "secure: false"
  // This bypasses certificate validation for self-signed certs in development
  return '';
};

export const environment = {
  production: false,
  apiBaseUrl: getApiBaseUrl(),
  wsUrl: '/ws',
  // Proxy configuration for development
  proxyBypass: true  // Indicates that we rely on proxy.conf.json
};


