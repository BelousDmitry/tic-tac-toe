import { Injectable } from '@angular/core';
import { OAuthService } from 'angular-oauth2-oidc';
import { RxStompService } from '../rx-stomp/rx-stomp.service';
import { Router } from '@angular/router';
import { Invite } from '../../DTO/invite';

@Injectable({
  providedIn: 'root'
})
export class InviteService {


  isInvite = false;

  constructor(private oauthService: OAuthService, private rxStompService: RxStompService, private router: Router) { }


  sendInvite(receiver:string) {
    this.isInvite = true;

    let payload = new Invite(this.oauthService.getIdentityClaims()['preferred_username'], receiver);

    this.rxStompService.publish({ destination: '/app/invite/send', body: JSON.stringify(payload)});
    this.router.navigate(['/game']);
  }

}
