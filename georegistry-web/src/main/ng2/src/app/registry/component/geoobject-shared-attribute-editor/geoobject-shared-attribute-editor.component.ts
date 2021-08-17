import { Component, OnInit, ViewChild, Input, Output, EventEmitter } from "@angular/core";
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
import { GeometryService } from "@registry/service";
import { DateService } from "@shared/service/date.service";

import { GeoObjectType, GeoObjectOverTime, AttributeType, AttributeTermType, Term, HierarchyOverTime } from "@registry/model/registry";
import { CreateGeoObjectAction, UpdateAttributeAction, AbstractAction, ChangeRequest } from "@registry/model/crtable";
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
 * - crtable (request-table.component.ts)
 * - change-request (for submitting change requests)
 * - master list geoobject editing widget (feature-panel.component.ts)
 * Be wary of changing this component for one usecase and breaking other usecases!
 */
export class GeoObjectSharedAttributeEditorComponent implements OnInit {

    // eslint-disable-next-line accessor-pairs
    @Input() set geoObjectData(value: {"geoObject": GeoObjectOverTime, "actions": CreateGeoObjectAction[] | UpdateAttributeAction[]}) {
        this.preGeoObject = value.geoObject;
        this.postGeoObject = JSON.parse(JSON.stringify(value.geoObject));

        this.actions = value.actions;
        this.onCodeChange();
    }

    // The current state of the GeoObject in the GeoRegistry
    @Input() preGeoObject: GeoObjectOverTime = null;

    // The changed state of the GeoObject in the GeoRegistry
    @Input() postGeoObject: GeoObjectOverTime = null;

    // Array of Actions that will be part of a Change Request Object
    actions: AbstractAction[] = [];

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

    @Input() changeRequest: ChangeRequest;

    @ViewChild("geometryEditor") geometryEditor;

    @Output() valid = new EventEmitter<boolean>();

    @Output() onManageVersion = new EventEmitter<AttributeType>();

    // Observable subject for MasterList changes.  Called when an update is successful
    @Output() onChange = new EventEmitter<GeoObjectOverTime>();

    @Input() customEvent: boolean = false;

    @Input() hierarchies: HierarchyOverTime[];

    modifiedTermOption: Term = null;
    currentTermOption: Term = null;
    isValid: boolean = true;

    geoObjectAttributeExcludes: string[] = ["uid", "sequence", "type", "lastUpdateDate", "createDate"];

    @ViewChild("attributeForm") attributeForm;

    parentAttributeType: AttributeType;

    geometryAttributeType: AttributeType;

    constructor(private lService: LocalizationService, private geomService: GeometryService, private authService: AuthService, private dateService: DateService) {
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
            this.geometryAttributeType = new AttributeType("geometry", "geometry", new LocalizedValue("Geometry", null), new LocalizedValue("Geometry", null), true, false, false, true);
        }

        this.parentAttributeType = new AttributeType("_PARENT_", "_PARENT_", new LocalizedValue("Parents", null), new LocalizedValue("Parents", null), true, false, false, true);
        this.onCodeChange();
    }

    ngAfterViewInit() {
        this.attributeForm.statusChanges.subscribe(result => {
            this.isValid = (result === "VALID" || result === "DISABLED");

            this.valid.emit(this.isValid);
        });
    }

    changePage(nextPage: number): void {
        this.geomService.destroy(false);
        
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

    formatDate(date: string): string {
        return this.dateService.formatDateForDisplay(date);
    }

    hasChanges(tabIndex: number) {
        let len = this.actions.length;

        if (len > 0) {
            for (let i = 0; i < len; ++i) {
                let action: AbstractAction = this.actions[i];

                if (action.actionType === ActionTypes.CREATEGEOOBJECTACTION) {
                    return false;
                } else if (action.actionType === ActionTypes.UPDATEATTRIBUTETACTION) {
                    let updateAttrAction: UpdateAttributeAction = action as UpdateAttributeAction;

                    if (updateAttrAction.attributeName === "_PARENT_" && tabIndex === 1) {
                        return true;
                    } else if (updateAttrAction.attributeName === "geometry" && tabIndex === 2) {
                        return true;
                    } else if (tabIndex === 0 && updateAttrAction.attributeName !== "_PARENT_" && updateAttrAction.attributeName !== "geometry") {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    onCodeChange(): void {
        let newVal = this.postGeoObject.attributes["code"];
        this.geoObjectType.attributes.forEach(att => {
            if (att.code === "code") {
                att.isValid = (newVal != null) && newVal.length > 0;
            }
        });
    }

    // Invoked when the dates have changed on an attribute
    // After each change we must check to see if all attributes
    // are now valid or not
    onIsValidChange(valid:boolean, attribute: AttributeType): void {
        attribute.isValid = valid;

        let allValid:boolean = true;

        this.geoObjectType.attributes.forEach(att => {
            if (att.isValid != null && !att.isValid) {
                allValid = false;
            }
        });

        if (this.parentAttributeType.isValid != null && !this.parentAttributeType.isValid) {
            allValid = false;
        }

        if (this.geometryAttributeType.isValid != null && !this.geometryAttributeType.isValid) {
            allValid = false;
        }

        this.valid.emit(allValid);
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
                attr = <AttributeTermType>attr;
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

    public getActions(): AbstractAction[] {
        return this.actions;
    }

    public getIsValid(): boolean {
        return this.isValid;
    }

    public getGeoObject(): any {
        // return this.postGeoObject;
    }

}
