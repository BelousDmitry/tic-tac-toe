import { Component, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RxStompService } from '../../services/rx-stomp/rx-stomp.service';
import { Subscription } from 'rxjs';
import { OAuthService } from 'angular-oauth2-oidc';
import { Message } from '@stomp/stompjs';
import { Move } from '../../DTO/move';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { InviteService } from '../../services/invitation/invite.service';
import { CdTimerModule, CdTimerComponent } from 'angular-cd-timer';
import { Router } from '@angular/router';

@Component({
  selector: 'app-game',
  standalone: true,
  imports: [CommonModule, CdTimerModule],
  templateUrl: './game.component.html',
  styleUrl: './game.component.css'
})
export class GameComponent implements OnInit, OnDestroy {

  private gameSub!: Subscription;
  private failedConnectSub!: Subscription;

  @ViewChild('disconnected')
  private disconnectedTemp: TemplateRef<any> | undefined;
  @ViewChild('winner')
  private winnerTemp: TemplateRef<any> | undefined;
  @ViewChild('myTimer', { static: true })
  private myTimerTemp: CdTimerComponent | undefined;



  field: string[][] = [['?', '?', '?'], ['?', '?', '?'], ['?', '?', '?']];
  game: any = "";
  username = "";
  disabled = true;
  output = "";


  constructor(private rxStompService: RxStompService, private oauthService: OAuthService, private modalService: NgbModal,
    private inviteService: InviteService, private router: Router) { }


  ngOnInit(): void { 
    this.username = this.oauthService.getIdentityClaims()['preferred_username'];

    if (this.inviteService.isInvite)
      this.inviteService.isInvite = false;
    else
      this.rxStompService.publish({ destination: '/app/ready/random', body: this.username });


    this.gameSub = this.rxStompService.watch(`/topic/game/${this.username}`).subscribe((message: Message) => {

      this.game = JSON.parse(message.body);
      this.parseField(this.game['current_field']);

      // client switch players 
      this.disabled = this.username == this.game['allowed_to_move'] ? false : true;

      if (!this.disabled){
        this.output = "your move..."
        this.myTimerTemp?.start();
      }
      else{
        this.output = "opponent's move..."
        // this.myTimerTemp?.reset();
      }


      if (this.failedConnectSub == undefined || this.failedConnectSub == Subscription.EMPTY) {
        let opponent = this.username == this.game['x_player'] ? this.game['o_player'] : this.game['x_player'];
        this.failedConnectSub = this.rxStompService.watch(`/topic/game/connection/failed/${opponent}`).subscribe((message: Message) => {
          this.myTimerTemp?.reset();
          this.openModal(this.disconnectedTemp);
        });
      }

      if (this.game['winner'] != null || this.game['tie'] == true) {
        this.disabled = true;
        this.failedConnectSub.unsubscribe();
        this.failedConnectSub = Subscription.EMPTY;
        this.myTimerTemp?.reset();
        this.openModal(this.winnerTemp);
      }

    });

  }


  overTime() {
    this.move(0, 0, true);
  }

  replaySame() {
    let opponent = this.username == this.game['x_player'] ? this.game['o_player'] : this.game['x_player'];
    this.inviteService.sendInvite(opponent);
    this.game = "";
    this.gameSub.unsubscribe();
    this.ngOnInit();
    this.modalService.dismissAll();
  }

  replayRandom() {
    this.gameSub.unsubscribe();
    this.game = "";
    this.ngOnInit();
    this.modalService.dismissAll();
  }


  move(row: number, column: number, overtime: boolean) {
    if (this.field[row][column] == '?' || overtime) {

      this.disabled = true;
      this.myTimerTemp?.reset();

      let payload = {
        'game_id': this.game['id'],
        'username': this.username,
        'row': row,
        'column': column,
        'overtime': overtime
      }

      const move = new Move(this.game['x_player'], this.game['o_player'], JSON.stringify(payload));

      this.rxStompService.publish({ destination: '/app/move', body: JSON.stringify(move) });
    }
  }



  parseField(field: string) {

    const rows: string[] = field.split("/");

    for (let i = 0; i < 3; i++) {
      for (let j = 0; j < 3; j++) {
        this.field[i][j] = rows[i].split(",")[j];
      }
    }
  }


  openModal(content: any) {
    this.modalService.open(content);
  }

  closeModal() {
    this.modalService.dismissAll();
    this.router.navigate(['/home']);
  }


  ngOnDestroy() {
    if (this.gameSub != undefined)
      this.gameSub.unsubscribe();
    if (this.failedConnectSub != undefined)
      this.failedConnectSub.unsubscribe();
    this.inviteService.isInvite = false;
  }



}
