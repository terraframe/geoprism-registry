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

import { GeoObjectType, AttributeType, AttributeOverTime, ValueOverTime, GeoObjectOverTime, AttributeTermType, PRESENT, ConflictMessage, HierarchyOverTime, HierarchyOverTimeEntry } from "@registry/model/registry";
import { CreateGeoObjectAction, UpdateAttributeAction, AbstractAction, ValueOverTimeDiff } from "@registry/model/crtable";
import { LocalizedValue } from "@shared/model/core";
import { ConflictType } from '@registry/model/constants';
import { AuthService } from "@shared/service/auth.service";

import { DateFieldComponent } from "../../../shared/component/form-fields/date-field/date-field.component";

import { RegistryService } from "@registry/service";
import { DateService } from "@shared/service/date.service";

import { LocalizationService } from "@shared/service";

import Utils from "../../utility/Utils";

export enum SummaryKey {
  NEW = "NEW",
  UNMODIFIED = "UNMODIFIED",
  DELETE = "DELETE",
  UPDATE = "UPDATE",
  TIME_CHANGE = "TIME_CHANGE",
  VALUE_CHANGE = "VALUE_CHANGE",
}

class ValueOverTimeEditPropagator {
  view: VersionDiffView;
  diff: ValueOverTimeDiff; // Any existing diff which may be associated with this view object.
  valueOverTime?: ValueOverTime; // Represents a vot on an existing GeoObject. If this is set and the action is UpdateAttribute, we must be doing an UPDATE, and valueOverTime represents the original value in the DB.
  action: AbstractAction;
  component: ManageVersionsComponent;
  
  constructor(component: ManageVersionsComponent, action: AbstractAction, view: VersionDiffView)
  {
    this.view = view;
    this.action = action;
    this.component = component;
  }
  
  get startDate()
  {
    return this.view.startDate;
  }
  
  set startDate(startDate: string)
  {
    if (this.action instanceof UpdateAttributeAction)
    {
      if (this.diff == null)
      {
        if (this.valueOverTime == null)
        {
          this.diff = new ValueOverTimeDiff();
          this.diff.action = "CREATE";
          this.action.attributeDiff.valuesOverTime.push(this.diff);
        }
        else
        {
          if (this.valueOverTime.startDate === startDate)
          {
            return;
          }
        
          this.diff = new ValueOverTimeDiff();
          this.diff.action = "UPDATE";
          this.diff.oid = this.valueOverTime.oid;
          this.diff.oldValue = this.valueOverTime.value;
          this.diff.oldStartDate = this.valueOverTime.startDate;
          this.diff.oldEndDate = this.valueOverTime.endDate;
          this.action.attributeDiff.valuesOverTime.push(this.diff);
        }
      }
      
      this.diff.newStartDate = startDate;
    }
    else if (this.action instanceof CreateGeoObjectAction)
    {
      this.valueOverTime.startDate = startDate;
    }
    
    this.view.startDate = startDate;
    
    this.component.onActionChange(this.action);
  }
  
  get endDate()
  {
    return this.view.endDate;
  }
  
  set endDate(endDate: string)
  {
    if (this.action instanceof UpdateAttributeAction)
    {
      if (this.diff == null)
      {
        if (this.valueOverTime == null)
        {
          this.diff = new ValueOverTimeDiff();
          this.diff.action = "CREATE";
          this.action.attributeDiff.valuesOverTime.push(this.diff);
        }
        else
        {
          if (this.valueOverTime.endDate === endDate)
          {
            return;
          }
        
          this.diff = new ValueOverTimeDiff();
          this.diff.action = "UPDATE";
          this.diff.oid = this.valueOverTime.oid;
          this.diff.oldValue = this.valueOverTime.value;
          this.diff.oldStartDate = this.valueOverTime.startDate;
          this.diff.oldEndDate = this.valueOverTime.endDate;
          this.action.attributeDiff.valuesOverTime.push(this.diff);
        }
      }
      
      this.diff.newEndDate = endDate;
    }
    else if (this.action instanceof CreateGeoObjectAction)
    {
      this.valueOverTime.endDate = endDate;
    }
    
    this.view.endDate = endDate;
    
    this.component.onActionChange(this.action);
  }
  
  get value()
  {
    return this.view.value;
  }
  
