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
import { HttpErrorResponse } from "@angular/common/http";

import { BsModalRef } from 'ngx-bootstrap/modal';

import { ErrorHandler } from '@shared/component';
import { Account, UserInvite } from '@admin/model/account';
import { Organization } from '@shared/model/core';

import { SettingsService } from '@admin/service/settings.service'
import { AccountService } from '@admin/service/account.service';
import { AuthService } from '@shared/service';


@Component({
	selector: 'account-invite',
	templateUrl: './account-invite.component.html',
	styles: ['.modal-form .check-block .chk-area { margin: 10px 0px 0 0;}']
})
export class AccountInviteComponent implements OnInit {
	invite: UserInvite;
	message: string = null;
	roleIds: string[] = [];
	organization: Organization;
	organizations: Organization[];

	constructor(
		private service: AccountService,
		private authService: AuthService,
		public bsModalRef: BsModalRef,
		public settingsService: SettingsService) {
	}

	ngOnInit(): void {
		this.invite = new UserInvite();
		let orgCodes = this.authService.getMyOrganizations();

		this.service.newInvite(orgCodes).then((account: Account) => {
			this.invite.roles = account.roles;
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});

		// this.settingsService.getOrganizations().then(orgs => {
		//     this.organizations = orgs
		// }).catch((err: HttpErrorResponse) => {
		//     this.error(err);
		// });
	}

	cancel(): void {
		this.bsModalRef.hide();
	}

	onRoleIdsUpdate(roleIds: string[]): void {
		this.roleIds = roleIds;
	}

	onSubmit(): void {
		this.service.inviteUser(this.invite, this.roleIds).then(() => {
			this.bsModalRef.hide();
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	error(err: HttpErrorResponse): void {
		this.message = ErrorHandler.getMessageFromError(err);
	}
}