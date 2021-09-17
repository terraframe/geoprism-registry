import {
    Component,
    OnInit,
    Input,
    Output,
    ChangeDetectorRef,
    EventEmitter,
    ViewChildren,
    QueryList,
    ElementRef
} from "@angular/core";
import {
    trigger,
    style,
    animate,
    transition
} from "@angular/animations";
import { HttpErrorResponse } from "@angular/common/http";
import { GeoObjectType, AttributeType, HierarchyOverTime } from "@registry/model/registry";
import { SummaryKey } from "@registry/model/crtable";
import { LocalizedValue } from "@shared/model/core";
import { GovernanceStatus, LayerColor } from "@registry/model/constants";
import { AuthService } from "@shared/service/auth.service";

import { DateFieldComponent } from "../../../shared/component/form-fields/date-field/date-field.component";
import { ErrorHandler } from "@shared/component";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import { RegistryService, GeometryService } from "@registry/service";
import { ChangeRequestService } from "@registry/service/change-request.service";
import { DateService } from "@shared/service/date.service";

import { LocalizationService } from "@shared/service";

import { VersionDiffView, Layer } from "./manage-versions-model";
import { ControlContainer, NgForm } from "@angular/forms";
import { Observable } from "rxjs";
import { ValueOverTimeCREditor } from "./ValueOverTimeCREditor";
import { TypeaheadMatch } from "ngx-bootstrap/typeahead";
import { HierarchyCREditor } from "./HierarchyCREditor";
import { ChangeRequestEditor } from "./change-request-editor";
import { ChangeRequestChangeOverTimeAttributeEditor } from "./change-request-change-over-time-attribute-editor";

@Component({
    selector: "manage-versions",
    templateUrl: "./manage-versions.component.html",
    styleUrls: ["./manage-versions.css"],
    host: { "[@fadeInOut]": "true" },
    animations: [
        [
            trigger("fadeInOut", [
                transition("void => *", [
                    style({
                        opacity: 0
                    }),
                    animate("500ms")
                ]),
                transition(":leave",
                    animate("500ms",
                        style({
                            opacity: 0
                        })
                    )
                )
            ])
        ]],
    viewProviders: [{ provide: ControlContainer, useExisting: NgForm }]

})
export class ManageVersionsComponent implements OnInit {

    // height (as number) in pixels
    mapRowHeight: number;

    bsModalRef: BsModalRef;

    @Input() isNew: boolean = false;

    @ViewChildren("dateFieldComponents") dateFieldComponentsArray: QueryList<DateFieldComponent>;

    message: string = null;

    currentDate: Date = new Date();

    isValid: boolean = true;
    @Output() isValidChange = new EventEmitter<boolean>();

    @Input() readonly: boolean = false;

    @Input() isGeometryInlined: boolean = false;

    @Input() changeRequestEditor: ChangeRequestEditor;

    changeRequestAttributeEditor: ChangeRequestChangeOverTimeAttributeEditor;

    @Input() geoObjectType: GeoObjectType;

    @Input() attributeType: AttributeType;

    @Input() hierarchy: HierarchyOverTime = null;

    viewModels: VersionDiffView[] = [];

    isRootOfHierarchy: boolean = false;

    // eslint-disable-next-line no-useless-constructor
    constructor(public geomService: GeometryService, public cdr: ChangeDetectorRef, public service: RegistryService, public lService: LocalizationService,
        public changeDetectorRef: ChangeDetectorRef, public dateService: DateService, private authService: AuthService,
        private requestService: ChangeRequestService, private modalService: BsModalService, private elementRef: ElementRef) { }

    ngOnInit(): void {
        this.changeRequestAttributeEditor = this.changeRequestEditor.getEditorForAttribute(this.attributeType, this.hierarchy) as ChangeRequestChangeOverTimeAttributeEditor;
        this.calculateViewModels();
        this.isRootOfHierarchy = this.attributeType.type === "_PARENT_" && (this.hierarchy == null || this.hierarchy.types == null || this.hierarchy.types.length === 0);
    }

