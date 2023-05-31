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

import {
    Component,
    OnInit,
    Input,
    Output,
    ChangeDetectorRef,
    EventEmitter,
    ViewChildren,
    QueryList,
    ElementRef,
    SimpleChanges,
    OnDestroy
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
import { ExternalId, LocalizedValue } from "@core/model/core";
import { GovernanceStatus } from "@registry/model/constants";
import { AuthService } from "@shared/service/auth.service";

import { DateFieldComponent, ErrorHandler } from "@shared/component";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import { RegistryService, GeometryService, OLD_LAYER_COLOR } from "@registry/service";
import { ChangeRequestService } from "@registry/service/change-request.service";
import { DateService } from "@shared/service/date.service";

import { LocalizationService } from "@shared/service/localization.service";

import { VersionDiffView } from "./manage-versions-model";
import { ControlContainer, NgForm } from "@angular/forms";
import { Observable, Observer, Subscription } from "rxjs";
import { ValueOverTimeCREditor } from "./ValueOverTimeCREditor";
import { TypeaheadMatch } from "ngx-bootstrap/typeahead";
import { HierarchyCREditor } from "./HierarchyCREditor";
import { ChangeRequestEditor } from "./change-request-editor";
import { ChangeRequestChangeOverTimeAttributeEditor } from "./change-request-change-over-time-attribute-editor";
import * as ColorGen from "color-generator";
import { CHANGE_REQUEST_SOURCE_TYPE_NEW, CHANGE_REQUEST_SOURCE_TYPE_OLD, GeoJsonLayer, GeoObjectLayerDataSource, Layer, ValueOverTimeDataSource } from "@registry/service/layer-data-source";
import { LocationManagerState } from "../location-manager/location-manager.component";

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
export class ManageVersionsComponent implements OnInit, OnDestroy {

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

    filterDate: string = null;

    @Input() showAllInstances: boolean = true;

    viewModels: VersionDiffView[] = [];

    isRootOfHierarchy: boolean = false;

    isInitialized: boolean = false;

    layerChangeSub: Subscription;

    stateSub: Subscription;

    // eslint-disable-next-line no-useless-constructor
    constructor(public geomService: GeometryService, public cdr: ChangeDetectorRef, public service: RegistryService, public lService: LocalizationService,
        public changeDetectorRef: ChangeDetectorRef, public dateService: DateService, private authService: AuthService,
        private requestService: ChangeRequestService, private modalService: BsModalService, private elementRef: ElementRef) { }

    ngOnInit(): void {
        this.changeRequestAttributeEditor = this.changeRequestEditor.getEditorForAttribute(this.attributeType, this.hierarchy) as ChangeRequestChangeOverTimeAttributeEditor;

        this.calculateViewModels();
        this.isRootOfHierarchy = this.attributeType.type === "_PARENT_" && (this.hierarchy == null || this.hierarchy.types == null || this.hierarchy.types.length === 0);
        this.isInitialized = true;

        this.layerChangeSub = this.geomService.layersChange.subscribe(layers => {
            if (this.viewModels) {
                this.viewModels.forEach(view => {
                    this.syncLayerReferences(layers, view);
                });
            }
        });
        this.stateSub = this.geomService.stateChange$.subscribe(state => this.stateChange(state));
        this.stateChange(this.geomService.getState());
    }

    ngAfterViewInit() {
        if (this.isNew && this.attributeType.code === "exists" && this.viewModels.length === 0) {
            this.onAddNewVersion();
        }
    }

    ngOnChanges(changes: SimpleChanges) {
        if (this.isInitialized && changes.showAllInstances && changes.showAllInstances.previousValue !== changes.showAllInstances.currentValue) {
            this.calculateViewModels();
        }
        if (changes.readonly && changes.readonly.previousValue !== changes.readonly.currentValue) {
            this.viewModels.forEach(vm => {
                vm.destroy(this);
                if (vm.objectLayer != null) {
                    this.geomService.removeLayer(vm.objectLayer.getId());
                    vm.objectLayer = null;
                }
            });
        }
    }

    ngOnDestroy(): void {
        this.viewModels.forEach(vm => vm.destroy(this));
        this.layerChangeSub.unsubscribe();
        this.stateSub.unsubscribe();
    }

    stateChange(state: LocationManagerState): void {
        if (this.filterDate !== state.date) {
            this.filterDate = state.date;
            this.calculateViewModels();
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

    onAddNewVersion(original?: ValueOverTimeCREditor): void {
        let editor = this.changeRequestAttributeEditor.createNewVersion(original);

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

        this.viewModels.forEach(viewModel => viewModel.destroy(this));

        let editors = this.changeRequestAttributeEditor.getEditors(this.showAllInstances);

        let layers: Layer[];
        if (this.attributeType.code === "geometry") {
            layers = this.geomService.getLayers();
        }

        editors.forEach((editor: ValueOverTimeCREditor) => {
            if (this.filterDate == null || this.dateService.between(this.filterDate, editor.startDate, editor.endDate)) {
                let view = new VersionDiffView(this, editor);
                viewModels.push(view);

                if (this.attributeType.code === "geometry") {
                    this.syncLayerReferences(layers, view);
                }
            }
        });

        this.viewModels = viewModels;
    }

    /*
     * It is possible that somebody else may have already created a particular layer for this view. We want to find it, and make the view
     *   aware that a layer already exists.
     */
    syncLayerReferences(layers: Layer[], view: VersionDiffView) {
        // Object layer
        let indexOL = layers.findIndex(layer => layer.dataSource instanceof GeoObjectLayerDataSource && (layer.dataSource as GeoObjectLayerDataSource).getCode() === this.changeRequestEditor.geoObject.attributes["code"] && (layer.dataSource as GeoObjectLayerDataSource).getTypeCode() === this.changeRequestEditor.geoObjectType.code && this.dateService.between((layer.dataSource as GeoObjectLayerDataSource).getDate(), view.editor.startDate, view.editor.endDate));
        if (indexOL !== -1) {
            view.objectLayer = layers[indexOL];
        } else {
            delete view.objectLayer;
        }

        // Editing Layer
        let keyEL = new ValueOverTimeDataSource("NEW", view.editor).getKey();
        let indexEL = layers.findIndex(l => l.dataSource.getKey() === keyEL);
        if (indexEL !== -1) {
            view.editingLayer = layers[indexEL] as GeoJsonLayer;
        } else {
            delete view.editingLayer;
        }

        // Old Layer
        let keyOLD = new ValueOverTimeDataSource("OLD", view.editor).getKey();
        let indexOLD = layers.findIndex(l => l.dataSource.getKey() === keyOLD);
        if (indexOLD !== -1) {
            view.oldLayer = layers[indexOLD] as GeoJsonLayer;
        } else {
            delete view.oldLayer;
        }
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

    getTypeAheadObservable(editor: ValueOverTimeCREditor, startDate: string, endDate: string, type: any, entry: any, index: number): Observable<any> {
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

        return new Observable((observer: Observer<Object>) => {
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

            this.service.getGeoObjectSuggestions(entry.parents[type.code].text, geoObjectTypeCode, parentCode, parentTypeCode, hierarchyCode, startDate, endDate).then(results => {
                observer.next(results);
            });
        });
    }

    typeaheadOnSelect(editor: ValueOverTimeCREditor, e: TypeaheadMatch, type: any, entry: any, date: string): void {
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

        this.service.getParentGeoObjects(e.item.code, type.code, parentTypes, true, date).then(ancestors => {
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

            if (editor instanceof HierarchyCREditor) {
                editor.setParentValue(type, entry.parents);
            }
        });
    }

    /**
     * GEOMETRY EDITING
     */

    toggleGeometryEditing(view: VersionDiffView) {
        this.geomService.setEditing(!view.editingLayer.editing, view.editingLayer);

        if (this.geoObjectType.geometryType === "POINT" || this.geoObjectType.geometryType === "MULTIPOINT") {
            view.coordinate = {};
        }
    }

    toggleGeometryView(view: VersionDiffView) {
        // Using setTimeout() to pull the calc out of the animation stack so the dom can finish mutating before getting final height.
        setTimeout(() => {
            this.mapRowHeight = this.elementRef.nativeElement.children[0].getElementsByClassName("attribute-element-wrapper")[0].offsetHeight;
        }, 0);

        if (this.readonly && !this.isGeometryInlined) {
            if (view.objectLayer) {
                this.geomService.removeLayer(view.objectLayer.getId());
                delete view.objectLayer;
            } else {
                let geoObject = this.changeRequestEditor.geoObject;
                let displayLabel = (geoObject.attributes["displayLabel"].values && geoObject.attributes["displayLabel"].values.length > 0) ? geoObject.attributes["displayLabel"].values[0].value.localizedValue : geoObject.attributes.code;
                let typeLabel = this.changeRequestEditor.geoObjectType.label.localizedValue;
                let label = displayLabel + " " + this.dateService.formatDateForDisplay(view.editor.startDate) + " (" + typeLabel + ")";

                let dataSource = new GeoObjectLayerDataSource(this.service, geoObject.attributes["code"], this.changeRequestEditor.geoObjectType.code, view.editor.startDate);
                view.objectLayer = dataSource.createLayer(label, true, ColorGen().hexString());

                this.geomService.addOrUpdateLayer(view.objectLayer, 0);
            }
        } else {
            if (view.editingLayer) {
                if (view.editingLayer.editing) {
                    this.geomService.stopEditing();
                }
                this.geomService.removeLayer(view.editingLayer.getId());
                this.geomService.unregisterDataSource(CHANGE_REQUEST_SOURCE_TYPE_NEW);
                delete view.editingLayer;
            } else {
                let geoObject = this.changeRequestEditor.geoObject;
                let displayLabel = (geoObject.attributes["displayLabel"].values && geoObject.attributes["displayLabel"].values.length > 0) ? geoObject.attributes["displayLabel"].values[0].value.localizedValue : geoObject.attributes.code;
                let typeLabel = this.changeRequestEditor.geoObjectType.label.localizedValue;
                let label = displayLabel + "* " + this.dateService.formatDateForDisplay(view.editor.startDate) + " (" + typeLabel + ")";

                let dataSource = new ValueOverTimeDataSource("NEW", view.editor);
                view.editingLayer = dataSource.createLayer(label, true, ColorGen().hexString()) as GeoJsonLayer;
                this.geomService.registerDataSource(dataSource);

                this.geomService.addOrUpdateLayer(view.editingLayer, 0);
            }
        }
    }

    toggleOldGeometryView(view: VersionDiffView) {
        if (view.oldLayer != null) {
            this.geomService.removeLayer(view.oldLayer.getId());
            this.geomService.unregisterDataSource(CHANGE_REQUEST_SOURCE_TYPE_OLD);
            delete view.oldLayer;
        } else {
            let geoObject = this.changeRequestEditor.geoObject;
            let displayLabel = (geoObject.attributes["displayLabel"].values && geoObject.attributes["displayLabel"].values.length > 0) ? geoObject.attributes["displayLabel"].values[0].value.localizedValue : geoObject.attributes.code;
            let typeLabel = this.changeRequestEditor.geoObjectType.label.localizedValue;
            let label = displayLabel + " " + this.dateService.formatDateForDisplay(view.editor.startDate) + " (" + typeLabel + ")";

            let dataSource = new ValueOverTimeDataSource("OLD", view.editor);
            view.oldLayer = dataSource.createLayer(label, true, OLD_LAYER_COLOR) as GeoJsonLayer;
            this.geomService.registerDataSource(dataSource);

            this.geomService.addOrUpdateLayer(view.oldLayer, 1);
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
