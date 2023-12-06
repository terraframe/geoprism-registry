///
/// Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Geoprism Registry(tm).
///
/// Geoprism Registry(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Geoprism Registry(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { Component, OnDestroy, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';
import { ErrorHandler } from '@shared/component';
import { Organization } from '@shared/model/core';

import { LocalizationService, OrganizationService } from '@shared/service';

@Component({
	selector: 'organization-modal',
	templateUrl: './organization-modal.component.html',
	styles: ['.modal-form .check-block .chk-area { margin: 10px 0px 0 0;}']
})
export class OrganizationModalComponent implements OnInit, OnDestroy {

	message: string = null;
	organization: Organization = { code: "", label: this.lService.create(), contactInfo: this.lService.create(), enabled: true };
	isNewOrganization: boolean = true;

	public onSuccess: Subject<Organization>;

	constructor(private orgService: OrganizationService, public bsModalRef: BsModalRef, private lService: LocalizationService) { }

	ngOnInit(): void {
		this.onSuccess = new Subject();
	}

	ngOnDestroy(): void {
		this.onSuccess.unsubscribe();
	}

	cancel(): void {
		this.bsModalRef.hide();
	}

	onSubmit(): void {
		if (this.isNewOrganization) {
			this.orgService.newOrganization(this.organization).then(data => {
				this.onSuccess.next(data);
				this.bsModalRef.hide();
			}).catch((err: HttpErrorResponse) => {
				this.error(err);
			});
		}
		else {
			this.orgService.updateOrganization(this.organization).then(data => {
				this.onSuccess.next(data);
				this.bsModalRef.hide();
			}).catch((err: HttpErrorResponse) => {
				this.error(err);
			});
		}
	}

	public error(err: HttpErrorResponse): void {
		this.message = ErrorHandler.getMessageFromError(err);
	}

}