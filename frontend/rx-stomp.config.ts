import { RxStompConfig } from '@stomp/rx-stomp';
import SockJS = require('sockjs-client');


export const myRxStompConfig: RxStompConfig = {
  webSocketFactory: () => new SockJS('http://localhost:8080/ws'),

  connectHeaders: {
    login: 'guest',
    passcode: 'guest',
  },

  heartbeatIncoming: 0,
  heartbeatOutgoing: 20000,
  reconnectDelay: 5000,

  debug: (msg: string): void => {
    console.log(new Date(), msg);
  },
};