    ngAfterViewInit() {
        if (this.isNew && this.attributeType.code === "exists" && this.viewModels.length === 0) {
            this.onAddNewVersion();
        }
    }

    checkDateFieldValidity(): boolean {
        let dateFields = this.dateFieldComponentsArray.toArray();

        for (let i = 0; i < dateFields.length; i++) {
            let field = dateFields[i];
            if (!field.valid) {
                return false;
            }
        }

        return true;
    }

    hasLocalizationChanged(viewModel: VersionDiffView, locale: string): boolean {
        return viewModel.editor.oldValue != null && this.getValueAtLocale(viewModel.editor.oldValue, locale) !== this.getValueAtLocale(viewModel.editor.value, locale);
    }

    onDateChange(): any {
        setTimeout(() => {
            this.isValid = this.changeRequestAttributeEditor.validate() && this.checkDateFieldValidity();

            this.isValidChange.emit(this.isValid);
        }, 0);
    }

    remove(view: VersionDiffView): void {
        if (this.geomService.isEditing()) {
            this.geomService.stopEditing();
        }

        this.changeRequestAttributeEditor.remove(view.editor);

        if (view.summaryKey === SummaryKey.NEW || this.changeRequestEditor.changeRequest.type === "CreateGeoObject") {
            const index = this.viewModels.findIndex(v => v.editor.oid === view.editor.oid);

            if (index > -1) {
                this.viewModels.splice(index, 1);
            }
        }

        if (this.attributeType.type === "geometry") {
            this.geomService.reload();
        }
    }

    onAddNewVersion(): void {
        let editor = this.changeRequestAttributeEditor.createNewVersion();

        if (this.isNew && this.changeRequestEditor.geoObject.attributes["exists"]) {
            let values = this.changeRequestEditor.geoObject.attributes["exists"].values;

            if (values && values.length > 0) {
                let value = values[0];

                editor.startDate = value.startDate;
                editor.endDate = value.endDate;
            }
        }

        this.viewModels.push(new VersionDiffView(this, editor));
    }

    getValueAtLocale(lv: LocalizedValue, locale: string) {
        return lv == null ? null : new LocalizedValue(lv.localizedValue, lv.localeValues).getValue(locale);
    }

    public getGeoObjectTypeTermAttributeOptions(termAttributeCode: string) {
        return GeoObjectType.getGeoObjectTypeTermAttributeOptions(this.geoObjectType, termAttributeCode);
    }

    calculateViewModels(): void {
        let viewModels: VersionDiffView[] = [];

        let includeUnmodified = false;

        if (this.changeRequestEditor.changeRequest == null || this.changeRequestEditor.changeRequest.type === "CreateGeoObject" ||
          (this.changeRequestEditor.changeRequest.approvalStatus !== "ACCEPTED" && this.changeRequestEditor.changeRequest.approvalStatus !== "PARTIAL" && this.changeRequestEditor.changeRequest.approvalStatus !== "REJECTED")) {
            includeUnmodified = true;
        }

        let editors = this.changeRequestAttributeEditor.getEditors(includeUnmodified);
        editors.forEach((editor: ValueOverTimeCREditor) => {
            let view = new VersionDiffView(this, editor);
            viewModels.push(view);
        });

        this.viewModels = viewModels;
    }

