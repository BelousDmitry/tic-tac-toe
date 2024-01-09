import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree } from '@angular/router';
import { OAuthService } from 'angular-oauth2-oidc';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard{

  constructor(private oauthService: OAuthService){}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
      
      let hasIdToken = this.oauthService.hasValidIdToken();
      let hasAccessToken = this.oauthService.hasValidAccessToken();

      if(hasIdToken && hasAccessToken){
        return true;
      }else{
        this.oauthService.initCodeFlow();
        return false;
      } 
  }
  
}