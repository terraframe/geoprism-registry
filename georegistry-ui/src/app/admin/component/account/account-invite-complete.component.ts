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

import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpErrorResponse } from "@angular/common/http";
import { Subscription } from 'rxjs';

import { User } from '@admin/model/account';
import { AccountService } from '@admin/service/account.service';

import { ErrorHandler } from '@shared/component';

import { GeoRegistryConfiguration } from "@core/model/core"; import { environment } from 'src/environments/environment';

@Component({
	selector: 'account-invite-complete',
	templateUrl: './account-invite-complete.component.html',
	styles: ['.modal-form .check-block .chk-area { margin: 10px 0px 0 0;}']
})
export class AccountInviteCompleteComponent implements OnInit, OnDestroy {
	user: User;
	sub: Subscription;
	token: string;
	message: string = null;

	constructor(
		private service: AccountService,
		private route: ActivatedRoute) {
	}

	ngOnInit(): void {
	  this.user = new User();

		this.sub = this.route.params.subscribe(params => {
			this.token = params['token'];
		});
	}

	ngOnDestroy(): void {
		this.sub.unsubscribe();
	}

	cancel(): void {
		window.location.href = environment.apiUrl;
	}

	onSubmit(): void {
		this.service.inviteComplete(this.user, this.token).then(response => {
			window.location.href = environment.apiUrl;
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	error(err: HttpErrorResponse): void {
		this.message = ErrorHandler.getMessageFromError(err);
	}
}