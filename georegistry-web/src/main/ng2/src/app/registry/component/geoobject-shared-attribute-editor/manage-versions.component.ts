import {
    Component,
    OnInit,
    Input,
    Output,
    ChangeDetectorRef,
    EventEmitter,
    ViewChildren,
    QueryList
} from "@angular/core";
import {
    trigger,
    style,
    animate,
    transition
} from "@angular/animations";

import { GeoObjectType, Attribute, AttributeOverTime, ValueOverTime, GeoObjectOverTime, AttributeTerm, PRESENT } from "@registry/model/registry";
import { CreateGeoObjectAction, UpdateAttributeAction, ValueOverTimeDiff } from "@registry/model/crtable";
import { LocalizedValue } from "@shared/model/core";

import { DateFieldComponent } from "../../../shared/component/form-fields/date-field/date-field.component";

import { RegistryService } from "@registry/service";
import { DateService } from "@shared/service/date.service";

import { LocalizationService } from "@shared/service";

import Utils from "../../utility/Utils";

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
        ]]
})
export class ManageVersionsComponent implements OnInit {

    @Input() isNew: boolean = false;

    @ViewChildren("dateFieldComponents") dateFieldComponentsArray: QueryList<DateFieldComponent>;

    message: string = null;

    currentDate: Date = new Date();

    isValid: boolean = true;
    @Output() isValidChange = new EventEmitter<boolean>();

    @Input() readonly: boolean = false;

    @Input() selectedTab: number = 0;

    // Observable subject for MasterList changes.  Called when an update is successful
    @Output() onChange = new EventEmitter<GeoObjectOverTime>();

    attributeType: Attribute;
    actions: CreateGeoObjectAction[] | UpdateAttributeAction[] = [];
    postActions: CreateGeoObjectAction[] | UpdateAttributeAction[] = [];

    // eslint-disable-next-line accessor-pairs
    @Input() set attributeData(value: {"attributeType":Attribute, "actions":CreateGeoObjectAction[] | UpdateAttributeAction[], geoObject:GeoObjectOverTime}) {

        this.attributeType = value.attributeType;

        this.actions = value.actions;
        this.postActions = JSON.parse(JSON.stringify(value.actions));

        this.originalGeoObjectOverTime = JSON.parse(JSON.stringify(value.geoObject));
        this.geoObjectOverTime = value.geoObject;

        if (this.attributeType.code === "geometry" && this.geoObjectOverTime.attributes[this.attributeType.code].values.length === 1) {

            this.editingGeometry = 0;

        }

    }

    @Input() geoObjectType: GeoObjectType;

    originalGeoObjectOverTime: GeoObjectOverTime;
    geoObjectOverTime: GeoObjectOverTime;

    @Input() isNewGeoObject: boolean = false;

    goGeometries: GeoObjectOverTime;

    newVersion: ValueOverTime;

    editingGeometry: number = -1;

    hasDuplicateDate: boolean = false;

    conflict: string;
    hasConflict: boolean = false;
    hasGap: boolean = false;

    originalAttributeState: Attribute;

    // eslint-disable-next-line no-useless-constructor
    constructor(private service: RegistryService, private lService: LocalizationService, public changeDetectorRef: ChangeDetectorRef, private dateService: DateService) { }

    ngOnInit(): void {
    }

    ngAfterViewInit() {
    }

