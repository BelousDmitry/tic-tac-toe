import { OAuthService } from 'angular-oauth2-oidc';
import { RxStompState } from '@stomp/rx-stomp';
import { RxStompService } from './rx-stomp.service';
import { HttpService } from '../http/http.service';
import { inject } from '@angular/core';
import { Subscription } from 'rxjs';

export function rxStompServiceFactory() {

    const rxStomp = new RxStompService();
    const http = inject(HttpService);
    const oauth = inject(OAuthService);


    if (oauth.hasValidAccessToken()) {
        return connect(http, rxStomp, oauth);
    }
    else {
        let subscription: Subscription = oauth.events
            .subscribe(event => {
                if (event.type == "token_received") {
                    subscription.unsubscribe();
                    return connect(http, rxStomp, oauth);
                }
                return rxStomp;
            })
    }
    return rxStomp;
}


function connect(http: HttpService, rxStomp: RxStompService, oauth: OAuthService): RxStompService {

    http.getTemporalToken().subscribe(token => {
        rxStomp.configure({
            brokerURL: 'ws://localhost:8080/ws/game?temporal_token=' + token + '&preferred_username=' + oauth.getIdentityClaims()['preferred_username'],
            connectHeaders: {
                preferred_username: oauth.getIdentityClaims()['preferred_username'],
            },
            heartbeatIncoming: 0,
            heartbeatOutgoing: 20000,
            reconnectDelay: 500,
        });
        rxStomp.activate();
    });

    rxStomp.connectionState$.subscribe(state => {
        console.log(RxStompState[state]);
    });
    return rxStomp;

}