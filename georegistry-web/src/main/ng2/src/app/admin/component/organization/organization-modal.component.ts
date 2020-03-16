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

import { Component, Input, OnInit } from '@angular/core';
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';

import { Location } from '@angular/common';
import 'rxjs/add/operator/switchMap';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';

import { Account, User } from '../../model/account';
import { Organization } from '../../model/settings';
import { AccountService } from '../../service/account.service';

@Component( {
    selector: 'organization-modal',
    templateUrl: './organization-modal.component.html',
    styles: ['.modal-form .check-block .chk-area { margin: 10px 0px 0 0;}']
} )
export class OrganizationModalComponent implements OnInit {

    message: string = null;
    organization: Organization = {name: "", code: "", contact: ""};

    public onSuccess: Subject<Organization>;

    constructor(
        private service: AccountService,
        public bsModalRef: BsModalRef
    ) {
    }

    ngOnInit(): void {
        this.onSuccess = new Subject();
    }

    cancel(): void {
        this.bsModalRef.hide();
    }

    onSubmit(): void {
        this.onSuccess.next( this.organization );
        this.bsModalRef.hide();

        // this.service.apply( this.account.user, roleIds ).then( data => {
        //     this.onEdit.next( data );
        //     this.bsModalRef.hide();
        // } ).catch(( err: HttpErrorResponse ) => {
        //     this.error( err );
        // } );
    }

    public error( err: HttpErrorResponse ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.error.localizedMessage || err.error.message || err.message );
        }

    }

}