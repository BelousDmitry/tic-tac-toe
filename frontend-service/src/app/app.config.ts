import { ApplicationConfig, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { RxStompService } from './services/rx-stomp/rx-stomp.service';
import { rxStompServiceFactory } from './services/rx-stomp/rx-stomp.factory';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { loggingInterceptor } from './interceptor/loggingInterceptor';
import { OAuthModule } from 'angular-oauth2-oidc';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),

    importProvidersFrom(
      OAuthModule.forRoot()
    ),
     
    provideHttpClient(
      // withInterceptors([loggingInterceptor]),
    ),

    {
      provide: RxStompService,
      useFactory: rxStompServiceFactory
    },

  ]



};