    geometryChange(vAttribute, event): void {

        vAttribute.value = event;

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

    onDateChange(): any {

        setTimeout(() => {

            this.hasConflict = false;
            this.hasGap = false;

            let vAttributes = this.geoObjectOverTime.attributes[this.attributeType.code].values;

            this.isValid = this.checkDateFieldValidity();

            this.hasConflict = this.dateService.checkRanges(vAttributes);

        }, 0);

    }

    onAddNewVersion(): void {

        let votArr: ValueOverTimeDiff[] = this.geoObjectOverTime.attributes[this.attributeType.code].values;

        let vot: ValueOverTimeDiff = new ValueOverTimeDiff();
        vot.newStartDate = null; // Utils.formatDateString(new Date());
        vot.newEndDate = null; // Utils.formatDateString(new Date());

        if (this.attributeType.type === "local") {

            //   vot.value = {"localizedValue":null,"localeValues":[{"locale":"defaultLocale","value":null},{"locale":"km_KH","value":null}]};
            vot.newValue = this.lService.create();

        } else if (this.attributeType.type === "geometry") {

            if (votArr.length > 0) {

                if (this.editingGeometry !== -1 && this.editingGeometry != null) {

                    vot.newValue = votArr[this.editingGeometry].oldValue;

                } else {

                    vot.newValue = votArr[0].oldValue;

                }

            } else {

                vot.newValue = { type: this.geoObjectType.geometryType, coordinates: [] };

                if (this.geoObjectType.geometryType === "MULTIPOLYGON") {

                    vot.newValue.type = "MultiPolygon";

                } else if (this.geoObjectType.geometryType === "POLYGON") {

                    vot.newValue.type = "Polygon";

                } else if (this.geoObjectType.geometryType === "POINT") {

                    vot.newValue.type = "Point";

                } else if (this.geoObjectType.geometryType === "MULTIPOINT") {

                    vot.newValue.type = "MultiPoint";

                } else if (this.geoObjectType.geometryType === "LINE") {

                    vot.newValue.type = "Line";

                } else if (this.geoObjectType.geometryType === "MULTILINE") {

                    vot.newValue.type = "MultiLine";

                }

            }

        } else if (this.attributeType.type === "term") {

            let terms = this.getGeoObjectTypeTermAttributeOptions(this.attributeType.code);

            if (terms && terms.length > 0) {

                vot.newValue = terms[0].code;

            }

        }

        votArr.push(vot);

        if (this.attributeType.code === "geometry") {

            this.editingGeometry = votArr.length - 1;

        }

        this.changeDetectorRef.detectChanges();

    }

    editGeometry(index: number) {

        this.editingGeometry = index;

    }

    getVersionData(attribute: Attribute) {

        let versions: ValueOverTime[] = [];

        this.geoObjectOverTime.attributes[attribute.code].values.forEach(vAttribute => {

            vAttribute.value.localeValues.forEach(val => {

                versions.push(val);

            });

        });

        return versions;

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

                attr = <AttributeTerm>attr;
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

    remove(version: any): void {

        let val = this.geoObjectOverTime.attributes[this.attributeType.code];

        let position = -1;
        for (let i = 0; i < val.values.length; i++) {

            let vals = val.values[i];

            if (vals.startDate === version.startDate) {

                position = i;

            }

        }

        if (position > -1) {

            val.values.splice(position, 1);

        }

        this.onDateChange();

    }

    isChangeOverTime(attr: Attribute): boolean {

        let isChangeOverTime = false;

        this.geoObjectType.attributes.forEach(attribute => {

            if (this.attributeType.code === attr.code) {

                isChangeOverTime = attr.isChangeOverTime;

            }

        });

        return isChangeOverTime;

    }

//    TODO: Deprecate becasue it seems to not be used anywhere
    sort(votArr: ValueOverTimeDiff[]): void {

        // Sort the data by start date
        votArr.sort(function(a, b) {

            if (a.oldStartDate == null || a.oldStartDate === "") {

                return 1;

            } else if (b.oldStartDate == null || b.oldStartDate === "") {

                return -1;

            }

            let first: any = new Date(a.oldStartDate);
            let next: any = new Date(b.oldStartDate);
            return first - next;

        });

    }

    getValueDifference(attribute: AttributeOverTime, localKey: string): string {

        let oldValue: string = null;

        // if ( this.isNullValue(this.calculatedPostObject[attribute.code]) && !this.isNullValue(this.calculatedPreObject[attribute.code])) {
        //  return true;
        // }
        //
        // return (this.calculatedPostObject[attribute.code].value && this.calculatedPostObject[attribute.code].value.trim() !== this.calculatedPreObject[attribute.code].value);

        // Iterate over all actions to find all the changes
        this.actions.forEach((action) => {

            if (attribute.name === action.attributeName) {

                action.attributeDiff.valuesOverTime.forEach((vot) => {

                    if (attribute.type === "date") {

                        if (new Date(String(vot.oldValue)).getTime() !== new Date(String(vot.newValue)).getTime()) {

                            oldValue = String(vot.oldValue);

                        }

                    } else if (attribute.type === "local") {

                        (vot.oldValue as LocalizedValue).localeValues.forEach(oldLocalVal => {

                            if (oldLocalVal.locale === localKey) {

                                (vot.newValue as LocalizedValue).localeValues.forEach(newLocalVal => {

                                    if (oldLocalVal.value !== newLocalVal.value) {

                                        oldValue = String(oldLocalVal.value);

                                    }

                                });

                            }

                        });

                    } else {

                        if (vot.oldValue !== vot.newValue) {

                            oldValue = String(vot.oldValue);

                            vot.currentValue = oldValue;

                        }

                    }

                });

            }

        });

        return oldValue;

    }

    /**
    * TODO: Dedeprecate
    */
//    isDifferentValue(attribute: AttributeOverTime, localKey: string): boolean {
//
//        let isDifferent: boolean = false;
//
//        // if ( this.isNullValue(this.calculatedPostObject[attribute.code]) && !this.isNullValue(this.calculatedPreObject[attribute.code])) {
//        //  return true;
//        // }
//        //
//        // return (this.calculatedPostObject[attribute.code].value && this.calculatedPostObject[attribute.code].value.trim() !== this.calculatedPreObject[attribute.code].value);
//
//        // Iterate over all actions to find all the changes
//        this.actions.forEach((action) => {
//
//            if (attribute.name === action.attributeName) {
//
//                action.attributeDiff.valuesOverTime.forEach((vot) => {
//
//                    if (attribute.type === "date") {
//
//                        if (new Date(String(vot.oldValue)).getTime() !== new Date(String(vot.newValue)).getTime()) {
//
//                            isDifferent = true;
//
//                        }
//
//                    } else if (attribute.type === "local") {
//
//                        (vot.oldValue as LocalizedValue).localeValues.forEach(oldLocalVal => {
//
//                            if (oldLocalVal.locale === localKey) {
//
//                                (vot.newValue as LocalizedValue).localeValues.forEach(newLocalVal => {
//
//                                    if (oldLocalVal.value !== newLocalVal.value) {
//
//                                        isDifferent = true;
//
//                                    }
//
//                                });
//
//                            }
//
//                        });
//
//                    } else {
//
//                        if (vot.oldValue !== vot.newValue) {
//
//                            isDifferent = true;
//
//                        }
//
//                    }
//
//                });
//
//            }
//
//        });
//
//        return isDifferent;
//
//    }

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

    onApprove(): void {

//        TODO
//        this.onChange.emit(this.geoObjectOverTime);
        // this.isValidChange.emit(this.isValid);

    }

    onReject(): void {

//        TODO
//        this.onChange.emit(this.originalGeoObjectOverTime);

    }

    onPending(): void {
//        TODO
    }

}