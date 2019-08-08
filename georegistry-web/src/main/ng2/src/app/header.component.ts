import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';

import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { ProfileService } from './profile/profile.service';
import { ProfileComponent } from './profile/profile.component';
import { SessionService } from './core/auth/session.service';
import { AuthService } from './core/auth/auth.service';

declare var acp:string;

@Component( {

    selector: 'cgr-header',
    templateUrl: './header.component.html',
    styleUrls: []
} )
export class CgrHeaderComponent {
    context:string;
  isAdmin: boolean;
  isMaintainer: boolean;
  isContributor: boolean;
  bsModalRef: BsModalRef;

  constructor(
    private sessionService:SessionService,
    private modalService: BsModalService,
    private profileService:ProfileService,
    private router:Router,
    private service: AuthService
  ) {
    this.context = acp;
    this.isAdmin = service.isAdmin();
    this.isMaintainer = this.isAdmin || service.isMaintainer();
    this.isContributor = this.isAdmin || this.isMaintainer || service.isContributer();
  }

  logout():void {
    this.sessionService.logout().then(response => {
      this.router.navigate(['/login']);   
    });     
  }
  
  getUsername()
  {
    return this.service.getUsername();
  }

  account():void{
    this.profileService.get().then(profile => {
      this.bsModalRef = this.modalService.show(ProfileComponent, {backdrop: 'static', class: 'gray modal-lg'});
      this.bsModalRef.content.profile = profile;
    });
  }

}
