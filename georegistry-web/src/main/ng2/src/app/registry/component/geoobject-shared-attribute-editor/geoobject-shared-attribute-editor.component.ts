import { Component, OnInit, ViewChild, Input, Output, EventEmitter, OnChanges, SimpleChanges } from "@angular/core";
import { DatePipe } from "@angular/common";
import {
    trigger,
    style,
    animate,
    transition,
    state
} from "@angular/animations";

import { LocalizedValue } from "@shared/model/core";
import { LocalizationService, AuthService } from "@shared/service";
import { DateService } from "@shared/service/date.service";

import { GeoObjectType, GeoObjectOverTime, Attribute, AttributeOverTime, AttributeTerm, Term, ValueOverTime } from "@registry/model/registry";
import { CreateGeoObjectAction, UpdateAttributeAction, AbstractAction, ValueOverTimeDiff } from "@registry/model/crtable";
import { ActionTypes } from "@registry/model/constants";

import Utils from "../../utility/Utils";

@Component({
    selector: "geoobject-shared-attribute-editor",
    templateUrl: "./geoobject-shared-attribute-editor.component.html",
    styleUrls: ["./geoobject-shared-attribute-editor.css"],
    providers: [DatePipe],
    animations: [
        [
            trigger("fadeInOut", [
                transition(":enter", [
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
            ]),
            trigger("slide", [
                state("left", style({ left: 0 })),
                state("right", style({ left: "100%" })),
                transition("* => *", animate(200))
            ])
        ]]
})

/**
 * IMPORTANT
 * This component is shared between:
 * - crtable (create-update-geo-object action detail)
 * - change-request (for submitting change requests)
 * - master list geoobject editing widget
 * Be wary of changing this component for one usecase and breaking other usecases!
 */
export class GeoObjectSharedAttributeEditorComponent implements OnInit, OnChanges {

    // eslint-disable-next-line accessor-pairs
    @Input() set geoObjectData(value: {"geoObject": GeoObjectOverTime, "actions": CreateGeoObjectAction[] | UpdateAttributeAction[]}) {

        this.geoObject = value.geoObject;
        this.postGeoObject = JSON.parse(JSON.stringify(value.geoObject));

        this.actions = value.actions;

        this.setGeoObjectToCurrent(this.postGeoObject);

    }

    // The current state of the GeoObject in the GeoRegistry
    geoObject: GeoObjectOverTime = null;

    // The changed state of the GeoObject in the GeoRegistry
    postGeoObject: GeoObjectOverTime = null;

    // Array of Actions that will be part of a Change Request Object
    actions: CreateGeoObjectAction[] | UpdateAttributeAction[] = [];

    calculatedGeoObject: any = {};

    showAllInstances: boolean = false;

    tabIndex: number = 0;

    isContributorOnly: boolean = false;

    @Input() animate: boolean = false;

    // The current state of the GeoObject in the GeoRegistry
//    @Input() action: Action = null;

    @Input() geoObjectType: GeoObjectType;

    @Input() attributeExcludes: string[] = [];

    @Input() forDate: Date = new Date();

    @Input() readOnly: boolean = false;

    @Input() isNew: boolean = false;

    @Input() isEditingGeometries = false;

    @Input() isGeometryInlined = false;

    @ViewChild("geometryEditor") geometryEditor;

    @Output() valid = new EventEmitter<boolean>();

    @Output() onManageVersion = new EventEmitter<Attribute>();

    // Observable subject for MasterList changes.  Called when an update is successful
    @Output() onChange = new EventEmitter<GeoObjectOverTime>();

    @Input() customEvent: boolean = false;

    modifiedTermOption: Term = null;
    currentTermOption: Term = null;
    isValid: boolean = true;

    geoObjectAttributeExcludes: string[] = ["uid", "sequence", "type", "lastUpdateDate", "createDate"];

    @ViewChild("attributeForm") attributeForm;

    constructor(private lService: LocalizationService, private authService: AuthService, private dateService: DateService) {

        this.isContributorOnly = this.authService.isContributerOnly();

    }

    ngOnInit(): void {

        if (this.attributeExcludes != null) {

            this.geoObjectAttributeExcludes.push.apply(this.geoObjectAttributeExcludes, this.attributeExcludes);

            if (!this.isGeometryInlined) {

                this.geoObjectAttributeExcludes.push("geometry");

            }

        }

        let geomAttr = null;
        for (let i = 0; i < this.geoObjectType.attributes.length; ++i) {

            if (this.geoObjectType.attributes[i].code === "geometry") {

                geomAttr = this.geoObjectType.attributes[i];

            }

        }
        if (geomAttr == null) {

            let geometry: Attribute = new Attribute("geometry", "geometry", new LocalizedValue("Geometry", null), new LocalizedValue("Geometry", null), true, false, false, true);
            this.geoObjectType.attributes.push(geometry);

        }

    }

    ngAfterViewInit() {

        this.attributeForm.statusChanges.subscribe(result => {

            this.isValid = (result === "VALID" || result === "DISABLED");

            this.valid.emit(this.isValid);

        });

    }

    ngOnChanges(changes: SimpleChanges) {

//        if (changes.geoObjectData) {

//            this.geoObject = new GeoObjectOverTime(this.geoObjectType, JSON.parse(JSON.stringify(this.geoObject)).attributes); // We're about to heavily modify this object. We don't want to muck with the original copy they sent us.
//
//            if (this.postGeoObject == null) {
//
//                this.postGeoObject = new GeoObjectOverTime(this.geoObjectType, JSON.parse(JSON.stringify(this.geoObject)).attributes); // Object.assign is a shallow copy. We want a deep copy.
//
//            } else {
//
//                this.postGeoObject = new GeoObjectOverTime(this.geoObjectType, JSON.parse(JSON.stringify(this.postGeoObject)).attributes); // We're about to heavily modify this object. We don't want to muck with the original copy they sent us.
//
//            }

//            this.calculate();

        // eslint-disable-next-line dot-notation

//        } else if (changes["forDate"]) {
//
//            this.calculate();
//
//        }

    }

    updateValueOverTime(actionDiffVOTs: ValueOverTimeDiff[], geoObjecAtttribute: AttributeOverTime, geoObject: GeoObjectOverTime, action: any) {

        // If action is an AttributeUpdateAction
        if (action.actionType === ActionTypes.UPDATEATTRIBUTETACTION) {

            if (geoObjecAtttribute.name === action.attributeName) {

                for (let i1 = 0; i1 < actionDiffVOTs.length; i1++) {

                    let actionDiffVOT = actionDiffVOTs[i1];

                    for (let i2 = 0; i2 < geoObjecAtttribute.values.length; i2++) {

                        let val = geoObjecAtttribute.values[i2];

                        if (actionDiffVOT.oid === val.oid) {

                            console.log("dif value --> ", actionDiffVOT.newValue)

//                            if (geoObjectAttribute.type === "date") {
//
//                                if (new Date(String(actionDiffVot.oldValue)) !== new Date(String(actionDiffVot.newValue))) {
//
//                                    let thisVot = new ValueOverTime();
//                                    thisVot.startDate = actionDiffVot.newStartDate;
//                                    thisVot.endDate = actionDiffVot.newEndDate;
//                                    thisVot.value = actionDiffVot.newValue;
//
//                                    newAttributeValues.push(thisVot);
//
//                                }
//
//                            } else if (geoObjectAttribute.type === "local") {
//
//                                newAttributeValues.push({ startDate: actionDiffVot.newStartDate, endDate: actionDiffVot.newEndDate, value: actionDiffVot.newValue });
//
//                            } else {
//
//                            }

                        }

                    }

                }

            }

        } else {

            // Else if action is a CreateGeoObjectAction

            console.log("create geoobject action")

        }

    }

    setGeoObjectToCurrent(geoObject: GeoObjectOverTime): void {

        // Iterate over all actions to find all the changes
        this.actions.forEach((action: any) => {

            // If action is an AttributeUpdateAction
            if (action.actionType === ActionTypes.UPDATEATTRIBUTETACTION) {

                if (geoObject.attributes[action.attributeName]) {

                    let geoObjectAttribute = geoObject.attributes[action.attributeName];

                    if (geoObjectAttribute.name === action.attributeName) {

                        this.updateValueOverTime(action.attributeDiff.valuesOverTime, geoObjectAttribute, geoObject, action);

                    }

                }

            }

        });

    }

//    setGeoObjectToCurrent(geoObject: GeoObjectOverTime): void {
//
//        // Iterate over all actions to find all the changes
//        this.actions.forEach((action) => {
//
//            if (geoObject.attributes[action.attributeName]) {
//
//                let attribute = geoObject.attributes[action.attributeName];
//
//                if (attribute.name === action.attributeName) {
//
//                    let newAttributeValues = [];
//                    action.attributeDiff.valuesOverTime.forEach((diffVot) => {
//
//                        if (attribute.type === "date") {
//
//                            if (new Date(String(diffVot.oldValue)) !== new Date(String(diffVot.newValue))) {
//
//                                let thisVot = new ValueOverTime();
//                                thisVot.startDate = diffVot.newStartDate;
//                                thisVot.endDate = diffVot.newEndDate;
//                                thisVot.value = diffVot.newValue;
//
//                                newAttributeValues.push(thisVot);
//
//                            }
//
//                        } else if (attribute.type === "local") {
//
//                            newAttributeValues.push({ startDate: diffVot.newStartDate, endDate: diffVot.newEndDate, value: diffVot.newValue });
//
//                        } else {
//
//                            if (diffVot.oldValue !== diffVot.newValue) {
//
//                                let thisVot = new ValueOverTime();
//                                thisVot.startDate = diffVot.newStartDate;
//                                thisVot.endDate = diffVot.newEndDate;
//                                thisVot.value = diffVot.newValue;
//
//                                newAttributeValues.push(thisVot);
//
//                            }
//
//                        }
//
//                    });
//
//                    attribute.values = newAttributeValues;
//
//                }
//
//            }
//
//        });
//
//    }

    changePage(nextPage: number): void {

        this.tabIndex = nextPage;

        if (nextPage === 2) {

            this.isEditingGeometries = true;

        } else {

            this.isEditingGeometries = false;

        }

    }

    setBoolean(attribute, value): void {

        attribute.value = value;

    }

//    calculate(): void {
//
//        this.calculatedGeoObject = this.calculateCurrent(this.geoObject);
//
//        if (this.geometryEditor != null) {
//
//            this.geometryEditor.reload();
//
//        }
//
//    }

//    calculateCurrent(goot: GeoObjectOverTime): any {
//
//        const object = {};
//
//        const time = this.forDate.getTime();
//
//        this.geoObjectType.attributes.forEach(attr => {
//
//            object[attr.code] = null;
//
//            if (attr.type === "local") {
//
//                object[attr.code] =
//                {
//                    startDate: null,
//                    endDate: null,
//                    value: this.lService.create()
//                };
//
//            }
//
//            if (attr.isChangeOverTime) {
//
//                let values = goot.attributes[attr.code].values;
//
//                values.forEach(vot => {
//
//                    const startDate = Date.parse(vot.startDate);
//                    const endDate = Date.parse(vot.endDate);
//
//                    if (time >= startDate && time <= endDate) {
//
//                        if (attr.type === "local") {
//
//                            object[attr.code] = {
//                                startDate: this.formatDate(vot.startDate),
//                                endDate: this.formatDate(vot.endDate),
//                                value: JSON.parse(JSON.stringify(vot.value))
//                            };
//
//                        } else if (attr.type === "term" && vot.value != null && Array.isArray(vot.value) && vot.value.length > 0) {
//
//                            object[attr.code] = {
//                                startDate: this.formatDate(vot.startDate),
//                                endDate: this.formatDate(vot.endDate),
//                                value: vot.value[0]
//                            };
//
//                        } else {
//
//                            object[attr.code] = {
//                                startDate: this.formatDate(vot.startDate),
//                                endDate: this.formatDate(vot.endDate),
//                                value: vot.value
//                            };
//
//                        }
//
//                    }
//
//                });
//
//            } else {
//
//                object[attr.code] = goot.attributes[attr.code];
//
//            }
//
//        });
//
//        for (let i = 0; i < this.geoObjectType.attributes.length; ++i) {
//
//            let attr = this.geoObjectType.attributes[i];
//
//            if (attr.isChangeOverTime && !object[attr.code]) {
//
//                object[attr.code] = {
//                    startDate: null,
//                    endDate: null,
//                    value: ""
//                };
//
//            }
//
//        }
//
//        return object;
//
//    }

    formatDate(date: string): string {

        return this.dateService.formatDateForDisplay(date);

    }

    handleChangeCode(e: any): void {
        // this.postGeoObject.attributes.code = this.calculatedPostObject["code"];

        // this.onChange.emit(this.postGeoObject);
    }

    onManageGeometryVersions(): void {

        let geometry = null;
        for (let i = 0; i < this.geoObjectType.attributes.length; ++i) {

            if (this.geoObjectType.attributes[i].code === "geometry") {

                // eslint-disable-next-line no-unused-vars
                geometry = this.geoObjectType.attributes[i];

            }

        }

        // TODO: Determine if this is needed with the new design
        // this.onManageAttributeVersions(geometry);

    }

    onVersionChange(event:any, attribute: Attribute): void {

        if (attribute) {

            // TODO: Update actions
            console.log(attribute);

        }

    }

    isNullValue(vot: any) {

        return vot == null || vot.value == null || vot.value === "";

    }

    onSelectPropertyOption(event: any, option: any): void {

        this.currentTermOption = JSON.parse(JSON.stringify(this.modifiedTermOption));

    }

    getGeoObjectTypeTermAttributeOptions(termAttributeCode: string) {

        for (let i = 0; i < this.geoObjectType.attributes.length; i++) {

            let attr: any = this.geoObjectType.attributes[i];

            if (attr.type === "term" && attr.code === termAttributeCode) {

                attr = <AttributeTerm>attr;
                let attrOpts = attr.rootTerm.children;

                if (attr.code === "status") {

                    return Utils.removeStatuses(attrOpts);

                } else {

                    return attrOpts;

                }

            }

        }

        return null;

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

    getTypeDefinition(key: string): string {

        for (let i = 0; i < this.geoObjectType.attributes.length; i++) {

            let attr = this.geoObjectType.attributes[i];

            if (attr.code === key) {

                return attr.type;

            }

        }

        return null;

    }

    public getIsValid(): boolean {

        return this.isValid;

    }

    public getGeoObject(): any {
        // return this.postGeoObject;
    }

}
