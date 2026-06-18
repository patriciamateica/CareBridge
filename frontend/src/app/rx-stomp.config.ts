import { RxStompConfig } from '@stomp/rx-stomp';
import { environment } from '../environments/environment';
// @ts-ignore
import * as SockJS_ from 'sockjs-client';
const SockJS = (SockJS_ as any).default || SockJS_;

export const myRxStompConfig: RxStompConfig = {
  webSocketFactory: () => new SockJS(environment.wsUrl),

  connectHeaders: {},

  heartbeatIncoming: 0,
  heartbeatOutgoing: 20000,

  reconnectDelay: 5000,

  debug: environment.production ? undefined : (msg: string): void => {
    console.log(new Date(), msg);
  },
};

import { RxStompService } from './rx-stomp.service';

export function rxStompServiceFactory() {
  const rxStomp = new RxStompService();
  rxStomp.configure(myRxStompConfig);
  rxStomp.activate();
  return rxStomp;
}
