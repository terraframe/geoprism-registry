import { Component, OnInit, ViewChild, Input, ViewChildren, QueryList } from "@angular/core";
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
import { ChangeRequestEditor } from "./change-request-editor";
import { ManageVersionsComponent } from "./manage-versions.component";

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

    changeRequestEditor: ChangeRequestEditor;

    @Input() geoObjectType: GeoObjectType;

    @Input() attributeExcludes: string[] = [];

    @Input() readOnly: boolean = false;

    @Input() isNew: boolean = false;

    @Input() isGeometryInlined = false;

    @Input() changeRequest: ChangeRequest;

    @Input() hierarchies: HierarchyOverTime[];

    modifiedTermOption: Term = null;
    currentTermOption: Term = null;

    @Input() filterDate: string = null;

    // TODO : This was copy / pasted into manage-versions.component::onDateChange and ChangeRequestEditor::generateAttributeEditors
    geoObjectAttributeExcludes: string[] = ["uid", "sequence", "type", "lastUpdateDate", "createDate", "invalid", "exists"];

    @ViewChild("attributeForm") attributeForm;

    @ViewChildren(ManageVersionsComponent) manageVersions: QueryList<any>;

    public parentAttributeType: AttributeType;

    public geometryAttributeType: AttributeType;

    showStabilityPeriods = false;

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

        this.changeRequestEditor = new ChangeRequestEditor(this.changeRequest, this.postGeoObject, this.geoObjectType, this.hierarchies, this.geometryAttributeType, this.parentAttributeType, this.lService, this.dateService);

        if (this.shouldForceSetExist()) {
            this.changePage(3);
        }

        if (this.isNew) {
            this.filterDate = null;
        }

        let got = this.changeRequest.current ? this.changeRequest.current.geoObjectType : this.postGeoObject.geoObjectType;
        let orgCode = got.organizationCode;
        this.showStabilityPeriods = (this.authService.isSRA() || this.authService.isOrganizationRA(orgCode) || this.authService.isGeoObjectTypeOrSuperRM(got) || this.authService.isGeoObjectTypeOrSuperRC(got));
    }

    setFilterDate(date: string, refresh: boolean = true) {
        this.filterDate = date;

        if (this.manageVersions != null) {
            this.manageVersions.forEach(manageVersion => manageVersion.setFilterDate(this.filterDate, refresh));
        }
    }

    getChangeRequestEditor(): ChangeRequestEditor {
        return this.changeRequestEditor;
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

    shouldForceSetExist() {
        if (this.isNew && this.postGeoObject.attributes["exists"]) {
            let values = this.postGeoObject.attributes["exists"].values;

            if (values && values.length > 0) {
                let value = values[0];

                return value.startDate == null || value.endDate == null || value.value === undefined || value.value === null;
            }
        }

        return this.isNew;
    }

    getAttribute(name: string): AttributeType {
        if (name === "_PARENT_") {
            return this.parentAttributeType;
        } else if (name === "geometry") {
            return this.geometryAttributeType;
        }

        for (let i = 0; i < this.geoObjectType.attributes.length; ++i) {
            if (this.geoObjectType.attributes[i].code === name) {
                return this.geoObjectType.attributes[i];
            }
        }

        return null;
    }

    changePage(nextPage: number): void {
        if (this.shouldForceSetExist() && nextPage !== 3) {
            return;
        }

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
                    } else if ((updateAttrAction.attributeName === "invalid" || updateAttrAction.attributeName === "exists") && tabIndex === 3) {
                        return true;
                    } else if (tabIndex === 0 && updateAttrAction.attributeName !== "_PARENT_" && updateAttrAction.attributeName !== "geometry" && updateAttrAction.attributeName !== "exists" && updateAttrAction.attributeName !== "invalid") {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    hasErrors(tabIndex: number) {
        let attributeEditors = this.changeRequestEditor.getEditors();

        if (tabIndex === 0) {
            let filter = ["invalid", "exists", "_PARENT_", "geometry"];
            let filteredEditors = attributeEditors.filter(editor => filter.indexOf(editor.attribute.code) === -1);

            for (let i = 0; i < filteredEditors.length; ++i) {
                let editor = filteredEditors[i];

                if (!editor.isValid()) {
                    return true;
                }
            }
        } else if (tabIndex === 1) {
            for (let i = 0; i < this.hierarchies.length; ++i) {
                let hierarchy = this.hierarchies[i];

                if (!this.changeRequestEditor.getEditorForAttribute(this.parentAttributeType, hierarchy).isValid()) {
                    return true;
                }
            }
        } else if (tabIndex === 2) {
            return !this.changeRequestEditor.getEditorForAttribute(this.geometryAttributeType).isValid();
        } else if (tabIndex === 3) {
            let invalid = this.getAttribute("invalid");

            let existsAttribute: AttributeType = GeoObjectType.getAttribute(this.changeRequestEditor.geoObjectType, "exists");
            let existsEditor = this.changeRequestEditor.getEditorForAttribute(existsAttribute);

            return (Object.prototype.hasOwnProperty.call(invalid, "isValid") && !invalid.isValid) ||
            !existsEditor.isValid();
        }

        return false;
    }

    public isValid(): boolean {
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

        return allValid && this.changeRequestEditor.validate();
    }

    public getActions(): AbstractAction[] {
        return this.changeRequestEditor.changeRequest.actions;
    }

}
