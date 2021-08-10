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

import { GeoObjectType, GeoObjectOverTime, AttributeType, AttributeOverTime, AttributeTermType, Term, ValueOverTime, HierarchyOverTime } from "@registry/model/registry";
import { CreateGeoObjectAction, UpdateAttributeAction, AbstractAction, ValueOverTimeDiff, ChangeRequest } from "@registry/model/crtable";
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

    constructor(private lService: LocalizationService, private authService: AuthService, private dateService: DateService) {

      this.isContributorOnly = this.authService.isContributerOnly();

    }
    
    stringify(input: any): string {
      return JSON.stringify(input);
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

    }

    ngAfterViewInit() {

        this.attributeForm.statusChanges.subscribe(result => {

            this.isValid = (result === "VALID" || result === "DISABLED");

            this.valid.emit(this.isValid);

        });

    }

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

    formatDate(date: string): string {

        return this.dateService.formatDateForDisplay(date);

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

    // Invoked when the dates have changed on an attribute
    // After each change we must check to see if all attributes
    // are now valid or not
    onIsValidChange(valid:boolean, attribute: AttributeType): void {
      attribute.isValid = valid;
      
      let allValid:boolean = true;
      
      this.geoObjectType.attributes.forEach(att => {
        if(att.isValid != null && !att.isValid) {
          allValid = false;
        }
      });
      
      this.valid.emit(allValid);
    }

    onVersionChange(event:any, attribute: AttributeType): void {
      
      console.log(event);

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

    onApprove(attr: any): void
    {
        console.log(attr)

//        this.requestService.setActionStatus(actionOid, status).then(results => {
//            console.log(results)
//        })
//        .catch((err: HttpErrorResponse) => {
//            this.error(err);
//        });

//        TODO
//        this.onChange.emit(this.postGeoObject);
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