  set value(value: any)
  {
    if (value != null)
    {
      if (this.component.attributeType.type === "term")
      {
        value = [value];
      }
      else if (this.component.attributeType.type === "date")
      {
        value = new Date(value).getTime();
      }
    }
  
    if (this.action instanceof UpdateAttributeAction)
    {
      if (this.diff == null)
      {
        if (this.valueOverTime == null)
        {
          this.diff = new ValueOverTimeDiff();
          this.diff.action = "CREATE";
          this.action.attributeDiff.valuesOverTime.push(this.diff);
        }
        else
        {
          if (this.areValuesEqual(this.valueOverTime.value, value))
          {
            return;
          }
        
          this.diff = new ValueOverTimeDiff();
          this.diff.action = "UPDATE";
          this.diff.oid = this.valueOverTime.oid;
          this.diff.oldValue = this.valueOverTime.value;
          this.diff.oldStartDate = this.valueOverTime.startDate;
          this.diff.oldEndDate = this.valueOverTime.endDate;
          this.action.attributeDiff.valuesOverTime.push(this.diff);
        }
      }
      
      this.diff.newValue = value;
    }
    else if (this.action instanceof CreateGeoObjectAction)
    {
      this.valueOverTime.value = value;
    }
    
    this.view.value = value;
    
    this.component.onActionChange(this.action);
  }
  
  public setLocalizedValue(localeValue: {locale: string, value: string})
  {
    if (this.action instanceof UpdateAttributeAction)
    {
      if (this.diff == null)
      {
        if (this.valueOverTime == null)
        {
          this.diff = new ValueOverTimeDiff();
          this.diff.action = "CREATE";
          this.action.attributeDiff.valuesOverTime.push(this.diff);
        }
        else
        {
          let areValuesEqual: boolean = this.component.getValueAtLocale(this.valueOverTime.value, localeValue.locale) === this.component.getValueAtLocale(this.view.value, localeValue.locale)
        
          if (areValuesEqual)
          {
            return;
          }
        
          this.diff = new ValueOverTimeDiff();
          this.diff.action = "UPDATE";
          this.diff.oid = this.valueOverTime.oid;
          this.diff.oldValue = this.valueOverTime.value;
          this.diff.oldStartDate = this.valueOverTime.startDate;
          this.diff.oldEndDate = this.valueOverTime.endDate;
          this.action.attributeDiff.valuesOverTime.push(this.diff);
        }
      }
      
      this.diff.newValue = this.view.value;
    }
    else if (this.action instanceof CreateGeoObjectAction)
    {
      this.valueOverTime.value = this.view.value;
    }
    
    this.component.onActionChange(this.action);
  }
  
  private areValuesEqual(val1: any, val2: any): boolean
  {
    //if (this.component.attributeType.type === "local")
    //{
      // Not used anymore
    //}
    if (this.component.attributeType.type === "term")
    {
      return val1.length === val2.length && val1[0] === val2[0];
    }
    
    return val1 === val2;
  }
  
  public remove(): void
  {
    if (this.action instanceof UpdateAttributeAction)
    {
      if (this.valueOverTime != null && this.diff == null)
      {
        this.diff = new ValueOverTimeDiff();
        this.diff.action = "DELETE";
        this.diff.oid = this.valueOverTime.oid;
        this.diff.oldValue = this.valueOverTime.value;
        this.diff.oldStartDate = this.valueOverTime.startDate;
        this.diff.oldEndDate = this.valueOverTime.endDate;
        this.action.attributeDiff.valuesOverTime.push(this.diff);
      }
      else if (this.diff != null)
      {
        let index = -1;
        
        let len = this.action.attributeDiff.valuesOverTime.length;
        for (let i = 0; i < len; ++i)
        {
          let diff = this.action.attributeDiff.valuesOverTime[i];
        
          if (diff.oid === this.diff.oid)
          {
            index = i;
          }
        }
      
        if (index != -1)
        {
          this.action.attributeDiff.valuesOverTime.splice(index, 1);
        }
      }
    }
    else if (this.action instanceof CreateGeoObjectAction)
    {
      let votc = this.action.geoObjectJson.attributes[this.component.attributeType.code].values;
      
      let index = votc.findIndex((vot) => {return vot.oid === this.valueOverTime.oid});
      
      if (index != -1)
      {
        votc.splice(index, 1);
      }
    }
    
    this.component.onActionChange(this.action);
  }
}

class VersionDiffView extends ValueOverTime {
  summaryKey: SummaryKey;
  conflictMessage?: [ConflictMessage];
  oldValue?: any;
  oldStartDate?: string;
  oldEndDate?: string;
  editPropagator: ValueOverTimeEditPropagator;
  