    onApprove(): void {
        this.requestService.setActionStatus(this.changeRequestAttributeEditor.editAction.oid, GovernanceStatus.ACCEPTED).then(results => {
            this.changeRequestAttributeEditor.editAction.approvalStatus = GovernanceStatus.ACCEPTED;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onReject(): void {
        this.requestService.setActionStatus(this.changeRequestAttributeEditor.editAction.oid, GovernanceStatus.REJECTED).then(results => {
            this.changeRequestAttributeEditor.editAction.approvalStatus = GovernanceStatus.REJECTED;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onPending(): void {
        this.requestService.setActionStatus(this.changeRequestAttributeEditor.editAction.oid, GovernanceStatus.PENDING).then(results => {
            this.changeRequestAttributeEditor.editAction.approvalStatus = GovernanceStatus.PENDING;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    public error(err: any): void {
        this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

    /**
     * Hierarchy Editing
     */

    getTypeAheadObservable(editor: HierarchyCREditor, date: string, type: any, entry: any, index: number): Observable<any> {
        let geoObjectTypeCode = type.code;

        let parentCode = null;
        let parentTypeCode = null;
        let hierarchyCode = null;

        if (index > 0) {
            let pType = editor.changeRequestAttributeEditor.hierarchy.types[index - 1];
            const parent = entry.parents[pType.code];

            if (parent.geoObject != null && parent.geoObject.properties.code != null) {
                hierarchyCode = editor.changeRequestAttributeEditor.hierarchy.code;
                parentCode = parent.geoObject.properties.code;
                parentTypeCode = parent.geoObject.properties.type;
            }
        }

        return Observable.create((observer: any) => {
            if (parentCode == null) {
                let loopI = index;

                while (parentCode == null && loopI > 0) {
                    loopI = loopI - 1;

                    let parent = entry.parents[editor.changeRequestAttributeEditor.hierarchy.types[loopI].code];

                    if (parent != null) {
                        if (parent.geoObject != null && parent.geoObject.properties.code != null) {
                            parentCode = parent.geoObject.properties.code;
                            hierarchyCode = editor.changeRequestAttributeEditor.hierarchy.code;
                            parentTypeCode = editor.changeRequestAttributeEditor.hierarchy.types[loopI].code;
                        } else if (parent.goCode != null) {
                            parentCode = parent.goCode;
                            hierarchyCode = editor.changeRequestAttributeEditor.hierarchy.code;
                            parentTypeCode = editor.changeRequestAttributeEditor.hierarchy.types[loopI].code;
                        }
                    }
                }
            }

            this.service.getGeoObjectSuggestions(entry.parents[type.code].text, geoObjectTypeCode, parentCode, parentTypeCode, hierarchyCode, date).then(results => {
                observer.next(results);
            });
        });
    }

    typeaheadOnSelect(editor: HierarchyCREditor, e: TypeaheadMatch, type: any, entry: any, date: string): void {
      //        let ptn: ParentTreeNode = parent.ptn;

        entry.parents[type.code].text = e.item.name + " : " + e.item.code;
        entry.parents[type.code].goCode = e.item.code;

        let parentTypes = [];

        for (let i = 0; i < editor.changeRequestAttributeEditor.hierarchy.types.length; i++) {
            let current = editor.changeRequestAttributeEditor.hierarchy.types[i];

            parentTypes.push(current.code);

            if (current.code === type.code) {
                break;
            }
        }

        this.service.getParentGeoObjects(e.item.uid, type.code, parentTypes, true, date).then(ancestors => {
            // First filter the response for ancestors of only the correct hierarchy
            ancestors.parents = ancestors.parents.filter(p => p.hierarchyType === editor.changeRequestAttributeEditor.hierarchy.code);

            delete entry.parents[type.code].goCode;
            entry.parents[type.code].geoObject = ancestors.geoObject;
            entry.parents[type.code].text = ancestors.geoObject.properties.displayLabel.localizedValue + " : " + ancestors.geoObject.properties.code;

            for (let i = 0; i < editor.changeRequestAttributeEditor.hierarchy.types.length; i++) {
                let current = editor.changeRequestAttributeEditor.hierarchy.types[i];
                let ancestor = ancestors;

                while (ancestor != null && ancestor.geoObject.properties.type !== current.code) {
                    if (ancestor.parents.length > 0) {
                        ancestor = ancestor.parents[0];
                    } else {
                        ancestor = null;
                    }
                }

                if (ancestor != null) {
                    entry.parents[current.code].geoObject = ancestor.geoObject;
                    entry.parents[current.code].text = ancestor.geoObject.properties.displayLabel.localizedValue + " : " + ancestor.geoObject.properties.code;
                }
            }

            editor.setParentValue(type, entry.parents);
        });
    }

    /**
     * GEOMETRY EDITING
     */

    toggleGeometryEditing(view: VersionDiffView) {
        this.geomService.setEditing(!view.newLayer.isEditing, view.newLayer);

        if (this.geoObjectType.geometryType === "POINT" || this.geoObjectType.geometryType === "MULTIPOINT") {
            view.coordinate = {};
        }
    }

    toggleGeometryView(view: VersionDiffView) {
        // Using setTimeout() to pull the calc out of the animation stack so the dom can finish mutating before getting final height.
        setTimeout(() => {
            this.mapRowHeight = this.elementRef.nativeElement.children[0].getElementsByClassName("attribute-element-wrapper")[0].offsetHeight;
        }, 0);

        let layer: Layer = this.getOrCreateLayer(view, "NEW");

        if (layer.isEditing) {
            this.geomService.stopEditing();
        }

        this.geomService.setRendering(!layer.isRendering, layer);
    }

    toggleOldGeometryView(view: VersionDiffView) {
        let layer: Layer = this.getOrCreateLayer(view, "OLD");

        this.geomService.setRendering(!layer.isRendering, layer);
    }

    getOrCreateLayer(view: VersionDiffView, context: string): Layer {
        if (context === "NEW") {
            if (view.newLayer != null) {
                return view.newLayer;
            }

            view.newLayer = new Layer();
            view.newLayer.oid = "NEW_" + view.editor.oid;
            view.newLayer.isEditing = false;
            view.newLayer.isRendering = false;
            view.newLayer.zindex = 1;
            view.newLayer.color = LayerColor.NEW;
            view.newLayer.geojson = view.editor.value;
            view.newLayer.editPropagator = view.editor;

            return view.newLayer;
        } else {
            if (view.oldLayer != null) {
                return view.oldLayer;
            }

            view.oldLayer = new Layer();
            view.oldLayer.oid = "OLD_" + view.editor.oid;
            view.oldLayer.isEditing = false;
            view.oldLayer.isRendering = false;
            view.oldLayer.zindex = 0;
            view.oldLayer.color = LayerColor.OLD;
            view.oldLayer.geojson = view.editor.oldValue;
            view.oldLayer.editPropagator = null;

            return view.oldLayer;
        }
    }

    manualCoordinateChange(view: VersionDiffView): void {
        if (view.newCoordinateX || view.newCoordinateY) {
            let newX = view.newCoordinateX;
            if (view.value.coordinates && view.value.coordinates[0]) {
                newX = view.value.coordinates[0];
            }
            let newY = view.newCoordinateY;
            if (view.value.coordinates && view.value.coordinates[0]) {
                newY = view.value.coordinates[1];
            }
            view.value.coordinates = [[newX || 0, newY || 0]];
            delete view.newCoordinateX;
            delete view.newCoordinateY;
            return;
        }

        const isLatitude = num => isFinite(num) && Math.abs(num) <= 90;
        const isLongitude = num => isFinite(num) && Math.abs(num) <= 180;

        view.coordinate.latValid = isLatitude(view.value.coordinates[0][1]);
        view.coordinate.longValid = isLongitude(view.value.coordinates[0][0]);

        if (!view.coordinate.latValid || !view.coordinate.longValid) {
            // outside EPSG bounds
            this.isValid = false;
            this.isValidChange.emit(this.isValid);
            return;
        }

        this.geomService.setPointCoordinates(view.value.coordinates[0][1], view.value.coordinates[0][0]);
    }

}
