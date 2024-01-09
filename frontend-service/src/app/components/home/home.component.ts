import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { InviteService } from '../../services/invitation/invite.service';
import { OAuthService } from 'angular-oauth2-oidc';
import { HttpService } from '../../services/http/http.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {

  receiver = "";

  friends = ["dmytro1999", "john1999", "david1999"];



  constructor(private inviteService: InviteService, private oauthService: OAuthService, private http: HttpService) { }

  ngOnInit(): void {


  }

  sendRequest() {
    this.inviteService.sendInvite(this.receiver);
  }


  get isRegistered() {
    return this.oauthService.hasValidAccessToken();
  }

  login() {
    this.oauthService.initCodeFlow();
  }






}
