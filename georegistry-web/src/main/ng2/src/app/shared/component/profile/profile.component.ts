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

import { Component, OnInit, Input} from '@angular/core';

import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { Profile } from '../../model/profile';
import { ProfileService } from '../../service/profile.service';
import { AuthService } from '../../service/auth.service';
import { Role } from '../../../admin/model/account';


@Component({  
  selector: 'profile',
  templateUrl: './profile.component.html',
  styles: ['.modal-form .check-block .chk-area { margin: 10px 0px 0 0;}']
})
export class ProfileComponent {
  public _profile:Profile = {
    oid: '',
    username: '',
    password: '',
    firstName: '',
    lastName: '',
    email: '',
    changePassword:false
  };
  
  @Input('profile')
  set profile(value: Profile) {
    this._profile = value;
    this.getRoles();
  }
  roles: Role[] = [];
  
  constructor(private service:ProfileService, public bsModalRef: BsModalRef, private authService: AuthService) {}
  

  getRoles(): void {
    this.service.getRolesForUser(this._profile.oid).then(roles => {
      this.roles = roles;
    });
  }
  
  onSubmit():void {
    if(!this._profile.changePassword) {
      delete this._profile.password;
    }
	  
    this.service.apply(this._profile).then(profile => {
      this.bsModalRef.hide();
    });
  }

  onChangePassword(): void {
    this._profile.changePassword = !this._profile.changePassword;
  }
  
  // getRoles():string {
  //   return this.authService.getRoleDisplayLabels();
  // }

  getRolesArray(): any {
    return this.authService.getRoles();
  }

  onRoleIdsUpdate(event: any): void {
    console.log(event)
  }
  
  cancel():void {
    this.service.unlock(this._profile.oid).then(profile => {
      this.bsModalRef.hide();
    });
  }  
}
