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

import { Component, OnDestroy, OnInit, ViewChildren, QueryList } from "@angular/core";
import { ActivatedRoute, Params } from "@angular/router";
import { HttpErrorResponse } from "@angular/common/http";

import { RegistryService } from "@registry/service";
import { AuthService } from "@shared/service/auth.service";

import { ErrorHandler } from "@shared/component";
import { Organization } from "@shared/model/core";
import { GeoObjectType } from "@registry/model/registry";
import { ListType, ListTypeByType } from "@registry/model/list-type";
import { ListTypeService } from "@registry/service/list-type.service";
import { Subscription } from "rxjs";
import Utils from "@registry/utility/Utils";

@Component({
    selector: "list-type-manager",
    templateUrl: "./list-type-manager.component.html",
    styleUrls: ["./list-type-manager.css"]
})
export class ListTypeManagerComponent implements OnInit, OnDestroy {

    message: string = null;
    typesByOrg: { org: Organization, types: GeoObjectType[] }[] = [];

    listByType: ListTypeByType = null;
    current: ListType = null;

    subscription: Subscription = null;

    noQueryParams = false;

    @ViewChildren("typesByOrgIter") typesByOrgIterEls: QueryList<any>;

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private service: ListTypeService,
        private registryService: RegistryService,
        private route: ActivatedRoute,
        private authService: AuthService) { }

    ngOnInit(): void {
        this.subscription = this.route.queryParams.subscribe((params: Params) => {
            const typeCode = params.typeCode;
            const listId = params.listId;

            if (listId != null && listId.length > 0) {
                this.service.entries(listId).then(current => {
                    this.current = current;
                    this.listByType = null;
                }).catch((err: HttpErrorResponse) => {
                    this.error(err);
                });
            } else if (typeCode != null && typeCode.length > 0) {
                this.service.listForType(typeCode).then(listByType => {
                    this.listByType = listByType;
                    this.current = null;
                }).catch((err: HttpErrorResponse) => {
                    this.error(err);
                });
            } else {
                this.noQueryParams = true;
            }

            // this.refresh();
        });

        if (this.typesByOrg.length === 0) {
            this.registryService.init(this.authService.isPublic()).then(response => {
                this.typesByOrg = [];

                //
                // Order alphabetically
                // TODO: sort these on the server
                //
                response.organizations.sort((a, b) => {
                    if (a.label.localizedValue < b.label.localizedValue) {
                        return -1;
                    }
                    if (a.label.localizedValue > b.label.localizedValue) {
                        return 1;
                    }
                    return 0;
                });
                //
                // End sort

                // put org of the user on top
                if (!this.authService.isSRA()) {
                    let pos = null;
                    let myorg = this.authService.getMyOrganizations();
                    pos = response.organizations.findIndex(org => {
                        return org.code === myorg[0];
                    });

                    if (pos >= 0) {
                        Utils.arrayMove(response.organizations, pos, 0);
                    }
                }

                response.organizations.forEach((org, index) => {
                    //
                    // Post processing to better handle groups in the frontend
                    //
                    let orgTypes = response.types.filter(t => t.organizationCode === org.code);
                    let orgTypesNoGroupMembers = orgTypes.filter(t => !t.superTypeCode);

                    orgTypesNoGroupMembers.sort((a, b) => {
                        if (a.label.localizedValue < b.label.localizedValue) {
                            return -1;
                        }
                        if (a.label.localizedValue > b.label.localizedValue) {
                            return 1;
                        }
                        return 0;
                    });

                    let groupTypes = [];
                    let groups = orgTypesNoGroupMembers.filter(gType => gType.isAbstract);
                    groups.forEach(group => {
                        let groupType = { group: group, members: [] };
                        orgTypes.forEach(t => {
                            if (t.superTypeCode === group.code) {
                                groupType.members.push(t);
                            }
                        });
                        groupTypes.push(groupType);
                    });

                    groupTypes.forEach(grpT => {
                        let index = orgTypesNoGroupMembers.findIndex(grp => grpT.group.code === grp.code);
                        if (index !== -1) {
                            orgTypesNoGroupMembers.splice(index + 1, 0, ...grpT.members);
                        }
                    });
                    //
                    // End post processing
                    //

                    this.typesByOrg.push({ org: org, types: orgTypesNoGroupMembers });
                });
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        }
    }

    ngAfterViewInit() {
        this.typesByOrgIterEls.changes.subscribe(t => {
            // Select the first type on load if no URL type params
            if (this.noQueryParams && t.length > 0) {
                let els = document.getElementsByClassName("got-li-item");
                if (els && els.length > 0) {
                    let el = els[0].firstChild as HTMLElement;
                    el.click();
                }
            }
        });
    }

    ngOnDestroy(): void {
        if (this.subscription != null) {
            this.subscription.unsubscribe();
        }
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
