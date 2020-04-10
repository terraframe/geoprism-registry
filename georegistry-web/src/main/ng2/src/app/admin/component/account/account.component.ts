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

import { RoleManagementComponent } from './role-management.component'

import { Account, User, Role } from '../../model/account';
import { AccountService } from '../../service/account.service';
import { LocalizationService } from '../../../shared/service/localization.service';
import { Localization } from '../../model/settings';

@Component( {
    selector: 'account',
    templateUrl: './account.component.html',
    styles: ['.modal-form .check-block .chk-area { margin: 10px 0px 0 0;}'],
    styleUrls: ['./account.css']
} )
export class AccountComponent implements OnInit {

    message: string = null;
    account: Account;
    roles: Role[];
    roleIds: string[] = [];

    @Input()
    set oid( oid: string ) {
        if ( oid === 'NEW' ) {
            this.service.newInstance("").then( data => {
                this.account = data;
            } ).catch(( err: HttpErrorResponse ) => {
                this.error( err );
            } );
        }
        else if ( oid ) {
            this.service.edit( oid ).then( data => {
                this.account = data;

            } ).catch(( err: HttpErrorResponse ) => {
                this.error( err );
            } );
        }
    }

    public onEdit: Subject<User>;

    constructor(
        private service: AccountService,
        private location: Location,
        public bsModalRef: BsModalRef,
        private localizeService: LocalizationService
    ) {
    }

    ngOnInit(): void {
        this.onEdit = new Subject();
    }

    onRoleIdsUpdate(event): void {
        this.roleIds = event;
    }


    cancel(): void {
        if ( this.account.user.newInstance === true ) {
            this.bsModalRef.hide();
        }
        else {
            this.service.unlock( this.account.user.oid ).then( response => {
                this.bsModalRef.hide();
            } );
        }
    }

    onChangePassword(): void {
        this.account.changePassword = !this.account.changePassword;
    }

    onSubmit(): void {

        if ( !this.account.changePassword && !this.account.user.newInstance ) {
            delete this.account.user.password;
        }

        if (this.roleIds.length > 0) {
            this.service.apply(this.account.user, this.roleIds).then(data => {
                this.onEdit.next(data);
                this.bsModalRef.hide();
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        }
        else{
            this.message = this.localizeService.decode("account.role.management.roles.required.message");
        }
    }


    public error( err: HttpErrorResponse ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.error.localizedMessage || err.error.message || err.message );
        }

    }

}