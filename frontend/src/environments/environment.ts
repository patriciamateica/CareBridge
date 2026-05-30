const getApiBaseUrl = (): string => {
  if (typeof window === 'undefined') {
    return 'https://localhost:8443';
  }
  return 'https://localhost:8443'; // for local dev
};

export const environment = {
  production: false,
  apiBaseUrl: getApiBaseUrl(),
  wsUrl: '/ws',
  proxyBypass: true
};
