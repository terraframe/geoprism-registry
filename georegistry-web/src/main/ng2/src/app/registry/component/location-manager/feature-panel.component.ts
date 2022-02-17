import { Component, OnInit, Input, Output, EventEmitter, ViewChild, ElementRef, OnChanges, SimpleChanges } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";

import { GeoObjectType, GeoObjectOverTime, AttributeType, HierarchyOverTime } from "@registry/model/registry";
import { RegistryService, GeometryService } from "@registry/service";
import { AuthService } from "@shared/service";
import { ErrorHandler } from "@shared/component";
import { CreateGeoObjectAction } from "@registry/model/crtable";
import { fakeAsync } from "@angular/core/testing";

@Component({
    selector: "feature-panel",
    templateUrl: "./feature-panel.component.html",
    styleUrls: ["./dataset-location-manager.css"]
})
export class FeaturePanelComponent implements OnInit, OnChanges {

    MODE = {
        VERSIONS: "VERSIONS",
        ATTRIBUTES: "ATTRIBUTES",
        HIERARCHY: "HIERARCHY",
        GEOMETRY: "GEOMETRY"
    }

    @Input() datasetId: string;

    @Input() type: GeoObjectType;

    @Input() forDate: Date = new Date();

    @Output() forDateChange = new EventEmitter<string>();

    @Input() readOnly: boolean = false;

    @Input() code: string;

    @ViewChild("attributeEditor") attributeEditor;

    @Output() featureChange = new EventEmitter<GeoObjectOverTime>();
    @Output() modeChange = new EventEmitter<boolean>();
    @Output() panelCancel = new EventEmitter<void>();
    @Output() panelSubmit = new EventEmitter<{ isChangeRequest: boolean, geoObject?: any, changeRequestId?: string }>();

    _isValid: boolean = true;

    bsModalRef: BsModalRef;

    mode: string = null;

    isMaintainer: boolean;

    // The current state of the GeoObject in the GeoRegistry
    preGeoObject: GeoObjectOverTime;

    // The state of the GeoObject after our edit has been applied
    postGeoObject: GeoObjectOverTime;

    attribute: AttributeType = null;

    isNew: boolean = false;

    isEdit: boolean = false;

    hierarchies: HierarchyOverTime[];

    hierarchy: HierarchyOverTime = null;

    // Flag indicating if the component is communicating with the server
    inProgress: number = 0;

    reason: string = "";

    constructor(public service: RegistryService, private modalService: BsModalService, private authService: AuthService, private geometryService: GeometryService) { }

    ngOnInit(): void {
        this.isMaintainer = this.authService.isSRA() || this.authService.isOrganizationRA(this.type.organizationCode) || this.authService.isGeoObjectTypeOrSuperRM(this.type);
        this.mode = "ATTRIBUTES";

        //        this.isEdit = !this.readOnly;
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.type != null || changes.code != null) {
            this.refresh();
        }
    }

    setValid(valid: boolean): void {
        this._isValid = valid;
    }

    isValid(): boolean {
        return this._isValid && this.attributeEditor && this.attributeEditor.isValid();
    }

    updateCode(code: string): void {
        this.code = code;
    }

    refresh(): void {
        this.postGeoObject = null;
        this.preGeoObject = null;
        this.hierarchies = null;

        if (this.code != null && this.type != null) {
            if (this.code !== "__NEW__") {
                this.isNew = false;

                this.inProgress++;

                this.service.getGeoObjectOverTime(this.code, this.type.code).then(geoObject => {
                    this.preGeoObject = new GeoObjectOverTime(this.type, JSON.parse(JSON.stringify(geoObject)).attributes);
                    this.postGeoObject = new GeoObjectOverTime(this.type, JSON.parse(JSON.stringify(this.preGeoObject)).attributes);
                }).catch((err: HttpErrorResponse) => {
                    this.error(err);
                }).finally(() => {
                    this.inProgress--;
                });

                this.inProgress++;

                this.service.getHierarchiesForGeoObject(this.code, this.type.code, false).then((hierarchies: HierarchyOverTime[]) => {
                    this.hierarchies = hierarchies;
                }).catch((err: HttpErrorResponse) => {
                    this.error(err);
                }).finally(() => {
                    this.inProgress--;
                });
            } else {
                this.isNew = true;

                this.inProgress++;

                this.service.newGeoObjectOverTime(this.type.code, false).then(retJson => {
                    this.preGeoObject = new GeoObjectOverTime(this.type, retJson.geoObject.attributes);
                    this.postGeoObject = new GeoObjectOverTime(this.type, JSON.parse(JSON.stringify(this.preGeoObject)).attributes);

                    this.hierarchies = retJson.hierarchies;
                    this.setEditMode(true);
                }).finally(() => {
                    this.inProgress--;
                });
            }
        }
    }

    editorForDateChange($event) {
        this.forDateChange.emit($event);
    }

    onCancelInternal(): void {
        this.panelCancel.emit();

        // if (this.code === '__NEW__') {
        //    this.updateCode(null);
        // }
        // else {
        //    this.updateCode(this.code);
        // }
    }

    canSubmit(): boolean {
        return this.isValid() &&
            (this.isMaintainer || (this.reason && this.reason.trim().length > 0)) &&
            (this.isNew || (this.attributeEditor && this.attributeEditor.getChangeRequestEditor().hasChanges()));
    }

    onSubmit(): void {
        if (this.isNew) {
            const action: CreateGeoObjectAction = this.attributeEditor.getActions()[0];

            this.inProgress++;

            this.service.applyGeoObjectCreate(action.parentJson, action.geoObjectJson, this.isNew, this.datasetId, this.reason, false).then((applyInfo: any) => {
                if (!applyInfo.isChangeRequest) {
                    this.featureChange.emit(this.postGeoObject);
                }
                this.panelSubmit.emit(applyInfo);
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            }).finally(() => {
                this.inProgress--;
            });

            // this.service.applyGeoObjectCreate(this.hierarchies, this.postGeoObject, this.isNew, this.datasetId, this.reason).then((applyInfo: any) => {
            //     if (!applyInfo.isChangeRequest) {
            //         this.featureChange.emit(this.postGeoObject);
            //     }
            //     this.panelSubmit.emit(applyInfo);
            // }).catch((err: HttpErrorResponse) => {
            //     this.error(err);
            // });
        } else {
            this.inProgress++;

            this.service.applyGeoObjectEdit(this.postGeoObject.attributes.code, this.type.code, this.attributeEditor.getActions(), this.datasetId, this.reason, false).then((applyInfo: any) => {
                if (!applyInfo.isChangeRequest) {
                    this.featureChange.emit(this.postGeoObject);
                }
                this.panelSubmit.emit(applyInfo);
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            }).finally(() => {
                this.inProgress--;
            });
        }

        this.geometryService.stopEditing();
    }

    onManageAttributeVersion(attribute: AttributeType): void {
        this.attribute = attribute;
        this.mode = this.MODE.VERSIONS;
    }

    onManageHiearchyVersion(hierarchy: HierarchyOverTime): void {
        this.hierarchy = hierarchy;
        this.mode = this.MODE.HIERARCHY;
    }

    onEditAttributes(): void {
        this.setEditMode(!this.isEdit);

        window.document.getElementById("navigator-left-sidebar").scroll({
            top: 0,
            behavior: "smooth"
        });
    }

    setEditMode(value: boolean): void {
        this.isEdit = value;
        this.reason = null;

        this.modeChange.emit(this.isEdit);
    }

    public error(err: HttpErrorResponse): void {
        this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

}
