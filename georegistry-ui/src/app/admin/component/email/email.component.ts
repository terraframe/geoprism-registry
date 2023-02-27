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

import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Subject } from 'rxjs';

import { ErrorHandler } from '@shared/component';
import { Email } from '@admin/model/email';
import { EmailService } from '@admin/service/email.service';

import { BsModalRef } from 'ngx-bootstrap/modal';


@Component({

	selector: 'email',
	templateUrl: './email.component.html',
	styleUrls: []
})
export class EmailComponent implements OnInit {
	message: string = null;
	public email: Email = {
		oid: '',
		server: '',
		username: '',
		password: '',
		port: 0,
		from: '',
		to: '',
	};

	public onSuccess: Subject<any>;

	constructor(private service: EmailService, public bsModalRef: BsModalRef) { }

	ngOnInit(): void {
		this.service.getInstance().then(email => {
			this.email = email;
		});

		this.onSuccess = new Subject();
	}

	cancel(): void {
		this.bsModalRef.hide();
	}

	onSubmit(): void {
		this.service.apply(this.email)
			.then(() => {
				this.onSuccess.next(true);
				this.bsModalRef.hide();
			})
			.catch((err: HttpErrorResponse) => {
				this.error(err);
			});
	}

	error(err: HttpErrorResponse): void {
		this.message = ErrorHandler.getMessageFromError(err);
	}
}
