import { ApplicationConfig, inject } from '@angular/core';
import { HttpHeaders } from '@angular/common/http';
import {InMemoryCache} from '@apollo/client/core';
import { Apollo, APOLLO_OPTIONS } from 'apollo-angular';
import { HttpLink } from 'apollo-angular/http';
import { GraphQLWsLink } from '@apollo/client/link/subscriptions';
import { createClient } from 'graphql-ws';
import { getMainDefinition } from '@apollo/client/utilities';
import {ApolloClient, ApolloLink} from '@apollo/client';
import { setContext } from '@apollo/client/link/context';

function getGraphqlUrls(): { httpUrl: string; wsUrl: string } {
  if (typeof window === 'undefined') {
    return {
      httpUrl: 'http://localhost:8080/graphql',
      wsUrl: 'ws://localhost:8080/graphql',
    };
  }

  const host = window.location.hostname || 'localhost';
  return {
    httpUrl: `http://${host}:8080/graphql`,
    wsUrl: `ws://${host}:8080/graphql`,
  };
}

export function apolloOptionsFactory(): ApolloClient.Options {
  const httpLink = inject(HttpLink);
  const { httpUrl, wsUrl } = getGraphqlUrls();

  const http = httpLink.create({
    uri: httpUrl,
  });

  const ws = new GraphQLWsLink(
    createClient({
      url: wsUrl,
      connectionParams: () => {
        const token = localStorage.getItem('carebridge_auth_token');
        return token ? { Authorization: `Bearer ${token}` } : {};
      },
      retryAttempts: 20,
      shouldRetry: () => true,
    })
  );

  const authLink = setContext((_, context) => {
    const token = localStorage.getItem('carebridge_auth_token');
    const existingHeaders = context.headers instanceof HttpHeaders
      ? context.headers
      : new HttpHeaders((context.headers as Record<string, string> | undefined) ?? {});

    return {
      headers: token
        ? existingHeaders.set('Authorization', `Bearer ${token}`)
        : existingHeaders,
    };
  });

  const authedHttp = ApolloLink.from([authLink, http]);

  const link = ApolloLink.split(
    ({ query }) => {
      const definition = getMainDefinition(query);
      return (
        definition.kind === 'OperationDefinition' &&
        definition.operation === 'subscription'
      );
    },
    ws,
    authedHttp
  );

  return {
    link: link,
    cache: new InMemoryCache(),
  };
}

export const graphqlProvider: ApplicationConfig['providers'] = [
  Apollo,
  {
    provide: APOLLO_OPTIONS,
    useFactory: apolloOptionsFactory,
  },
];
