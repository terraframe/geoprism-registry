import { Component, OnDestroy, OnInit } from "@angular/core";
import { ActivatedRoute, Params } from "@angular/router";
import { HttpErrorResponse } from "@angular/common/http";

import { RegistryService } from "@registry/service";
import { AuthService } from "@shared/service/auth.service";

import { ErrorHandler } from "@shared/component";
import { Organization } from "@shared/model/core";
import { GeoObjectType } from "@registry/model/registry";
import { ListType, ListTypeByType } from "@registry/model/list-type";
import { ListTypeService } from "@registry/service/list-type.service";
import { Location } from "@angular/common";
import { Subscription } from "rxjs";

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

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private service: ListTypeService,
        private registryService: RegistryService,
        private route: ActivatedRoute,
        private location: Location, 
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
            }
            else if (typeCode != null && typeCode.length > 0) {

                this.service.listForType(typeCode).then(listByType => {
                    this.listByType = listByType;
                    this.current = null;
                }).catch((err: HttpErrorResponse) => {
                    this.error(err);
                });
            }

            // this.refresh();
        });

        if (this.typesByOrg.length === 0) {
            this.registryService.init().then(response => {
                this.typesByOrg = [];
                
                //
                // Order alphabetically
                // TODO: sort these on the server
                //
                function compare( a, b ) {
                  if ( a.label.localizedValue < b.label.localizedValue ){
                    return -1;
                  }
                  if ( a.label.localizedValue > b.label.localizedValue ){
                    return 1;
                  }
                  return 0;
                }
                response.organizations.sort( compare );
                //
                // End sort
                
                // put org of the user on top
                if(!this.authService.isSRA()) {
                    let pos = null;
                    let myorg = this.authService.getMyOrganizations();
                    pos = response.organizations.findIndex(org => {
                      return org.code === myorg[0];
                    });
                    
                    if(pos >= 0) {
                        this.array_move(response.organizations, pos, 0);
                    }
                }
                

                response.organizations.forEach(org => {
                    
                    //
                    // Post processing to better handle groups in the frontend
                    //
                    let orgTypes = response.types.filter(t => t.organizationCode === org.code);
                    let orgTypesNoGroupMembers = orgTypes.filter(t => !t.superTypeCode);
                    
                    function compare( a, b ) {
                      if ( a.label.localizedValue < b.label.localizedValue){
                        return -1;
                      }
                      if ( a.label.localizedValue > b.label.localizedValue ){
                        return 1;
                      }
                      return 0;
                    }
                    
                    orgTypesNoGroupMembers.sort(compare);
                    
                    let groupTypes = [];
                    let groups = orgTypesNoGroupMembers.filter(gType => gType.isAbstract);
                    groups.forEach(group => {
                        let groupType = {group:group, members:[]};
                        orgTypes.forEach(t => {
                            
                            if(t.superTypeCode === group.code){
                                groupType.members.push(t);
                            }
                        });
                        groupTypes.push(groupType);
                    });
                    
                    groupTypes.forEach(grpT => {
                        let index = orgTypesNoGroupMembers.findIndex(grp => grpT.group.code === grp.code);
                        if(index !== -1){
                            orgTypesNoGroupMembers.splice(index+1, 0, ...grpT.members);
                        }
                    });
                    //
                    // End post processing
                    //
                    
                    
                    this.typesByOrg.push({ org: org, types: orgTypesNoGroupMembers });
                });
                
                // Select the first type on load if no URL type params
                setTimeout(() => {
                    if (this.typesByOrg.length > 0) {
                        
                        let els = document.getElementsByClassName("got-li-item");
                        if(els && els.length > 0) {
                            let el = els[0].firstChild as HTMLElement;
                            el.click();
                        }
                    }
                }, 0);
                
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        }
    }
    
    array_move(arr, old_index, new_index): void {
        if (new_index >= arr.length) {
            var k = new_index - arr.length + 1;
            while (k--) {
                arr.push(undefined);
            }
        }
        arr.splice(new_index, 0, arr.splice(old_index, 1)[0]);
    };

    ngOnDestroy(): void {
        if (this.subscription != null) {
            this.subscription.unsubscribe();
        }
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
