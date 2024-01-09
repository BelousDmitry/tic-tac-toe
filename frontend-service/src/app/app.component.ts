import { Component, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterOutlet } from '@angular/router';
import { NavBarComponent } from './components/nav-bar/nav-bar.component';
import { NgbModal, NgbModalConfig } from '@ng-bootstrap/ng-bootstrap'
import { Subscription } from 'rxjs';
import { RxStompService } from './services/rx-stomp/rx-stomp.service';
import { OAuthService } from 'angular-oauth2-oidc';
import { Message } from '@stomp/stompjs';
import { InviteService } from './services/invitation/invite.service';
import { Invite } from './DTO/invite';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, NavBarComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, OnDestroy {

  private inviteSub!: Subscription;
  private notifySub!: Subscription;

  @ViewChild('invitation')
  private invitationTemp: TemplateRef<any> | undefined;
  @ViewChild('notify')
  private notificationTemp: TemplateRef<any> | undefined;

  invite: Invite | undefined;
  notification: string | undefined;


  constructor(private modalService: NgbModal, private rxStompService: RxStompService, private oauthService: OAuthService,
    private router: Router, private inviteService: InviteService, config: NgbModalConfig) {
      config.backdrop = 'static';
      config.keyboard = false;
     }

  ngOnInit(): void {

    if (this.oauthService.hasValidAccessToken()) {
      this.initializeSubs();
    }
    else {
      let subscription: Subscription = this.oauthService.events.subscribe(event => {
        if (event.type == "token_received") {
          subscription.unsubscribe();
          this.initializeSubs();
        }
      })
    }

  }


  initializeSubs() {
    let username = this.oauthService.getIdentityClaims()['preferred_username'];

    this.inviteSub = this.rxStompService.watch(`/topic/invite/${username}`).subscribe((message: Message) => {
      this.invite = JSON.parse(message.body);
      this.openModal(this.invitationTemp);
    });

    this.notifySub = this.rxStompService.watch(`/topic/notify/${username}`).subscribe((message: Message) => {
      this.notification = message.body;
      this.openModal(this.notificationTemp);
    });


  }


  acceptInvite() {
    this.rxStompService.publish({ destination: '/app/invite/accept', body: JSON.stringify(this.invite) });
    this.inviteService.isInvite = true;
    this.router.navigate(['/game']);
    this.modalService.dismissAll();
  }



  denyInvite() {
    this.rxStompService.publish({ destination: '/app/invite/deny', body: JSON.stringify(this.invite) });
    this.modalService.dismissAll();
  }




  openModal(content: any) {
    this.modalService.open(content);
  }


  closeModal() {
    this.modalService.dismissAll();
    this.router.navigate(['/home']);
  }



  ngOnDestroy(): void {
    if (this.inviteSub != undefined)
      this.inviteSub.unsubscribe();
    if (this.notifySub != undefined)
      this.notifySub.unsubscribe();
  }

}
