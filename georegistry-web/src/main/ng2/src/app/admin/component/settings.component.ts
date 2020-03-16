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

import { ConfirmModalComponent } from '../../shared/component/modals/confirm-modal.component';
import { ErrorModalComponent } from '../../shared/component/modals/error-modal.component';
import { LocalizationService } from '../../shared/service/localization.service';

import { User, PageResult, Account } from '../model/account';
import { AccountService } from '../service/account.service';
import { AccountComponent } from './account/account.component';
import { AccountInviteComponent } from './account/account-invite.component';
import { EmailComponent } from './email/email.component'
import { OrganizationModalComponent } from './organization/organization-modal.component'
import { NewLocaleModalComponent } from './localization-manager/new-locale-modal.component';

import { SettingsService } from '../service/settings.service'
import { Settings, Organization } from '../model/settings';
import { LocaleInfo, AllLocaleInfo, Locale } from '../model/localization-manager'

import { SystemLogo } from '../model/system-logo';
import { SystemLogoService } from '../service/system-logo.service';
import { AuthService } from '../../shared/service/auth.service';


declare let acp: string;

@Component( {
    selector: 'settings',
    templateUrl: './settings.component.html',
    styleUrls: ['./settings.css']
} )
export class SettingsComponent implements OnInit {
    bsModalRef: BsModalRef;
    message: string = null;
    organizations: any = [];
    installedLocales: Locale[]; // TODO: this should be from the localizaiton-manager model
    isAdmin: boolean;
    isMaintainer: boolean;
    isContributor: boolean;
    settings: Settings = {email: {isConfigured: false}}

    constructor(
        private router: Router,
        private modalService: BsModalService,
        private localizeService: LocalizationService,
        private settingsService:  SettingsService,
        private authService: AuthService,
        private brandingService: SystemLogoService
    ) {
        this.isAdmin = authService.isAdmin();
        this.isMaintainer = this.isAdmin || authService.isMaintainer();
        this.isContributor = this.isAdmin || this.isMaintainer || authService.isContributer();
     }

    ngOnInit(): void {

         this.settingsService.getOrganizations().then( orgs => {
            this.organizations = orgs
        } );

        this.installedLocales = this.getLocales();

    }


    public getCGRVersion(): string {
        return this.authService.getVersion();
    }

    public getLocales(): Locale[] {
        return this.authService.getLocales();
    }

    exportLocalization() {
        //this.localizationManagerService.exportLocalization();
        window.location.href = acp + "/localization/exportSpreadsheet";
    }

    public onEditOrganization(org: Organization): void {
        let bsModalRef = this.modalService.show( OrganizationModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );

        bsModalRef.content.organization = org;

        bsModalRef.content.onSuccess.subscribe( data => {
            this.organizations.push(data);
        })
    }

    public onRemoveOrganization(org: Organization): void {

    }

    public newOrganization(): void {
        let bsModalRef = this.modalService.show( OrganizationModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );

         bsModalRef.content.onSuccess.subscribe( data => {
            this.organizations.push(data);
         })
    }

    
    public newLocalization(): void {

        let bsModalRef = this.modalService.show( NewLocaleModalComponent, { 
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        } );

        bsModalRef.content.onSuccess.subscribe( data => {
            this.installedLocales.push(data);
        })
    }

    public configureEmail(): void {
        this.bsModalRef = this.modalService.show( EmailComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );

         this.bsModalRef.content.onSuccess.subscribe( data => {
            this.settings.email.isConfigured = data
         })
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
