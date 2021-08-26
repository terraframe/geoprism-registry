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

import { GeoObjectType, GeoObjectOverTime, AttributeType, Term, HierarchyOverTime } from "@registry/model/registry";
import { UpdateAttributeOverTimeAction, AbstractAction, CreateGeoObjectAction, ChangeRequest } from "@registry/model/crtable";
import { ActionTypes } from "@registry/model/constants";

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

    // The changed state of the GeoObject in the GeoRegistry
    @Input() postGeoObject: GeoObjectOverTime = null;

    showAllInstances: boolean = false;

    tabIndex: number = 0;

    isContributorOnly: boolean = false;

    // The current state of the GeoObject in the GeoRegistry
//    @Input() action: Action = null;

    @Input() geoObjectType: GeoObjectType;

    @Input() attributeExcludes: string[] = [];

    @Input() readOnly: boolean = false;

    @Input() isNew: boolean = false;

    @Input() isGeometryInlined = false;

    @Input() changeRequest: ChangeRequest;

    @ViewChild("geometryEditor") geometryEditor;

    @Output() valid = new EventEmitter<boolean>();

    @Output() onManageVersion = new EventEmitter<AttributeType>();

    // Observable subject for MasterList changes.  Called when an update is successful
    @Output() onChange = new EventEmitter<GeoObjectOverTime>();

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

        this.geometryAttributeType = this.getAttribute("geometry");
        if (this.geometryAttributeType == null) {
            this.geometryAttributeType = new AttributeType("geometry", "geometry", new LocalizedValue("Geometry", null), new LocalizedValue("Geometry", null), true, false, false, true);
        }

        this.parentAttributeType = new AttributeType("_PARENT_", "_PARENT_", new LocalizedValue("Parents", null), new LocalizedValue("Parents", null), true, false, false, true);
        this.onNonChangeOverTimeAttributeChange(this.getAttribute("code"));

        if (this.changeRequest == null) {
            this.changeRequest = this.createNewChangeRequest();
        } else {
            if (!this.changeRequest.actions) {
                this.changeRequest.actions = [];
            }

            if (this.changeRequest.actions.length === 0 && this.isNew) {
                this.changeRequest.actions = this.createNewChangeRequest().actions;
            }
        }
    }

    createNewChangeRequest(): ChangeRequest {
        let cr = new ChangeRequest();
        cr.approvalStatus = "PENDING";
        cr.actions = [];

        if (this.isNew) {
            cr.type = "CreateGeoObject";

            let createAction: CreateGeoObjectAction = new CreateGeoObjectAction();
            createAction.geoObjectJson = this.postGeoObject;
            createAction.parentJson = this.hierarchies;
            cr.actions[0] = createAction;
        } else {
            cr.type = "UpdateGeoObject";
        }

        return cr;
    }

    ngAfterViewInit() {
        this.attributeForm.statusChanges.subscribe(result => {
            this.isValid = (result === "VALID" || result === "DISABLED");

            this.valid.emit(this.isValid);
        });
    }

    getAttribute(name: string): AttributeType {
        for (let i = 0; i < this.geoObjectType.attributes.length; ++i) {
            if (this.geoObjectType.attributes[i].code === name) {
                return this.geoObjectType.attributes[i];
            }
        }

        return null;
    }

    changePage(nextPage: number): void {
        this.geomService.destroy(false);

        this.tabIndex = nextPage;
    }

    hasChanges(tabIndex: number) {
        let len = this.changeRequest.actions.length;

        if (len > 0) {
            for (let i = 0; i < len; ++i) {
                let action: AbstractAction = this.changeRequest.actions[i];

                if (action.actionType === ActionTypes.CREATEGEOOBJECTACTION) {
                    return false;
                } else if (action.actionType === ActionTypes.UPDATEATTRIBUTETACTION) {
                    let updateAttrAction: UpdateAttributeOverTimeAction = action as UpdateAttributeOverTimeAction;

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

    onNonChangeOverTimeAttributeChange(attribute: AttributeType): void {
        let newVal = this.postGeoObject.attributes[attribute.code];
        attribute.isValid = (newVal != null) && newVal.length > 0;
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

    stringify(input: any) {
        return JSON.stringify(input);
    }

    public getActions(): AbstractAction[] {
        return this.changeRequest.actions;
    }

    public getIsValid(): boolean {
        return this.isValid;
    }

    public getGeoObject(): any {
        // return this.postGeoObject;
    }

}
