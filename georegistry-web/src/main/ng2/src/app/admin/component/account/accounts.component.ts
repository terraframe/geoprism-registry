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
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { ErrorHandler, ErrorModalComponent } from '@shared/component';

import { PageResult } from '@shared/model/core';

import { User, Account } from '@admin/model/account';
import { AccountService } from '@admin/service/account.service';
import { AccountComponent } from './account.component';

@Component({
	selector: 'accounts',
	templateUrl: './accounts.component.html',
	styleUrls: ['./accounts.css']
})
export class AccountsComponent implements OnInit {
	res: PageResult<User> = {
		resultSet: [],
		count: 0,
		pageNumber: 1,
		pageSize: 10
	};
	bsModalRef: BsModalRef;
	message: string = null;

	constructor(
		private service: AccountService,
		private modalService: BsModalService
	) { }

	ngOnInit(): void {
		this.service.page(1).then(res => {
			this.res = res;
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}


	edit(user: User): void {
		// this.router.navigate(['/admin/account', user.oid]);

		this.bsModalRef = this.modalService.show(AccountComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
		});
		this.bsModalRef.content.oid = user.oid;

		let that = this;
		this.bsModalRef.content.onEdit.subscribe((data: Account) => {
			
			let index = that.res.resultSet.findIndex(u => u.oid === data.user.oid);

			if (index !== -1) {
				that.res.resultSet[index] = data.user;
			}
		});
	}

	newInstance(): void {
		// this.router.navigate(['/admin/account', 'NEW']);

		this.bsModalRef = this.modalService.show(AccountComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
		});
		this.bsModalRef.content.oid = 'NEW';

		this.bsModalRef.content.onEdit.subscribe(data => {
			this.onPageChange(this.res.pageNumber);
		});

	}

	onPageChange(pageNumber: number): void {
		this.service.page(pageNumber).then(res => {
			this.res = res;
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	public error(err: HttpErrorResponse): void {
			this.bsModalRef = this.modalService.show(ErrorModalComponent, { backdrop: true });
			this.bsModalRef.content.message = ErrorHandler.getMessageFromError(err);
	}
}