  constructor(component: ManageVersionsComponent, action: AbstractAction)
  {
    super();
    this.editPropagator = new ValueOverTimeEditPropagator(component, action, this);
  }
}

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

    attributeType: AttributeType;
    actions: AbstractAction[] = [];

    // eslint-disable-next-line accessor-pairs
    @Input() set attributeData(value: {"attributeType":AttributeType, "actions":AbstractAction[], geoObject:GeoObjectOverTime}) {

        this.attributeType = value.attributeType;

        this.actions = value.actions;

        this.originalGeoObjectOverTime = JSON.parse(JSON.stringify(value.geoObject));
        this.postGeoObject = value.geoObject;

        if (this.attributeType.code === "geometry" && this.postGeoObject.attributes[this.attributeType.code].values.length === 1) {

            this.editingGeometry = 0;

        }
    }

    @Input() geoObjectType: GeoObjectType;

    originalGeoObjectOverTime: GeoObjectOverTime;
    postGeoObject: GeoObjectOverTime;

    @Input() isNewGeoObject: boolean = false;
    
    @Input() hierarchy: HierarchyOverTime = null;

    goGeometries: GeoObjectOverTime;

    newVersion: ValueOverTime;

    editingGeometry: number = -1;

    hasDuplicateDate: boolean = false;

    conflict: string;
    hasConflict: boolean = false;
    hasGap: boolean = false;

    originalAttributeState: AttributeType;
    
    viewModels: VersionDiffView[] = [];
    
    // The 'current' action which is to be used whenever we're applying new edits.
    editAction: AbstractAction;
    
    // eslint-disable-next-line no-useless-constructor
    constructor(private service: RegistryService, private lService: LocalizationService, public changeDetectorRef: ChangeDetectorRef, private dateService: DateService, private authService: AuthService) { }

    ngOnInit(): void {
      this.calculateViewModels();
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

            this.isValid = this.checkDateFieldValidity();

            this.hasConflict = this.dateService.checkRanges(this.viewModels);

        }, 0);

    }
    
    remove(view: VersionDiffView): void {

      view.editPropagator.remove();

      let position = -1;
      let len = this.viewModels.length;
      for (let i = 0; i < len; i++) {
          let loopView = this.viewModels[i];

          if (loopView.oid === view.oid) {
              position = i;
          }
      }

      if (position > -1) {
        this.viewModels.splice(position, 1);
      }
      
      this.onDateChange();

    }

    onAddNewVersion(): void {

        let view: VersionDiffView = new VersionDiffView(this, this.editAction);
        view.oid = this.generateUUID();

        if (this.attributeType.type === "local") {

            //   vot.value = {"localizedValue":null,"localeValues":[{"locale":"defaultLocale","value":null},{"locale":"km_KH","value":null}]};
            view.value = this.lService.create();

        } else if (this.attributeType.type === "geometry") {

          /*
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
            */

        } else if (this.attributeType.type === "term") {

            let terms = this.getGeoObjectTypeTermAttributeOptions(this.attributeType.code);

            if (terms && terms.length > 0) {

                view.value = terms[0].code;

            }

        }
        
        if (this.editAction instanceof CreateGeoObjectAction)
        {
          let vot = new ValueOverTime();
          vot.oid = this.generateUUID();
          
          this.editAction.geoObjectJson.attributes[this.attributeType.code].values.push(vot);
        
          view.editPropagator.valueOverTime = vot;
        }

        this.viewModels.push(view);

        /*
        if (this.attributeType.code === "geometry") {

            this.editingGeometry = votArr.length - 1;

        }
        */
        
        this.changeDetectorRef.detectChanges();

    }
    
    // https://stackoverflow.com/questions/105034/how-to-create-a-guid-uuid
    generateUUID() {
      return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
      });
    }

    editGeometry(index: number) {

        this.editingGeometry = index;

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
        
        if (updateAction.attributeDiff.valuesOverTime.length == 0)
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
        this.actions[0] = createAction;
        this.editAction = createAction;
      }
      else
      {
        this.actions.forEach((action: AbstractAction) => {
          if (action.actionType === 'UpdateAttributeAction')
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
      if (this.attributeType.code === '_PARENT_')
      {
        this.hierarchy.entries.forEach((entry: HierarchyOverTimeEntry) => {
          let view = new VersionDiffView(this, this.editAction);
          
          view.oid = entry.oid;
          view.summaryKey = SummaryKey.UNMODIFIED;
          view.startDate = entry.startDate;
          view.endDate = entry.endDate;
          
          // TODO
          //view.value = JSON.parse(JSON.stringify(entry.value));
          
          //view.editPropagator.valueOverTime = entry;
          
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
          view.value = JSON.parse(JSON.stringify(vot.value));
          
          view.editPropagator.valueOverTime = vot;
          
          this.viewModels.push(view);
        });
      }
      
      // Next, we must apply all changes which may exist in the actions.
      this.actions.forEach((action: AbstractAction) => {
      
        if (action.actionType === 'UpdateAttributeAction')
        {
          let updateAttrAction: UpdateAttributeAction = action as UpdateAttributeAction;
          
          if (this.attributeType.code === updateAttrAction.attributeName) {
  
            updateAttrAction.attributeDiff.valuesOverTime.forEach((votDiff: ValueOverTimeDiff) => {
  
              let view = this.findViewByOid(votDiff.oid);
  
              if (votDiff.action === "DELETE")
              {
                if (view == null)
                {
                  view = new VersionDiffView(this, action);
                  this.viewModels.push(view);
                  
                  view.conflictMessage = [{severity: "ERROR", message: "Could not find expected reference?", type: ConflictType.MISSING_REFERENCE}]; // TODO : Localize
                }
                
                delete view.oldValue;
                delete view.oldStartDate;
                delete view.oldEndDate;
                
                view.startDate = votDiff.oldStartDate;
                view.endDate = votDiff.oldEndDate;
                view.oid = votDiff.oid;
                view.value = votDiff.oldValue;
                
                view.summaryKey = SummaryKey.DELETE;
                
                view.editPropagator.diff = votDiff;
              }
              else if (votDiff.action === "UPDATE")
              {
                if (view == null)
                {
                  view = new VersionDiffView(this, action);
                  this.viewModels.push(view);
                  
                  view.conflictMessage = [{severity: "ERROR", message: "Could not find expected reference?", type: ConflictType.MISSING_REFERENCE}]; // TODO : Localize
                }
                
                view.oid = votDiff.oid;
                
                if (votDiff.newStartDate != null)
                {
                  view.startDate = votDiff.newStartDate;
                }
                if (votDiff.newEndDate != null)
                {
                  view.endDate = votDiff.newEndDate;
                }
                
                if (votDiff.newValue != null)
                {
                  view.value = votDiff.newValue;
                }
                
                view.oldStartDate = votDiff.oldStartDate;
                view.oldEndDate = votDiff.oldEndDate;
                view.oldValue = votDiff.oldValue;
                view.editPropagator.diff = votDiff;
                
                let hasTime = votDiff.newStartDate != null || votDiff.newEndDate != null;
                let hasValue = votDiff.newValue != null;
                
                if (hasTime && hasValue)
                {
                  view.summaryKey = SummaryKey.UPDATE;
                }
                else if (hasTime)
                {
                  view.summaryKey = SummaryKey.TIME_CHANGE;
                }
                else if (hasValue)
                {
                  view.summaryKey = SummaryKey.VALUE_CHANGE;
                }
                else
                {
                  view.summaryKey = SummaryKey.UPDATE;
                }
              }
              else if (votDiff.action === "CREATE")
              {
                if (view != null)
                {
                  // This action doesn't make sense. We're trying to create something that already exists?
                }
                else
                {
                  view = new VersionDiffView(this, action);
                  
                  view.oid = votDiff.oid;
                  view.startDate = votDiff.newStartDate;
                  view.endDate = votDiff.newEndDate;
                  view.oldStartDate = votDiff.oldStartDate;
                  view.oldEndDate = votDiff.oldEndDate;
                  view.value = votDiff.newValue;
                  view.oldValue = votDiff.oldValue;
                  view.summaryKey = SummaryKey.NEW;
                  
                  view.editPropagator.diff = votDiff;
                  
                  this.viewModels.push(view);
                }
              }
  
            });
    
          }
        }
        else if (action.actionType === 'CreateGeoObjectAction')
        {
          // TODO
          //let postGoVot = this.findPostGeoObjectVOT(votDiff.oid);
        }
        else
        {
          console.log("Unexpected action : " + action.actionType, action);
        }

      });

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

    onApprove(): void {

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