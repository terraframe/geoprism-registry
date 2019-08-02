///
/// Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Runway SDK(tm).
///
/// Runway SDK(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Runway SDK(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';

import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { ProfileService } from '../profile/profile.service';
import { ProfileComponent } from '../profile/profile.component';
import { SessionService } from '../core/auth/session.service';

declare var acp:string;

@Component({
  selector: 'hub-header',
  templateUrl: './hub-header.component.html',
  styleUrls: []
})
export class HubHeaderComponent {
  private context:string;
  @Input() isAdmin: boolean = false;
  bsModalRef: BsModalRef;  

  constructor(
    private sessionService:SessionService,
    private modalService: BsModalService,
    private profileService:ProfileService,
	private router:Router) {
    this.context = acp;
  }

  logout():void {
    this.sessionService.logout().then(response => {
      this.router.navigate(['/login']);	  
    }); 	  
  }

  account():void{
    this.profileService.get().then(profile => {
      this.bsModalRef = this.modalService.show(ProfileComponent, {backdrop: 'static', class: 'gray modal-lg'});
      this.bsModalRef.content.profile = profile;
    });
  }

}
