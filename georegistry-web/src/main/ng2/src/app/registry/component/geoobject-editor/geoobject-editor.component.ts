import { Component, OnInit, ViewChild, Input } from "@angular/core";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import { DatePipe } from "@angular/common";
import { HttpErrorResponse } from "@angular/common/http";

import { ErrorHandler } from "@shared/component";

import { RegistryService } from "@registry/service";
import { LocalizationService, AuthService } from "@shared/service";

import { GeoObjectType, GeoObjectOverTime, HierarchyOverTime, ParentTreeNode, ImportError, ValueOverTime } from "@registry/model/registry";

import { Observable } from "rxjs";
import { TypeaheadMatch } from "ngx-bootstrap/typeahead";

@Component({
    selector: "geoobject-editor",
    templateUrl: "./geoobject-editor.component.html",
    styleUrls: ["./geoobject-editor.component.css"],
    providers: [DatePipe]
})

/**
 * This component is used in the master list when editing a row. In the future it will also be used by the navigator and has
 * potential to also be used in the submit change request and manage change requests.
 */
export class GeoObjectEditorComponent implements OnInit {

    @Input() geoObjectType: GeoObjectType;

    isGeometryEditable: boolean;

    tabIndex: number = 0;

    dataSource: Observable<any>;

    masterListId: string;
    notes: string;

    isNewGeoObject: boolean = false;

    @Input() onSuccessCallback: Function;

    submitFunction: Function = null;

    isAdmin: boolean;
    isMaintainer: boolean;
    isContributor: boolean;

    /*
     * GeoObject Property Editor
     */
    @ViewChild("attributeEditor") attributeEditor;

    geoObject: GeoObjectOverTime;

    //    /*
    //     * GeoObject Geometry Editor
    //     */
    //    @ViewChild( "geometryEditor" ) geometryEditor;
    //
    //    areGeometriesValid: boolean = false;

    hierarchies: HierarchyOverTime[];

    constructor(private modalService: BsModalService, public bsModalRef: BsModalRef,
        private registryService: RegistryService, private localizeService: LocalizationService,
        authService: AuthService) {
        this.isAdmin = authService.isAdmin();
        this.isMaintainer = this.isAdmin || authService.isMaintainer();
        this.isContributor = this.isAdmin || this.isMaintainer || authService.isContributer();
    }

    ngOnInit(): void {

    }

    findVotWithStartDate(votArray: ValueOverTime[], startDate: string): any {
        for (let i: number = 0; i < votArray.length; ++i) {
            if (votArray[i].startDate === startDate) {
                return votArray[i];
            }
        }

        return null;
    }

    setMasterListId(id: string) {
        this.masterListId = id;
    }

    setOnSuccessCallback(func: Function) {
        this.onSuccessCallback = func;
    }

    // Configures the widget to be used in a "New" context, that is to say
    // that it will be used to create a new GeoObject.
    public configureAsNew(typeCode: string, dateStr: string, isGeometryEditable: boolean) {
        this.isNewGeoObject = true;
        this.isGeometryEditable = isGeometryEditable;

        this.fetchGeoObjectType(typeCode);
        this.fetchLocales();

        this.registryService.newGeoObjectOverTime(typeCode).then(retJson => {
            this.geoObject = new GeoObjectOverTime(this.geoObjectType, retJson.geoObject.attributes);
            this.hierarchies = retJson.hierarchies;
        });
    }

