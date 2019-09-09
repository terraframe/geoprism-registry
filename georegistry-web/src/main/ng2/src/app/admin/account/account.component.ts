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
import { Location } from '@angular/common';
import 'rxjs/add/operator/switchMap';
import { Account } from './account';
import { AccountService } from './account.service';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';
import { User } from './account';


@Component({
	selector: 'account',
	templateUrl: './account.component.html',
	styles: ['.modal-form .check-block .chk-area { margin: 10px 0px 0 0;}']
})
export class AccountComponent implements OnInit {

	message: string = null;
	account: Account;
	
	@Input()
	set oid(oid: string) {
		if (oid === 'NEW') {
			this.service.newInstance()
				.then(data => {
					this.account = data;
				})
				.catch((err: Response) => {
					this.error( err.json() );
				});
		}
		else if (oid) {
			this.service.edit(oid)
				.then(data => {
					this.account = data;
				})
				.catch((err: Response) => {
					this.error( err.json() );
				});
		}
	}

	public onEdit: Subject<User>;

	constructor(
		private service: AccountService,
		private location: Location,
		public bsModalRef: BsModalRef
		) {
	}

	ngOnInit(): void {
		// this.account = this.route.snapshot.data['account'];

		this.onEdit = new Subject();
	}

	cancel(): void {
		if (this.account.user.newInstance === true) {
			this.bsModalRef.hide();
		}
		else {
			this.service.unlock(this.account.user.oid).then(response => {
				this.bsModalRef.hide();
			});
		}
	}

	onSubmit(): void {
		let roleIds: string[] = [];

		for (let i = 0; i < this.account.groups.length; i++) {
			let group = this.account.groups[i];

			roleIds.push(group.assigned);
			//      for(let j = 0; j < group.roles.length; j++) {
			//        let role = group.roles[j];
			//        
			//        if(role.assigned) {
			//          roleIds.push(role.roleId);
			//        }      
			//      }    
		}

		if (!this.account.changePassword && !this.account.user.newInstance) {
			delete this.account.user.password;
		}

		this.service.apply(this.account.user, roleIds).then(data => {
			this.onEdit.next( data );
			this.bsModalRef.hide();
        } ).catch(( err: Response ) => {
            this.error( err.json() );
        } );
	}

	error(err: any): void {
		// Handle error
		if (err !== null) {
			this.message = (err.localizedMessage || err.message);
		}
	}
}