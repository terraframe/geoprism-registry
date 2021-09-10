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
import { GeoObjectType, AttributeType, ValueOverTime, GeoObjectOverTime, AttributeTermType, HierarchyOverTime, HierarchyOverTimeEntry } from "@registry/model/registry";
import { CreateGeoObjectAction, UpdateAttributeOverTimeAction, AbstractAction, ValueOverTimeDiff, ChangeRequest, SummaryKey } from "@registry/model/crtable";
import { LocalizedValue } from "@shared/model/core";
import { ConflictType, ActionTypes, GovernanceStatus, LayerColor } from "@registry/model/constants";
import { AuthService } from "@shared/service/auth.service";
import { v4 as uuid } from "uuid";

import { DateFieldComponent } from "../../../shared/component/form-fields/date-field/date-field.component";
import { ErrorHandler } from "@shared/component";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import { RegistryService, GeometryService } from "@registry/service";
import { ChangeRequestService } from "@registry/service/change-request.service";
import { DateService } from "@shared/service/date.service";

import { LocalizationService } from "@shared/service";

import Utils from "../../utility/Utils";

import { VersionDiffView, Layer } from "./manage-versions-model";
import { HierarchyEditPropagator } from "./HierarchyEditPropagator";
import { ControlContainer, NgForm } from "@angular/forms";
import { GeoObjectSharedAttributeEditorComponent } from "./geoobject-shared-attribute-editor.component";

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

    @Input() sharedAttributeEditor: GeoObjectSharedAttributeEditorComponent;

    attributeType: AttributeType;
    actions: AbstractAction[] = [];

    // eslint-disable-next-line accessor-pairs
    @Input() set attributeData(value: {"attributeType":AttributeType, "changeRequest":ChangeRequest, "actions":AbstractAction[], "editor": any, geoObject:GeoObjectOverTime}) {
        this.sharedAttributeEditor = value.editor;
        this.attributeType = value.attributeType;

        this.changeRequest = value.changeRequest;

        if (this.changeRequest != null) {
            this.actions = value.changeRequest.actions;
        } else {
            this.actions = value.actions;
        }

        this.postGeoObject = value.geoObject;
    }

    @Input() geoObjectType: GeoObjectType;

    postGeoObject: GeoObjectOverTime;

    @Input() hierarchy: HierarchyOverTime = null;

    changeRequest: ChangeRequest;

    viewModels: VersionDiffView[] = [];

    // The 'current' action which is to be used whenever we're applying new edits.
    editAction: AbstractAction;

    isRootOfHierarchy: boolean = false;

    // eslint-disable-next-line no-useless-constructor
    constructor(public geomService: GeometryService, public cdr: ChangeDetectorRef, public service: RegistryService, public lService: LocalizationService,
        public changeDetectorRef: ChangeDetectorRef, public dateService: DateService, private authService: AuthService,
        private requestService: ChangeRequestService, private modalService: BsModalService, private elementRef: ElementRef) { }

    ngOnInit(): void {
        this.calculateViewModels();
        this.isRootOfHierarchy = this.attributeType.type === "_PARENT_" && (this.hierarchy == null || this.hierarchy.types == null || this.hierarchy.types.length === 0);
    }

    ngAfterViewInit() {
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
        return viewModel.oldValue != null && this.getValueAtLocale(viewModel.oldValue, locale) !== this.getValueAtLocale(viewModel.value, locale);
    }

    onDateChange(): any {
        setTimeout(() => {
            let geoObjectAttributeExcludes: string[] = ["uid", "sequence", "type", "lastUpdateDate", "createDate", "invalid", "exists"];

            this.isValid = this.checkDateFieldValidity();

            let hasConflict = this.dateService.checkRanges(this.attributeType, this.viewModels);

            let existViews = this.getViewsForAttribute("exists", null, true);
            let hasExistConflict = false;
            if (this.attributeType.code !== "exists") {
                hasExistConflict = this.dateService.checkExistRanges(this.viewModels, existViews);
            } else {
                let attrs = this.geoObjectType.attributes.slice(); // intentionally a shallow copy

                attrs.push(this.sharedAttributeEditor.geometryAttributeType);
                attrs.push(this.sharedAttributeEditor.parentAttributeType);

                attrs.forEach((attr: AttributeType) => {
                    if (geoObjectAttributeExcludes.indexOf(attr.code) === -1 && attr.isChangeOverTime) {
                        if (attr.code !== "_PARENT_") {
                            let attrViews = this.getViewsForAttribute(attr.code, null);

                            if (!attr.isValidReason) {
                                attr.isValidReason = { timeConflict: false, existConflict: false, dateField: false };
                            }
                            attr.isValidReason.existConflict = this.dateService.checkExistRanges(attrViews, existViews);

                            if (attr.isValidReason.existConflict) {
                                attr.isValid = false;
                            } else {
                                attr.isValidReason.timeConflict = this.dateService.checkRanges(attr, attrViews);

                                attr.isValid = !(attr.isValidReason.dateField || attr.isValidReason.timeConflict || attr.isValidReason.existConflict);
                            }
                        } else {
                            this.sharedAttributeEditor.hierarchies.forEach(hierarchy => {
                                let attrViews = this.getViewsForAttribute(attr.code, hierarchy);

                                if (!attr.isValidReasonHierarchy) {
                                    attr.isValidReasonHierarchy = {};
                                    attr.isValidReasonHierarchy[hierarchy.code] = { timeConflict: false, existConflict: false, dateField: false };
                                } else if (!attr.isValidReasonHierarchy[hierarchy.code]) {
                                    attr.isValidReasonHierarchy[hierarchy.code] = { timeConflict: false, existConflict: false, dateField: false };
                                }
                                attr.isValidReasonHierarchy[hierarchy.code].existConflict = this.dateService.checkExistRanges(attrViews, existViews);

                                if (attr.isValidReasonHierarchy[hierarchy.code].existConflict) {
                                    attr.isValid = false;
                                } else {
                                    attr.isValidReasonHierarchy[hierarchy.code].timeConflict = this.dateService.checkRanges(attr, attrViews);

                                    attr.isValid = true;
                                    this.sharedAttributeEditor.hierarchies.forEach(hierarchy2 => {
                                        if (!attr.isValidReasonHierarchy[hierarchy2.code]) {
                                            attr.isValidReasonHierarchy[hierarchy2.code] = { timeConflict: false, existConflict: false, dateField: false };
                                        }

                                        let hierValid = !(attr.isValidReasonHierarchy[hierarchy2.code].dateField || attr.isValidReasonHierarchy[hierarchy2.code].timeConflict || attr.isValidReasonHierarchy[hierarchy2.code].existConflict);

                                        if (!hierValid) {
                                            attr.isValid = false;
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            }

            let valid = this.isValid && !(hasConflict || hasExistConflict);
            this.attributeType.isValid = valid;
            this.attributeType.isValidReason = { timeConflict: hasConflict, existConflict: hasExistConflict, dateField: !this.isValid };
            this.isValidChange.emit(valid);
        }, 0);
    }

    public stringify(obj: any): string {
        return JSON.stringify(obj);
    }

    remove(view: VersionDiffView): void {
        if (this.geomService.isEditing()) {
            this.geomService.stopEditing();
        }

        view.editPropagator.remove();

        if (view.summaryKey === SummaryKey.NEW || view.editPropagator.action.actionType === ActionTypes.CREATEGEOOBJECTACTION) {
            const index = this.viewModels.findIndex(v => v.oid === view.oid);

            if (index > -1) {
                this.viewModels.splice(index, 1);
            }
        }

        this.onDateChange();

        if (this.attributeType.type === "geometry") {
            this.geomService.reload();
        }
    }

    onAddNewVersion(): void {
        let view: VersionDiffView = new VersionDiffView(this, this.editAction);
        view.oid = uuid();

        view.editPropagator.onAddNewVersion();

        this.viewModels.push(view);

        this.changeDetectorRef.detectChanges();
    }

    getVersionData(attribute: AttributeType) {
        let versions: ValueOverTime[] = [];

        this.postGeoObject.attributes[attribute.code].values.forEach(vAttribute => {
            vAttribute.value.localeValues.forEach(val => {
                versions.push(val);
            });
        });

        return versions;
    }

    getValueAtLocale(lv: LocalizedValue, locale: string) {
        return new LocalizedValue(lv.localizedValue, lv.localeValues).getValue(locale);
    }

    getDefaultLocaleVal(locale: any): string {
        let defVal = null;

        locale.localeValues.forEach(locVal => {
            if (locVal.locale === "defaultLocale") {
                defVal = locVal.value;
            }
        });

        return defVal;
    }

    getGeoObjectTypeTermAttributeOptions(termAttributeCode: string) {
        for (let i = 0; i < this.geoObjectType.attributes.length; i++) {
            let attr: any = this.geoObjectType.attributes[i];

            if (attr.type === "term" && attr.code === termAttributeCode) {
                attr = <AttributeTermType>attr;
                let attrOpts = attr.rootTerm.children;

                // only remove status of the required status type
                if (attrOpts.length > 0) {
                    if (attr.code === "status") {
                        return Utils.removeStatuses(attrOpts);
                    } else {
                        return attrOpts;
                    }
                }
            }
        }

        return null;
    }

    findViewByOid(oid: string, viewModels: VersionDiffView[]): VersionDiffView {
        if (!viewModels) {
            viewModels = this.viewModels;
        }

        let len = viewModels.length;
        for (let i = 0; i < len; ++i) {
            let view = viewModels[i];

            if (view.oid === oid) {
                return view;
            }
        }

        return null;
    }

    findPostGeoObjectVOT(oid: string) {
        this.postGeoObject.attributes[this.attributeType.code].values.forEach((vot: ValueOverTime) => {
            if (vot.oid === oid) {
                return vot;
            }
        });

        return null;
    }

    /**
     * Our goal here is to loop over the action diffs and then calculate what to display to the end user.
     */
    calculateViewModels(): void {
        if (this.isNew) {
            if (this.actions.length > 0 && this.actions[0].actionType === ActionTypes.CREATEGEOOBJECTACTION) {
                this.editAction = this.actions[0];
                const action = this.editAction as CreateGeoObjectAction;

                if (action.geoObjectJson == null) {
                    action.geoObjectJson = this.postGeoObject;
                }
            }
        } else {
            this.actions.forEach((action: AbstractAction) => {
                if (action.actionType === ActionTypes.UPDATEATTRIBUTETACTION) {
                    let updateAttrAction: UpdateAttributeOverTimeAction = action as UpdateAttributeOverTimeAction;

                    if (this.attributeType.code === updateAttrAction.attributeName && (this.attributeType.type !== "_PARENT_" || updateAttrAction.attributeDiff.hierarchyCode === this.hierarchy.code)) {
                        this.editAction = action;
                    }
                }
            });

            if (this.editAction == null) {
                this.editAction = new UpdateAttributeOverTimeAction(this.attributeType.code);
            }
        }

        this.viewModels = this.getViewsForAttribute(this.attributeType.code, this.hierarchy);
    }

    getViewsForAttribute(typeCode: string, hierarchy: HierarchyOverTime, includeUnmodified: boolean = false): VersionDiffView[] {
        let viewModels: VersionDiffView[] = [];

        // First, we have to create a view for every ValueOverTime object. This is done to simply display what's currently
        // on the GeoObject
        if (includeUnmodified || this.changeRequest == null || this.changeRequest.type === "CreateGeoObject" ||
          (this.changeRequest.approvalStatus !== "ACCEPTED" && this.changeRequest.approvalStatus !== "PARTIAL" && this.changeRequest.approvalStatus !== "REJECTED")) {
            if (typeCode === "_PARENT_") {
                hierarchy.entries.forEach((entry: HierarchyOverTimeEntry) => {
                    let view = new VersionDiffView(this, this.editAction);

                    view.oid = entry.oid;
                    view.summaryKey = SummaryKey.UNMODIFIED;
                    view.startDate = entry.startDate;
                    view.endDate = entry.endDate;
                    view.value = JSON.parse(JSON.stringify(entry));
                    view.value.loading = {};

                    view.editPropagator = new HierarchyEditPropagator(this, this.editAction, view, entry, hierarchy);

                    // In the corner case where this object isn't assigned to the lowest level, we may have
                    // empty values in our parents array for some of the types. Our front-end assumes there
                    // will always be an entry for all the types.
                    let len = hierarchy.types.length;
                    for (let i = 0; i < len; ++i) {
                        let type = hierarchy.types[i];

                        if (!Object.prototype.hasOwnProperty.call(view.value.parents, type.code)) {
                            view.value.parents[type.code] = { text: "", geoObject: null };
                        }
                    }

                    viewModels.push(view);
                });
            } else {
                if (this.postGeoObject.attributes[typeCode]) {
                    this.postGeoObject.attributes[typeCode].values.forEach((vot: ValueOverTime) => {
                        let view = new VersionDiffView(this, this.editAction);

                        view.oid = vot.oid;
                        view.summaryKey = SummaryKey.UNMODIFIED;
                        view.startDate = vot.startDate;
                        view.endDate = vot.endDate;
                        view.value = vot.value == null ? null : JSON.parse(JSON.stringify(vot.value));

                        view.editPropagator.valueOverTime = vot;

                        viewModels.push(view);
                    });
                }
            }
        }

        // Next, we must apply all changes which may exist in the actions.
        let len = this.actions.length;
        for (let i = 0; i < len; ++i) {
            let action: AbstractAction = this.actions[i];

            if (action.actionType === ActionTypes.UPDATEATTRIBUTETACTION) {
                let updateAttrAction: UpdateAttributeOverTimeAction = action as UpdateAttributeOverTimeAction;

                if (typeCode === updateAttrAction.attributeName) {
                    if (typeCode === "_PARENT_" && updateAttrAction.attributeDiff.hierarchyCode !== hierarchy.code) {
                        continue;
                    }

                    updateAttrAction.attributeDiff.valuesOverTime.forEach((votDiff: ValueOverTimeDiff) => {
                        let view = this.findViewByOid(votDiff.oid, viewModels);

                        if (votDiff.action === "DELETE") {
                            if (view == null) {
                                view = new VersionDiffView(this, action);
                                viewModels.push(view);

                                if (this.changeRequest == null || (this.changeRequest.approvalStatus !== "ACCEPTED" && this.changeRequest.approvalStatus !== "PARTIAL" && this.changeRequest.approvalStatus !== "REJECTED")) {
                                    view.conflictMessage = [{ severity: "ERROR", message: this.lService.decode("changeovertime.manageVersions.missingReference"), type: ConflictType.MISSING_REFERENCE }];
                                }
                            }

                            this.populateViewFromDiff(typeCode, hierarchy, view, votDiff);

                            delete view.oldValue;
                            delete view.oldStartDate;
                            delete view.oldEndDate;

                            // view.startDate = votDiff.oldStartDate;
                            // view.endDate = votDiff.oldEndDate;
                            // view.oid = votDiff.oid;
                            // view.value = votDiff.oldValue;

                            view.summaryKey = SummaryKey.DELETE;

                            view.editPropagator.diff = votDiff;
                        } else if (votDiff.action === "UPDATE") {
                            if (view == null) {
                                view = new VersionDiffView(this, action);
                                viewModels.push(view);

                                if (this.changeRequest == null || (this.changeRequest.approvalStatus !== "ACCEPTED" && this.changeRequest.approvalStatus !== "PARTIAL" && this.changeRequest.approvalStatus !== "REJECTED")) {
                                    view.conflictMessage = [{ severity: "ERROR", message: this.lService.decode("changeovertime.manageVersions.missingReference"), type: ConflictType.MISSING_REFERENCE }];
                                }
                            }

                            this.populateViewFromDiff(typeCode, hierarchy, view, votDiff);

                            view.calculateSummaryKey(votDiff);
                        } else if (votDiff.action === "CREATE") {
                            if (view != null) {
                                console.log("This action doesn't make sense. We're trying to create something that already exists?", votDiff);
                            } else {
                                view = new VersionDiffView(this, action);

                                this.populateViewFromDiff(typeCode, hierarchy, view, votDiff);

                                view.summaryKey = SummaryKey.NEW;

                                viewModels.push(view);
                            }
                        }
                    });
                }
            } else if (action.actionType === ActionTypes.CREATEGEOOBJECTACTION) {
              // Nothing to do here. Create actions don't have diffs.
            } else {
                console.log("Unexpected action : " + action.actionType, action);
            }
        }

        return viewModels;
    }

    populateViewFromDiff(typeCode: string, hierarchy: HierarchyOverTime, view: VersionDiffView, votDiff: ValueOverTimeDiff) {
        if (typeCode === "_PARENT_") {
            view.value = (view.editPropagator as HierarchyEditPropagator).createEmptyHierarchyEntry();
            view.value.oid = votDiff.oid;
            view.value.startDate = votDiff.newStartDate || votDiff.oldStartDate;
            view.value.endDate = votDiff.newEndDate || votDiff.oldEndDate;

            view.value.parents = votDiff.parents;

            if (!view.value.parents) {
                view.value.parents = {};
            }

            // In the corner case where this object isn't assigned to the lowest level, we may have
            // empty values in our parents array for some of the types. Our front-end assumes there
            // will always be an entry for all the types.
            let len = hierarchy.types.length;
            for (let i = 0; i < len; ++i) {
                let type = hierarchy.types[i];

                if (!Object.prototype.hasOwnProperty.call(view.value.parents, type.code)) {
                    view.value.parents[type.code] = { text: "", geoObject: null };
                }
            }

            view.value.loading = {};

            if (votDiff.oldValue != null) {
                let oldCodeArray: string[] = votDiff.oldValue.split("_~VST~_");
                // let oldTypeCode: string = oldCodeArray[0];
                let oldGoCode: string = oldCodeArray[1];

                view.oldValue = oldGoCode;

                let len = hierarchy.types.length;
                for (let i = len - 1; i >= 0; --i) {
                    let type = hierarchy.types[i];

                    if (votDiff.parents && Object.prototype.hasOwnProperty.call(votDiff.parents, type.code)) {
                        let lowestLevel = votDiff.parents[type.code];

                        if (lowestLevel.text == null || lowestLevel.text.length === 0) {
                            lowestLevel.text = oldGoCode;
                            break;
                        }
                    }
                }
            }
        } else {
            if (votDiff.newValue != null) {
                view.value = JSON.parse(JSON.stringify(votDiff.newValue));
                view.oldValue = votDiff.oldValue == null ? null : JSON.parse(JSON.stringify(votDiff.oldValue));
            } else {
                view.value = votDiff.oldValue == null ? null : JSON.parse(JSON.stringify(votDiff.oldValue));
            }
        }

        view.oid = votDiff.oid;
        view.startDate = votDiff.newStartDate || votDiff.oldStartDate;
        view.endDate = votDiff.newEndDate || votDiff.oldEndDate;
        if (votDiff.newStartDate !== votDiff.oldStartDate) {
            view.oldStartDate = votDiff.newStartDate == null ? null : votDiff.oldStartDate;
        }
        if (votDiff.newEndDate !== votDiff.oldEndDate) {
            view.oldEndDate = votDiff.newEndDate == null ? null : votDiff.oldEndDate;
        }
        view.editPropagator.diff = votDiff;
    }

    isStatusChanged(post, pre) {
        if ((pre != null && post == null) || (post != null && pre == null)) {
            return true;
        } else if (pre == null && post == null) {
            return false;
        }

        if ((pre.length === 0 && post.length > 0) || (post.length === 0 && pre.length > 0)) {
            return true;
        }

        let preCompare = pre;
        if (Array.isArray(pre)) {
            preCompare = pre[0];
        }

        let postCompare = post;
        if (Array.isArray(post)) {
            postCompare = post[0];
        }

        return preCompare !== postCompare;
    }

    hasAttributeChanges(): boolean {
        if (this.attributeType.isChangeOverTime) {
            for (let i = 0; i < this.actions.length; i++) {
                let action = this.actions[i];

                if (action.actionType === ActionTypes.UPDATEATTRIBUTETACTION) {
                    let uAction = action as UpdateAttributeOverTimeAction;

                    if (uAction.attributeName === this.attributeType.code) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    onApprove(): void {
        this.requestService.setActionStatus(this.editAction.oid, GovernanceStatus.ACCEPTED).then(results => {
            this.editAction.approvalStatus = GovernanceStatus.ACCEPTED;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onReject(): void {
        this.requestService.setActionStatus(this.editAction.oid, GovernanceStatus.REJECTED).then(results => {
            this.editAction.approvalStatus = GovernanceStatus.REJECTED;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onPending(): void {
        this.requestService.setActionStatus(this.editAction.oid, GovernanceStatus.PENDING).then(results => {
            this.editAction.approvalStatus = GovernanceStatus.PENDING;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    public error(err: any): void {
        this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
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
            view.newLayer.oid = "NEW_" + view.oid;
            view.newLayer.isEditing = false;
            view.newLayer.isRendering = false;
            view.newLayer.zindex = 1;
            view.newLayer.color = LayerColor.NEW;
            view.newLayer.geojson = view.value;
            view.newLayer.editPropagator = view.editPropagator;

            return view.newLayer;
        } else {
            if (view.oldLayer != null) {
                return view.oldLayer;
            }

            view.oldLayer = new Layer();
            view.oldLayer.oid = "OLD_" + view.oid;
            view.oldLayer.isEditing = false;
            view.oldLayer.isRendering = false;
            view.oldLayer.zindex = 0;
            view.oldLayer.color = LayerColor.OLD;
            view.oldLayer.geojson = view.oldValue;
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
