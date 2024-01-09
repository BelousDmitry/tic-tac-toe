import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OAuthService } from 'angular-oauth2-oidc';
import { authCodeFlowConfig } from '../../configs/sso-config';
import { RouterLink } from '@angular/router';


@Component({
  selector: 'app-nav-bar',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './nav-bar.component.html',
  styleUrl: './nav-bar.component.css'
})
export class NavBarComponent implements OnInit {

  constructor(private oauthService: OAuthService){}


  ngOnInit(): void {
    this.configureSSO();
  }

  configureSSO() {
    this.oauthService.configure(authCodeFlowConfig);
    this.oauthService.loadDiscoveryDocumentAndTryLogin();
    this.oauthService.setupAutomaticSilentRefresh();
  }

  login() {
    this.oauthService.initCodeFlow();
  }


  logout() {
    this.oauthService.logOut();
  }

  get isRegistered(){
    return this.oauthService.hasValidAccessToken();
  }

  get name(){
    let claims: any = this.oauthService.getIdentityClaims();
    return claims ? claims.preferred_username : null;
  }

}
