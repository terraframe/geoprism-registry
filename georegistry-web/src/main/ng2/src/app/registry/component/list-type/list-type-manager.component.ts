import { Component, OnDestroy, OnInit } from "@angular/core";
import { ActivatedRoute, Params } from "@angular/router";
import { HttpErrorResponse } from "@angular/common/http";

import { RegistryService } from "@registry/service";

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
        private location: Location) { }

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

                response.organizations.forEach(org => {
                    this.typesByOrg.push({ org: org, types: response.types.filter(t => t.organizationCode === org.code) });
                })
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        }
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
