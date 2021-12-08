import { Component, OnInit } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { HttpErrorResponse } from "@angular/common/http";

import { RegistryService } from "@registry/service";

import { ErrorHandler } from "@shared/component";
import { Organization } from "@shared/model/core";
import { GeoObjectType } from "@registry/model/registry";
import { ListType, ListTypeByType } from "@registry/model/list-type";
import { ListTypeService } from "@registry/service/list-type.service";
import { Location } from "@angular/common";

@Component({
    selector: "list-type-manager",
    templateUrl: "./list-type-manager.component.html",
    styleUrls: ["./list-type-manager.css"]
})
export class ListTypeManagerComponent implements OnInit {

    message: string = null;
    typesByOrg: { org: Organization, types: GeoObjectType[] }[] = [];

    listByType: ListTypeByType = null;
    current: ListType = null;

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private service: ListTypeService,
        private registryService: RegistryService,
        private route: ActivatedRoute,
        private location: Location) { }

    ngOnInit(): void {

        this.route.paramMap.subscribe(params => {
            const typeCode = params.get("typeCode");
            const listId = params.get("listId");

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

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
