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

import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { ConfirmModalComponent } from '../../../shared/component/modals/confirm-modal.component';
import { ErrorModalComponent } from '../../../shared/component/modals/error-modal.component';
import { LocalizationService } from '../../../shared/service/localization.service';

import { User, PageResult, Account } from '../../model/account';
import { AccountService } from '../../service/account.service';
import { AccountComponent } from './account.component';
import { AccountInviteComponent } from './account-invite.component';

declare let acp: string;

@Component( {
    selector: 'accounts',
    templateUrl: './accounts.component.html',
    styles: ['./accounts.css']
} )
export class AccountsComponent implements OnInit {
    res: PageResult = {
        resultSet: [],
        count: 0,
        pageNumber: 1,
        pageSize: 10
    };
    bsModalRef: BsModalRef;
    message: string = null;

    constructor(
        private router: Router,
        private service: AccountService,
        private modalService: BsModalService,
        private localizeService: LocalizationService
    ) { }

    ngOnInit(): void {
        this.service.page( 1 ).then( res => {
            this.res = res;
        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );
    }


    edit( user: User ): void {
        // this.router.navigate(['/admin/account', user.oid]);

        this.bsModalRef = this.modalService.show( AccountComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        this.bsModalRef.content.oid = user.oid;

        let that = this;
        ( <AccountComponent>this.bsModalRef.content ).onEdit.subscribe( data => {
            let updatedUserIndex = that.res.resultSet.map(
                function( e ) { return e.oid; }
            ).indexOf( data.oid );

            if ( updatedUserIndex !== -1 ) {
                that.res.resultSet[updatedUserIndex] = data;
            }
        } );
    }

    newInstance(): void {
        // this.router.navigate(['/admin/account', 'NEW']);

        this.bsModalRef = this.modalService.show( AccountComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        this.bsModalRef.content.oid = 'NEW';

        let that = this;
        this.bsModalRef.content.onEdit.subscribe( data => {
            this.onPageChange( this.res.pageNumber );
        } );

    }

    onPageChange( pageNumber: number ): void {
        this.service.page( pageNumber ).then( res => {
            this.res = res;
        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );
    }

    inviteUsers(): void {
        // this.router.navigate(['/admin/invite']);	  

        this.bsModalRef = this.modalService.show( AccountInviteComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
    }

    public error( err: HttpErrorResponse ): void {
        // Handle error
        if ( err !== null ) {
            // TODO: add error modal
            this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            this.bsModalRef.content.message = ( err.error.localizedMessage || err.error.message || err.message );
        }

    }
}
