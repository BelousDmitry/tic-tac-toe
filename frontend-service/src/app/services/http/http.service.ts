import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { OAuthService } from 'angular-oauth2-oidc';

@Injectable({
  providedIn: 'root'
})
export class HttpService {

  constructor(private http: HttpClient, private oauthService: OAuthService) { }



  getTemporalToken(){
    const token = this.oauthService.getAccessToken();
    const headers = { 'Authorization': `Bearer ${token}`};
    return this.http.get('http://localhost:8080/ws/auth/temporal-token', { headers : headers, responseType: 'text' });
  }


}
