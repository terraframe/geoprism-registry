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
import { CreateGeoObjectAction, UpdateAttributeAction, AbstractAction, ValueOverTimeDiff, ChangeRequest, SummaryKey } from "@registry/model/crtable";
import { LocalizedValue } from "@shared/model/core";
import { ConflictType, ActionTypes, GovernanceStatus } from '@registry/model/constants';
import { AuthService } from "@shared/service/auth.service";
import { v4 as uuid } from 'uuid';

import { DateFieldComponent } from "../../../shared/component/form-fields/date-field/date-field.component";
import { ErrorHandler } from "@shared/component";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import { RegistryService, GeometryService } from "@registry/service";
import { ChangeRequestService } from "@registry/service/change-request.service";
import { DateService } from "@shared/service/date.service";

import { LocalizationService } from "@shared/service";

import Utils from "../../utility/Utils";

import { VersionDiffView } from "./VersionDiffView";
import { HierarchyEditPropagator } from "./HierarchyEditPropagator";
import { ValueOverTimeEditPropagator } from "./ValueOverTimeEditPropagator";

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

    @Input() selectedTab: number = 0;

    @Input() isGeometryInlined: boolean = false;

    // Observable subject for MasterList changes.  Called when an update is successful // TODO : This probably doesn't work anymore
    @Output() onChange = new EventEmitter<GeoObjectOverTime>();
    
    attributeType: AttributeType;
    actions: AbstractAction[] = [];

    // eslint-disable-next-line accessor-pairs
    @Input() set attributeData(value: {"attributeType":AttributeType, "changeRequest":ChangeRequest, "actions":AbstractAction[], geoObject:GeoObjectOverTime}) {

        this.attributeType = value.attributeType;

        this.changeRequest = value.changeRequest;
        
        if(this.changeRequest != null) {
          this.actions = value.changeRequest.actions;
        }
        else {
          this.actions = value.actions;
        }

        this.originalGeoObjectOverTime = JSON.parse(JSON.stringify(value.geoObject));
        this.postGeoObject = value.geoObject;
    }

    @Input() geoObjectType: GeoObjectType;

    originalGeoObjectOverTime: GeoObjectOverTime;
    postGeoObject: GeoObjectOverTime;

    @Input() isNewGeoObject: boolean = false;
    
    @Input() hierarchy: HierarchyOverTime = null;
    
    changeRequest: ChangeRequest;

    goGeometries: GeoObjectOverTime;

    newVersion: ValueOverTime;

    hasDuplicateDate: boolean = false;

    conflict: string;
    hasConflict: boolean = false;
    hasGap: boolean = false;

    originalAttributeState: AttributeType;
    
    viewModels: VersionDiffView[] = [];
    
    // The 'current' action which is to be used whenever we're applying new edits.
    editAction: AbstractAction;
    
    isRootOfHierarchy: boolean = false;
    
    // eslint-disable-next-line no-useless-constructor
    constructor(private geomService: GeometryService, public cdr: ChangeDetectorRef, public service: RegistryService, public lService: LocalizationService, 
        public changeDetectorRef: ChangeDetectorRef, private dateService: DateService, private authService: AuthService,
        private requestService: ChangeRequestService, private modalService: BsModalService, private elementRef: ElementRef) { }

    ngOnInit(): void {
      this.calculateViewModels();
      this.isRootOfHierarchy = this.attributeType.type === '_PARENT_' && (this.hierarchy == null || this.hierarchy.types == null || this.hierarchy.types.length == 0);
      this.geomService.setLayers(this.getRenderedLayers());
    }

    ngAfterViewInit() {
    }
    
    getRenderedLayers(): ValueOverTimeEditPropagator[]
    {
      let renderedLayers: ValueOverTimeEditPropagator[] = [];
      
      let len = this.viewModels.length;
      for (let i = 0; i < len; ++i)
      {
        let view: VersionDiffView = this.viewModels[i];
        
        if (view.isRenderingLayer)
        {
          renderedLayers.push(view.editPropagator);
        }
      }
      
      return renderedLayers;
    }

    geometryChange(vAttribute, event): void {

        //vAttribute.value = event; // TODO

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

            this.isValid = this.checkDateFieldValidity();

            this.hasConflict = this.dateService.checkRanges(this.viewModels);
            
            this.isValidChange.emit(this.isValid && !this.hasConflict);
        }, 0);

    }
    
    public stringify(obj: any): string {
      return JSON.stringify(obj);
    }
    
    remove(view: VersionDiffView): void {
      
      view.editPropagator.remove();      
      
      if(view.summaryKey === SummaryKey.NEW) {
        const index = this.viewModels.findIndex(v => v.oid === view.oid);       
        
        if(index > -1) {
          this.viewModels.splice(index, 1);
        }
      }
      
            console.log(view);

      this.onDateChange();      
    }

    onAddNewVersion(): void {

        let view: VersionDiffView = new VersionDiffView(this, this.editAction);
        view.oid = this.generateUUID();
        
        view.editPropagator.onAddNewVersion();

        this.viewModels.push(view);

        /*
        if (this.attributeType.code === "geometry") {

            this.editingGeometry = votArr.length - 1;

        }
        */
        
        this.changeDetectorRef.detectChanges();

    }
    
    generateUUID() {
      return uuid();
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
    
    getValueAtLocale(lv: LocalizedValue, locale: string)
    {
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

    isChangeOverTime(attr: AttributeType): boolean {

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
    
    onActionChange(action: AbstractAction)
    {
      let hasChanges: boolean = true;
    
      if (action instanceof UpdateAttributeAction)
      {
        let updateAction: UpdateAttributeAction = action as UpdateAttributeAction;
        
        if (updateAction.attributeDiff.valuesOverTime.length === 0)
        {
          hasChanges = false;
        }
        else
        {
          
        }
      }
      
      let index = -1;
      
      let len = this.actions.length;
      for (let i = 0; i < len; ++i)
      {
        let loopAction = this.actions[i];
      
        if (action === loopAction)
        {
          index = i;
        }
      };
      
      if (index != -1 && !hasChanges)
      {
        this.actions.splice(index, 1);
      }
      else if (index == -1 && hasChanges)
      {
        this.actions.push(action);
      }
    }
    
    findViewByOid(oid: string): VersionDiffView
    {
      let len = this.viewModels.length;
      for (let i = 0; i < len; ++i)
      {
        let view = this.viewModels[i];
        
        if (view.oid === oid)
        {
          return view;
        }
      }
      
      return null;
    }
    
    findPostGeoObjectVOT(oid: string)
    {
      this.postGeoObject.attributes[this.attributeType.code].values.forEach((vot: ValueOverTime) => {
        if (vot.oid === oid)
        {
          return vot;
        }
      });
      
      return null;
    }
    
    /**
     * Our goal here is to loop over the action diffs and then calculate what to display to the end user.
     */
    calculateViewModels(): void {
    
      if (this.isNew)
      {
        let createAction: CreateGeoObjectAction = new CreateGeoObjectAction();
        createAction.geoObjectJson = this.postGeoObject;
        createAction.parentJson = this.hierarchy;
        this.actions[0] = createAction;
        this.editAction = createAction;
      }
      else
      {
        this.actions.forEach((action: AbstractAction) => {
          if (action.actionType === ActionTypes.UPDATEATTRIBUTETACTION)
          {
            let updateAttrAction: UpdateAttributeAction = action as UpdateAttributeAction;
            
            if (this.attributeType.code === updateAttrAction.attributeName) {
              this.editAction = action;
            }
          }
        });
      
        if (this.editAction == null)
        {
          this.editAction = new UpdateAttributeAction(this.attributeType.code);
        }
      }

      this.viewModels = [];
      
      // First, we have to create a view for every ValueOverTime object. This is done to simply display what's currently
      // on the GeoObject
      if (this.attributeType.type === '_PARENT_')
      {
        this.hierarchy.entries.forEach((entry: HierarchyOverTimeEntry) => {
          let view = new VersionDiffView(this, this.editAction);
          
          view.oid = entry.oid;
          view.summaryKey = SummaryKey.UNMODIFIED;
          view.startDate = entry.startDate;
          view.endDate = entry.endDate;
          view.value = JSON.parse(JSON.stringify(entry));
          view.value.loading = {};
          
          view.editPropagator = new HierarchyEditPropagator(this, this.editAction, view, entry);
          
          this.viewModels.push(view);
        });
      }
      else
      {
        this.postGeoObject.attributes[this.attributeType.code].values.forEach((vot: ValueOverTime) => {
          let view = new VersionDiffView(this, this.editAction);
          
          view.oid = vot.oid;
          view.summaryKey = SummaryKey.UNMODIFIED;
          view.startDate = vot.startDate;
          view.endDate = vot.endDate;
          view.value = vot.value == null ? null : JSON.parse(JSON.stringify(vot.value));
          
          view.editPropagator.valueOverTime = vot;
          
          this.viewModels.push(view);
        });
      }
      
      // Next, we must apply all changes which may exist in the actions.
      let len = this.actions.length;
      for (let i = 0; i < len; ++i)
      {
        let action: AbstractAction = this.actions[i];
      
        if (action.actionType === ActionTypes.UPDATEATTRIBUTETACTION)
        {
          let updateAttrAction: UpdateAttributeAction = action as UpdateAttributeAction;
          
          if (this.attributeType.code === updateAttrAction.attributeName) {
  
            if (this.attributeType.type === '_PARENT_' && updateAttrAction.attributeDiff.hierarchyCode != this.hierarchy.code)
            {
              console.log("Skipping because not equal", updateAttrAction.attributeDiff.hierarchyCode, this.hierarchy.code);
              continue;
            }
  
            updateAttrAction.attributeDiff.valuesOverTime.forEach((votDiff: ValueOverTimeDiff) => {
  
              let view = this.findViewByOid(votDiff.oid);
  
              if (votDiff.action === "DELETE")
              {
                if (view == null)
                {
                  view = new VersionDiffView(this, action);
                  this.viewModels.push(view);
                  
                  view.conflictMessage = [{severity: "ERROR", message: this.lService.decode("changeovertime.manageVersions.missingReference"), type: ConflictType.MISSING_REFERENCE}];
                }
                
                this.populateViewFromDiff(view, votDiff);
                
                delete view.oldValue;
                delete view.oldStartDate;
                delete view.oldEndDate;
                
                //view.startDate = votDiff.oldStartDate;
                //view.endDate = votDiff.oldEndDate;
                //view.oid = votDiff.oid;
                //view.value = votDiff.oldValue;
                
                view.summaryKey = SummaryKey.DELETE;
                
                view.editPropagator.diff = votDiff;
              }
              else if (votDiff.action === "UPDATE")
              {
                if (view == null)
                {
                  view = new VersionDiffView(this, action);
                  this.viewModels.push(view);
                  
                  view.conflictMessage = [{severity: "ERROR", message: this.lService.decode("changeovertime.manageVersions.missingReference"), type: ConflictType.MISSING_REFERENCE}];
                }
                
                this.populateViewFromDiff(view, votDiff);
                
                view.calculateSummaryKey(votDiff);
              }
              else if (votDiff.action === "CREATE")
              {
                if (view != null)
                {
                  console.log("This action doesn't make sense. We're trying to create something that already exists?", votDiff)
                }
                else
                {
                  view = new VersionDiffView(this, action);
                  
                  this.populateViewFromDiff(view, votDiff);
                  
                  view.summaryKey = SummaryKey.NEW;
                  
                  this.viewModels.push(view);
                }
              }
  
            });
    
          }
        }
        else if (action.actionType === ActionTypes.CREATEGEOOBJECTACTION)
        {
          // TODO
          //let postGoVot = this.findPostGeoObjectVOT(votDiff.oid);
        }
        else
        {
          console.log("Unexpected action : " + action.actionType, action);
        }

      };
      
    }
    
    populateViewFromDiff(view: VersionDiffView, votDiff: ValueOverTimeDiff) {
      if (votDiff.newValue != null)
      {
        if (this.attributeType.type === "local") {
          view.value = votDiff.newValue;
        }
        else if (this.attributeType.type === "_PARENT_") {
          view.value = (view.editPropagator as HierarchyEditPropagator).createEmptyHierarchyEntry();
          view.value.oid = votDiff.oid;
          view.value.startDate = votDiff.newStartDate || votDiff.oldStartDate;
          view.value.endDate = votDiff.newEndDate || votDiff.oldEndDate;
          view.value.parents = votDiff.parents;
          
          view.value.loading = {};
          
          
          if (votDiff.oldValue != null)
          {
            let oldCodeArray: string[] = votDiff.oldValue.split("_~VST~_");
            let oldTypeCode: string = oldCodeArray[0];
            let oldGoCode: string = oldCodeArray[1];
            
            view.oldValue = oldGoCode;
          }
        }
        else
        {
          view.value = votDiff.newValue;
        }
        
        view.oldValue = votDiff.oldValue;
      }
      else
      {
        if (this.attributeType.type === "_PARENT_") {
          this.hierarchy.entries.forEach(entry => {
            if (entry.oid === votDiff.oid)
            {
              view.value = JSON.parse(JSON.stringify(entry));
            }
          });
          
          //if (votDiff.oldValue != null)
          //{
          //  let oldCodeArray: string[] = votDiff.oldValue.split("_~VST~_");
          //  let oldTypeCode: string = oldCodeArray[0];
          //  let oldGoCode: string = oldCodeArray[1];
            
          //  view.oldValue = oldGoCode;
          //}
        }
        else
        {
          view.value = votDiff.oldValue;
          
          //if (votDiff.oldValue != null)
          //{
          //  view.oldValue = votDiff.oldValue;
          //}
        }
      }
      
      view.oid = votDiff.oid;
      view.startDate = votDiff.newStartDate || votDiff.oldStartDate;
      view.endDate = votDiff.newEndDate || votDiff.oldEndDate;
      if (votDiff.newStartDate !== votDiff.oldStartDate)
      {
        view.oldStartDate = votDiff.newStartDate == null ? null : votDiff.oldStartDate;
      }
      if (votDiff.newEndDate !== votDiff.oldEndDate)
      {
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


                if (action.actionType === "UpdateAttributeAction") {
                    let uAction = action as UpdateAttributeAction;
                    
                    if(uAction.attributeName === this.attributeType.code) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    onApprove(): void
    {

        this.requestService.setActionStatus(this.editAction.oid, GovernanceStatus.ACCEPTED).then(results => {

            console.log("accepted");

        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });

    }

    onReject(): void {

        this.requestService.setActionStatus(this.editAction.oid, GovernanceStatus.REJECTED).then(results => {

            console.log("rejected");

        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });

    }

    onPending(): void {

        this.requestService.setActionStatus(this.editAction.oid, GovernanceStatus.PENDING).then(results => {

            console.log("pending");

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
      //view.layer.editing = !view.layer.editing;
      
      //if (this.geometryEditor != null)
      //{
      //  this.geometryEditor.reload();
      //}
      
      //this.geomService.setLayers(this.renderedLayers);
      
      if (view.isEditingGeometries)
      {
        this.geomService.stopEditing();
      }
      else
      {
        this.geomService.startEditing(view.editPropagator);
      }
      
      view.isEditingGeometries = !view.isEditingGeometries;
      
      if (this.geoObjectType.geometryType === 'POINT' || this.geoObjectType.geometryType === 'MULTIPOINT')
      {
        view.coordinate = {};
      }
    }
    
    toggleGeometryView(view: VersionDiffView) {
    
      // Using setTimeout() to pull the calc out of the animation stack so the dom can finish mutating before getting final height.
      setTimeout(() => { 
        this.mapRowHeight = this.elementRef.nativeElement.children[0].getElementsByClassName("attribute-element-wrapper")[0].offsetHeight;
      }, 0)
    
      if (view.isEditingGeometries)
      {
        this.toggleGeometryEditing(view);
      }
      
      view.isRenderingLayer = !view.isRenderingLayer;
      
      this.geomService.setLayers(this.getRenderedLayers());
    }
    
    manualCoordinateChange(view: VersionDiffView): void {
      
      const isLatitude = num => isFinite(num) && Math.abs(num) <= 90;
      const isLongitude = num => isFinite(num) && Math.abs(num) <= 180;
      
      view.coordinate.latValid = isLatitude(view.value.coordinates[0][1]);
      view.coordinate.longValid = isLongitude(view.value.coordinates[0][0]);
  
      if( !view.coordinate.latValid || !view.coordinate.longValid ){
        // outside EPSG bounds
        this.isValid = false;
        this.isValidChange.emit(this.isValid);
        return;
      }
      
      this.geomService.setPointCoordinates(view.value.coordinates[0][1], view.value.coordinates[0][0])
    }

}