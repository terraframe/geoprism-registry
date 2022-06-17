import { Component, OnInit, ViewChild } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import { Observable } from "rxjs";
import { TypeaheadMatch } from "ngx-bootstrap/typeahead";

import { ErrorHandler } from "@shared/component";
import { LocalizationService, AuthService, DateService } from "@shared/service";

import * as ColorGen from "color-generator";
import { RegistryService, ChangeRequestService, GeometryService } from "@registry/service";
import { GeoObjectType, GeoObjectOverTime } from "@registry/model/registry";
import { GeoObjectLayerDataSource } from "@registry/service/layer-data-source";
import { Router } from "@angular/router";
import { RegistryCacheService } from "@registry/service/registry-cache.service";

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

    constructor(private modalService: BsModalService, private registryService: RegistryService, private geomService: GeometryService,
        private changeRequestService: ChangeRequestService, private localizeService: LocalizationService, private authService: AuthService,
        private router: Router, private dateService: DateService, private cacheService: RegistryCacheService) {
        this.dataSource = Observable.create((observer: any) => {
            this.registryService.getGeoObjectSuggestionsTypeAhead(this.geoObjectId, this.geoObjectType.code).then(results => {
                observer.next(results);
            });
        });
    }

    ngOnInit(): void {
        this.cacheService.getTypeCache().waitOnTypes().then((types: GeoObjectType[]) => {
        // this.registryService.getGeoObjectTypes([], null).then(types => {
            let myOrgTypes = [];
            for (let i = 0; i < types.length; ++i) {
                const type = types[i];
                const orgCode = type.organizationCode;
                const typeCode = type.superTypeCode != null && type.superTypeCode !== "" ? type.superTypeCode : type.code;

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

    onNewChangeRequest() {
        let dataSource = new GeoObjectLayerDataSource(this.registryService, this.geoObject.attributes.code, this.geoObject.attributes.type, this.dateStr);

        let displayLabel = (this.geoObject.attributes["displayLabel"].values && this.geoObject.attributes["displayLabel"].values.length > 0) ? this.geoObject.attributes["displayLabel"].values[0].value.localizedValue : this.geoObject.attributes.code;
        let typeLabel = this.geoObjectType.label.localizedValue;
        let sDate = this.dateStr == null ? "" : " " + this.dateService.formatDateForDisplay(this.dateStr);
        let label = displayLabel + " " + sDate + "(" + typeLabel + ")";

        let layer = dataSource.createLayer(label, true, ColorGen().hexString());

        this.geomService.zoomOnReady(layer.getId());
        let layers = this.geomService.getDataSourceFactory().serializeLayers([layer]);

        const params: any = { layers: JSON.stringify(layers) };

        params.attrPanelOpen = true;
        params.objectType = "GEOOBJECT";
        params.type = this.geoObjectType.code;
        params.code = this.geoObject.attributes.code;
        params.date = this.dateStr;

        this.router.navigate(["/registry/location-manager"], {
            queryParams: params
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
