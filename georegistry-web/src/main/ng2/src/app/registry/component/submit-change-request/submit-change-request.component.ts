import { Component, OnInit, ViewChild } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import { Observable } from "rxjs";
import { TypeaheadMatch } from "ngx-bootstrap/typeahead";

import { ErrorHandler, ErrorModalComponent, SuccessModalComponent } from "@shared/component";
import { LocalizationService, AuthService } from "@shared/service";

import { RegistryService, ChangeRequestService } from "@registry/service";
import { GeoObjectType, GeoObjectOverTime } from "@registry/model/registry";

@Component({
    selector: "submit-change-request",
    templateUrl: "./submit-change-request.component.html",
    styleUrls: ["./submit-change-request.css"]
})
export class SubmitChangeRequestComponent implements OnInit {

    /*
     * Reference to the modal current showing
     */

    bsModalRef: BsModalRef;

    geoObjectType: GeoObjectType;

    geoObjectTypes: GeoObjectType[] = [];

    geoObjectId: string = "";

    reason: string = "";

    dataSource: Observable<any>;

    dateStr: string = null;

    forDate: Date = null;

    @ViewChild("attributeEditor") attributeEditor;

    @ViewChild("geometryEditor") geometryEditor;

    geoObject: GeoObjectOverTime = null;

    isValid: boolean = false;

    loading: boolean = false;

    geoObjectAttributeExcludes: string[] = ["uid", "sequence", "type", "lastUpdateDate", "createDate", "status"];

    constructor(private modalService: BsModalService, private registryService: RegistryService,
        private changeRequestService: ChangeRequestService, private localizeService: LocalizationService, private authService: AuthService) {
        this.dataSource = Observable.create((observer: any) => {
            this.registryService.getGeoObjectSuggestionsTypeAhead(this.geoObjectId, this.geoObjectType.code).then(results => {
                observer.next(results);
            });
        });
    }

    ngOnInit(): void {
        this.registryService.getGeoObjectTypes([], null).then(types => {
            let myOrgTypes = [];
            for (let i = 0; i < types.length; ++i) {
                const type = types[i];
                const orgCode = type.organizationCode;
                const typeCode = type.superTypeCode != null && type.superTypeCode != "" ? type.superTypeCode : type.code;

                if (this.authService.isGeoObjectTypeRC(orgCode, typeCode)) {
                    myOrgTypes.push(types[i]);
                }
            }
            this.geoObjectTypes = myOrgTypes;

            this.geoObjectTypes.sort((a, b) => {
                if (a.label.localizedValue.toLowerCase() < b.label.localizedValue.toLowerCase()) return -1;
                else if (a.label.localizedValue.toLowerCase() > b.label.localizedValue.toLowerCase()) return 1;
                else return 0;
            });

            let pos = this.getGeoObjectTypePosition("ROOT");
            if (pos) {
                this.geoObjectTypes.splice(pos, 1);
            }

            // this.currentGeoObjectType = this.geoObjectTypes[1];
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    public handleDateChange() {
        if (this.dateStr != null) {
            this.forDate = new Date(Date.parse(this.dateStr));
        } else {
            this.forDate = null;
        }
    }

    public onValidChange(newValid: boolean) {
        if (this.geoObject == null) {
            this.isValid = false;
            return;
        }

        if (this.geometryEditor != null && !this.geometryEditor.getIsValid()) {
            this.isValid = false;
            return;
        }

        if (this.attributeEditor != null && !this.attributeEditor.getIsValid()) {
            this.isValid = false;
            return;
        }

        this.isValid = true;
    }

    private getGeoObjectTypePosition(code: string): number {
        for (let i = 0; i < this.geoObjectTypes.length; i++) {
            let obj = this.geoObjectTypes[i];
            if (obj.code === code) {
                return i;
            }
        }

        return null;
    }

    changeTypeaheadLoading(e: boolean): void {
        this.loading = e;
    }

    typeaheadOnSelect(e: TypeaheadMatch): void {
        this.registryService.getGeoObjectOverTime(e.item.code, this.geoObjectType.code).then(geoObject => {
            this.geoObject = geoObject;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    cancel(): void {
        this.isValid = false;
        this.geoObject = null;
        this.geoObjectId = null;
        this.geoObjectType = null;
        this.reason = null;
    }

    public error(err: any): void {
        this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

}
