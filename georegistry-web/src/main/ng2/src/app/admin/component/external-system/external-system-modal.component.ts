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
import { HttpErrorResponse } from '@angular/common/http';

import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';

import { ExternalSystem, Organization } from '@shared/model/core';
import { ExternalSystemService } from '@shared/service/external-system.service';
import { LocalizationService } from '@shared/service/localization.service';
import { AuthService } from '@shared/service/auth.service';
import { ErrorHandler } from '@shared/component/error-handler/error-handler';

@Component({
	selector: 'external-system-modal',
	templateUrl: './external-system-modal.component.html',
	styles: ['.modal-form .check-block .chk-area { margin: 10px 0px 0 0;}']
})
export class ExternalSystemModalComponent implements OnInit {

	message: string = null;

	system: ExternalSystem = {
		id: "",
		type: 'DHIS2ExternalSystem',
		organization: "",
		label: this.lService.create(),
		description: this.lService.create()
	};

	organizations: Organization[] = [];


	public onSuccess: Subject<ExternalSystem>;

	constructor(private systemService: ExternalSystemService, private authService: AuthService, public bsModalRef: BsModalRef, private lService: LocalizationService) { }

	ngOnInit(): void {
		this.onSuccess = new Subject();
	}

	init(organizations: Organization[], system: ExternalSystem): void {
		this.organizations = organizations.filter(o => {
			return this.authService.isOrganizationRA(o.code);
		});

		if (system != null) {
			this.system = system;
		}
	}

	cancel(): void {
		this.bsModalRef.hide();
	}

	onSubmit(): void {
		this.systemService.applyExternalSystem(this.system).then(data => {
			this.onSuccess.next(data);
			this.bsModalRef.hide();
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	public error(err: HttpErrorResponse): void {
			this.message = ErrorHandler.getMessageFromError(err);
	}

}