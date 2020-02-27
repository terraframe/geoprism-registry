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
import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';

import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { Application } from '../../../shared/model/application';
import { AuthService } from '../../../shared/service/auth.service';

import { HubService } from '../../service/hub.service';

declare var acp: any;

@Component( {
    selector: 'hub',
    templateUrl: './hub.component.html',
    styleUrls: ['./hub.component.css']
} )
export class HubComponent implements OnInit {
    context: string;
    applications: Application[] = [];
    isAdmin: boolean = false;
    buckets: string = 'col-sm-6';
    bsModalRef: BsModalRef;

    constructor(
        private service: HubService,
        private authService: AuthService,
        private modalService: BsModalService,
        private router: Router,
        private route: ActivatedRoute
    ) {
        this.context = acp as string;
    }

    ngOnInit(): void {
        this.service.applications().then( applications => {
            this.applications = applications;
        } );

        this.isAdmin = this.authService.isAdmin();
    }

    //   logout():void {
    //     this.sessionService.logout().then(response => {
    //       this.router.navigate(['/login']);	  
    //     }); 	  
    //   }


    open( application: Application ): void {
        window.location.href = this.context + '/' + application.url;
    }

    //   account():void{
    //     this.profileService.get().then(profile => {
    //       this.bsModalRef = this.modalService.show(ProfileComponent, {backdrop: 'static', class: 'gray modal-lg'});
    //       this.bsModalRef.content.profile = profile;
    //     });
    //   }
}