    // Configures the widget to be used to resolve an ImportError
    public configureFromImportError(importError: ImportError, historyId: string, dateStr: string, isGeometryEditable: boolean) {
        let typeCode = importError.object.geoObject.attributes.type;
        this.isNewGeoObject = importError.object.isNew;
        this.isGeometryEditable = isGeometryEditable;

        this.fetchGeoObjectType(typeCode);
        this.fetchLocales();

        if (importError.object != null && importError.object.parents != null && importError.object.parents.length > 0) {
            this.hierarchies = importError.object.parents;
        } else {
            this.registryService.newGeoObjectOverTime(typeCode).then(retJson => {
                this.hierarchies = retJson.hierarchies;
            });
        }

        this.geoObject = new GeoObjectOverTime(this.geoObjectType, importError.object.geoObject.attributes);

        this.submitFunction = () => {
            let config = {
                historyId: historyId,
                importErrorId: importError.id,
                resolution: "APPLY_GEO_OBJECT",
                parentTreeNode: this.hierarchies,
                geoObject: this.geoObject,
                isNew: importError.object.isNew
            };

            this.registryService.submitErrorResolve(config)
                .then(() => {
                    if (this.onSuccessCallback != null) {
                        this.onSuccessCallback();
                    }
                }).catch((err: HttpErrorResponse) => {
                    this.error(err);
                });
        };
    }

    // Configures the widget to be used in an "Edit Existing" context
    public configureAsExisting(code: string, typeCode: string, dateStr: string, isGeometryEditable: boolean): void {
        this.isNewGeoObject = false;
        this.isGeometryEditable = isGeometryEditable;

        this.fetchGeoObject(code, typeCode);
        this.fetchGeoObjectType(typeCode);
        this.fetchHierarchies(code, typeCode);
        this.fetchLocales();
    }

    private fetchGeoObject(code: string, typeCode: string) {
        this.registryService.getGeoObjectOverTime(code, typeCode).then(geoObject => {
            this.geoObject = new GeoObjectOverTime(this.geoObjectType, JSON.parse(JSON.stringify(geoObject)).attributes);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    private fetchLocales() {
        this.registryService.getLocales().then(locales => {
            this.localizeService.setLocales(locales);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    private fetchGeoObjectType(code: string) {
        this.registryService.getGeoObjectTypes([code], null)
            .then(geoObjectType => {
                this.geoObjectType = geoObjectType[0];

                if (this.geoObject != null) {
                    this.geoObject.geoObjectType = this.geoObjectType;
                }

                if (!this.geoObjectType.isGeometryEditable) {
                    //                    this.areGeometriesValid = true;
                }
            }).catch((err: HttpErrorResponse) => {
                // eslint-disable-next-line no-console
                console.log(err);
            });
    }

    private fetchHierarchies(code: string, typeTypeCode: string) {
        this.registryService.getHierarchiesForGeoObject(code, typeTypeCode)
            .then((hierarchies: any) => {
                this.hierarchies = hierarchies;
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
    }

    getTypeAheadObservable(text, typeCode) {
        return Observable.create((observer: any) => {
            this.registryService.getGeoObjectSuggestionsTypeAhead(text, typeCode).then(results => {
                observer.next(results);
            });
        });
    }

    typeaheadOnSelect(e: TypeaheadMatch, ptn: ParentTreeNode): void {
        this.registryService.getGeoObjectByCode(e.item.code, ptn.geoObject.properties.type)
            .then(geoObject => {
                ptn.geoObject = geoObject;
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
    }

    canSubmit(): boolean {
        return this.attributeEditor && this.attributeEditor.isValid() &&
            (this.isNewGeoObject || (this.attributeEditor && this.attributeEditor.getChangeRequestEditor().hasChanges()));
    }

    public error(err: HttpErrorResponse): void {
        this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

    public cancel(): void {
        this.bsModalRef.hide();
    }

    public submit(): void {
        this.bsModalRef.hide();

        if (this.submitFunction == null) {
        /*
            this.registryService.applyGeoObjectEdit(this.hierarchies, this.goSubmit, this.isNewGeoObject, this.masterListId, this.notes)
                .then(() => {

                    if (this.onSuccessCallback != null) {
                        this.onSuccessCallback();
                    }

                }).catch((err: HttpErrorResponse) => {
                    this.error(err);
                });
                */
        } else {
            this.submitFunction();
        }
    }

}
