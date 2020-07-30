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

import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ErrorHandler } from '@shared/component';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { AuthService } from '@shared/service';

import { Role, FormattedRoles, FormattedOrganization, FormattedGeoObjectTypeRoleGroup } from '@admin/model/account';
import { RegistryRoleType } from '@shared/model/core';

@Component({
	selector: 'role-management',
	templateUrl: './role-management.component.html',
	styles: ['.modal-form .check-block .chk-area { margin: 10px 0px 0 0;}'],
	styleUrls: ['./role-management.css']
})
export class RoleManagementComponent implements OnInit {

	message: string = null;
	isAdmin: boolean;
	isMaintainer: boolean;
	isContributor: boolean;
	isSRA: boolean;

	_roles: FormattedRoles;

	@Input('roles')
	set roles(data: any) {
		if (data) {
			this._roles = this.formatRoles(data);
			this.onChangeRole();
		}
	}

	@Output() onRoleIdsUpdate = new EventEmitter();

	_roleIds: string[] = [];
	@Input() newInstance: boolean = true;


	constructor(
		public bsModalRef: BsModalRef, private authService: AuthService
	) {

		this.isSRA = authService.isSRA();
		this.isAdmin = authService.isAdmin();
		this.isMaintainer = this.isAdmin || authService.isMaintainer();
		this.isContributor = this.isAdmin || this.isMaintainer || authService.isContributer();

	}

	ngOnInit(): void {
	}


	formatRoles(roles: Role[]): any {

		let formattedObj: FormattedRoles = { "SRA": null, "ORGANIZATIONS": [] };

		roles.forEach(role => {

			// If orgCode exists this is NOT an SRA
			if (role.orgCode) {

				let addedToGroup = false;

				formattedObj.ORGANIZATIONS.forEach(orgGroup => {

					if (orgGroup.ORGANIZATIONLABEL === role.orgLabel.localizedValue) {

						if (role.type === "RA") {
							orgGroup.RA = role;
						}
						else {

							let added = this.addToGeoObjectTypeGroup(orgGroup, role);

							if (!added) {
								let geoObjectTypeGroup: FormattedGeoObjectTypeRoleGroup = { "GEOOBJECTTYPEROLESGROUP": [role], "ENABLEDROLE": "", "GEOOBJECTTYPELABEL": role.geoObjectTypeLabel.localizedValue };

								if (role.assigned) {
									geoObjectTypeGroup.ENABLEDROLE = role.name
								}

								orgGroup.GEOOBJECTTYPEROLES.push(geoObjectTypeGroup);
							}
						}

						addedToGroup = true;
					}

				});


				// The organization hasn't been created yet
				if (!addedToGroup) {

					let newObj: FormattedOrganization = { "ORGANIZATIONLABEL": null, "RA": null, "GEOOBJECTTYPEROLES": [] };

					if (role.type === "RA") {
						newObj.ORGANIZATIONLABEL = role.orgLabel.localizedValue;
						newObj.RA = role;
					}
					else {
						newObj.ORGANIZATIONLABEL = role.orgLabel.localizedValue;

						let geoObjectTypeGroup: FormattedGeoObjectTypeRoleGroup = { "GEOOBJECTTYPEROLESGROUP": [role], "ENABLEDROLE": "", "GEOOBJECTTYPELABEL": role.geoObjectTypeLabel.localizedValue };

						if (role.assigned) {
							geoObjectTypeGroup.ENABLEDROLE = role.name
						}

						newObj.GEOOBJECTTYPEROLES.push(geoObjectTypeGroup);
					}

					formattedObj.ORGANIZATIONS.push(newObj)
				}
			}
			else if (role.type === "SRA") {
				formattedObj.SRA = role;
			}
		})

		this.sortRoles(formattedObj);

		return formattedObj;
	}

	sortRoles(roles: FormattedRoles): void {
		roles.ORGANIZATIONS.forEach(org => {
			org.GEOOBJECTTYPEROLES.forEach(gotrole => {
				gotrole.GEOOBJECTTYPEROLESGROUP.sort((a, b) => {
					if (RegistryRoleType[a.type] < RegistryRoleType[b.type]) return -1;
					if (RegistryRoleType[a.type] > RegistryRoleType[b.type]) return 1;
					return 0;
				});
			})
		});
	}

	addToGeoObjectTypeGroup(organization: FormattedOrganization, role: Role): boolean {
		let exists = false;
		organization.GEOOBJECTTYPEROLES.forEach(rg => {
			if (rg.GEOOBJECTTYPELABEL === role.geoObjectTypeLabel.localizedValue) {

				if (role.assigned) {
					rg.ENABLEDROLE = role.name
				}

				rg.GEOOBJECTTYPEROLESGROUP.push(role);

				exists = true;
			}
		});

		return exists;
	}

	onToggleOrgRA(event: any, organization: FormattedOrganization): void {

		organization.RA.assigned = event;

		// Disable all GeoObjectType radio buttons in this organization
		if (organization.RA.assigned) {
			organization.GEOOBJECTTYPEROLES.forEach(rg => {
				rg.ENABLEDROLE = "";
			});
		}

		this.onChangeRole();
	}

	onToggleSRA(event: any, role: Role): void {

		this._roles.ORGANIZATIONS.forEach(org => {
			org.GEOOBJECTTYPEROLES.forEach(rg => {
				rg.ENABLEDROLE = "";
			});
		})

		this.onChangeRole();
	}

	setGroupRole(group: FormattedGeoObjectTypeRoleGroup, role: Role, event:any): void {	
		
		if(role === null){
			group.ENABLEDROLE = "";
		}
		else{
			group.ENABLEDROLE = (event.target.checked) ? role.name : "";
		}
		
		this.onChangeRole();
	}

	onChangeRole(): void {

		let newRoleIds: string[] = [];

		this._roles.ORGANIZATIONS.forEach(orgGroup => {

			if (orgGroup.RA && orgGroup.RA.assigned) {
				newRoleIds.push(orgGroup.RA.name);
			}
			// If organization RA is enabled we don't add GeoObjectType level roles
			else {
				orgGroup.GEOOBJECTTYPEROLES.forEach(rg => {
					if (rg.ENABLEDROLE && rg.ENABLEDROLE.length > 0) {
						// add GeoObjectType level role selected
						newRoleIds.push(rg.ENABLEDROLE);
					}
				});
			}
		});

		if(this._roles.SRA && this._roles.SRA.assigned) {
			newRoleIds.push(this._roles.SRA.name);
		}

		this._roleIds = newRoleIds;
		this.onRoleIdsUpdate.emit(this._roleIds);
	}

	removeRoleId(id: string): void {

		let pos = this._roleIds.indexOf(id);
		if (pos !== -1) {
			this._roleIds.splice(pos, 1);
		}

		this.onRoleIdsUpdate.emit(JSON.stringify(this._roleIds));
	}

	showData() {
		console.log(this._roles)
	}


	public error(err: HttpErrorResponse): void {
			this.message = ErrorHandler.getMessageFromError(err);
	}

}