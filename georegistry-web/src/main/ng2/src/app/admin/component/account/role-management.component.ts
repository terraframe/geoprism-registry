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
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';

import { Location } from '@angular/common';
import 'rxjs/add/operator/switchMap';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';

import { Account, User, Role, FormattedRoles, FormattedOrganization, FormattedGeoObjectTypeRoleGroup } from '../../model/account';
import { AccountService } from '../../service/account.service';

@Component( {
    selector: 'role-management',
    templateUrl: './role-management.component.html',
    styles: ['.modal-form .check-block .chk-area { margin: 10px 0px 0 0;}'],
    styleUrls: ['./role-management.css']
} )
export class RoleManagementComponent implements OnInit {

    message: string = null;

    _roles: FormattedRoles;

    @Input('roles')
    set roles(data: any){
        if(data){
            this._roles = this.formatRoles(data);
        }
    }

    @Output() onRoleIdsUpdate = new EventEmitter();

    _roleIds: string[] = [];
    @Input() newInstance: boolean = true;


    constructor(
        public bsModalRef: BsModalRef
    ) {

    }

    ngOnInit(): void {
    }


    formatRoles(roles: Role[]): any {

        let formattedObj: FormattedRoles = { "SRA":null, "ORGANIZATIONS":[] };

        roles.forEach(role => {
            role.isEnabled = false;
            
            if(role.orgCode){

                let addedToGroup = false;

                formattedObj.ORGANIZATIONS.forEach(orgGroup => {

                    if(orgGroup.ORGANIZATION && orgGroup.ORGANIZATION.length > 0){

                        if(orgGroup.ORGANIZATION === role.orgCode){

                            if(role.type === "RA"){
                                orgGroup.RA = role;
                            }
                            else{

                                let exists = false;
                                orgGroup.GEOOBJECTTYPEROLES.forEach(rg => {
                                    if (rg.GEOOBJECTTYPEID === role.geoObjectTypeCode) {
                                        rg.GEOOBJECTTYPEROLESGROUP.push(role);
                                        exists = true;
                                    }
                                });

                                if (!exists) {
                                    let roleGroup: FormattedGeoObjectTypeRoleGroup = { "GEOOBJECTTYPEROLESGROUP": [role], "ENABLEDROLE": "", "GEOOBJECTTYPEID": role.geoObjectTypeCode };
                                    orgGroup.GEOOBJECTTYPEROLES.push(roleGroup);
                                }
                            }

                            addedToGroup = true;
                        }
                    }
                    // This is an SRA if no ORGANIZATION
                    // else{
                    //     let groupOrgCode;
                    //     orgGroup.GEOOBJECTTYPEROLEGROUPS.forEach(geoObjTypeRole => {
                    //         groupOrgCode = geoObjTypeRole.orgCode;
                    //     });

                    //     if(groupOrgCode === role.orgCode){

                    //         if(role.type === "RA"){
                    //             orgGroup.RA = role;
                    //         }
                    //         else{
                    //             orgGroup.GEOOBJECTTYPEROLEGROUPS.push(role);
                    //         }

                    //         addedToGroup = true;
                    //     }
                    // }

                });

                if(!addedToGroup){

                    let newObj: FormattedOrganization = { "ORGANIZATION" : null, "RA" : null, "GEOOBJECTTYPEROLES" : [] };

                    if(role.type === "RA"){
                        newObj.ORGANIZATION = role.orgCode;
                        newObj.RA = role;
                    }
                    // else{
                    //     let geoObjTypeGroup = {"GEOOBJECTTYPE" : role.geoObjectTypeCode, "ROLES": [role]}
                    //     newObj.GEOOBJECTTYPEROLES.push(geoObjTypeGroup);
                    // }

                    formattedObj.ORGANIZATIONS.push(newObj)
                }
            }
            else if(role.type === "SRA"){
                formattedObj.SRA = role;
            }
        })

        return formattedObj;
    }


    onToggleOrgRA(event: any, role: Role): void {

        role.isEnabled = event;

        this.onChangeRole()
    }

    onChangeRole(): void {

        let newRoleIds: string[] = [];

        this._roles.ORGANIZATIONS.forEach(orgGroup => {

            if (orgGroup.RA && orgGroup.RA.isEnabled) {
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

        this._roleIds = newRoleIds;
        this.onRoleIdsUpdate.emit(this._roleIds);
    }

    removeRoleId(id: string): void {

        let pos = this._roleIds.indexOf(id);
        if(pos !== -1){
            this._roleIds.splice(pos, 1);
        }

        this.onRoleIdsUpdate.emit(JSON.stringify(this._roleIds));
    }

    showData(){
        console.log(this._roles)
    }


    public error( err: HttpErrorResponse ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.error.localizedMessage || err.error.message || err.message );
        }

    }

}