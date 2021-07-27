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

import { GeoObjectType, AttributeType, AttributeOverTime, ValueOverTime, GeoObjectOverTime, AttributeTermType, PRESENT } from "@registry/model/registry";
import { CreateGeoObjectAction, UpdateAttributeAction, AbstractAction, ValueOverTimeDiff } from "@registry/model/crtable";
import { LocalizedValue } from "@shared/model/core";

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
  valueOverTime?: ValueOverTime;
  diff?: ValueOverTimeDiff;
  
  set newStartDate(oldStartDate: string)
  {
    if (this.diff != null)
    {
      this.diff.oldStartDate = oldStartDate;
    }
  }
}

class VersionDiffView {
  summaryKey: SummaryKey;
  conflictMessage?: [{message: string, type: string}]; // 
  oid: string;
  oldValue?: any;
  newValue?: any
  oldStartDate?: string;
  newStartDate?: string;
  oldEndDate?: string;
  newEndDate?: string;
  versionEditPropagator?: ValueOverTimeEditPropagator; // If this is null, then it means we had some kind of critical problem (either the oid reference is out of date or something) and this diff/action is not valid
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
    @Input() set attributeData(value: {"attributeType":AttributeType, "actions":CreateGeoObjectAction[] | UpdateAttributeAction[], geoObject:GeoObjectOverTime}) {

        this.attributeType = value.attributeType;

        this.actions = value.actions;

        this.originalGeoObjectOverTime = JSON.parse(JSON.stringify(value.geoObject));
        this.postGeoObject = value.geoObject;

        if (this.attributeType.code === "geometry" && this.postGeoObject.attributes[this.attributeType.code].values.length === 1) {

            this.editingGeometry = 0;

        }
        
        this.calculateViewModels();
    }

    @Input() geoObjectType: GeoObjectType;

    originalGeoObjectOverTime: GeoObjectOverTime;
    postGeoObject: GeoObjectOverTime;

    @Input() isNewGeoObject: boolean = false;

    goGeometries: GeoObjectOverTime;

    newVersion: ValueOverTime;

    editingGeometry: number = -1;

    hasDuplicateDate: boolean = false;

    conflict: string;
    hasConflict: boolean = false;
    hasGap: boolean = false;

    originalAttributeState: AttributeType;
    
    viewModels: VersionDiffView[] = [];
    
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

            let vAttributes = this.postGeoObject.attributes[this.attributeType.code].values;

            this.isValid = this.checkDateFieldValidity();

            this.hasConflict = this.dateService.checkRanges(vAttributes);

        }, 0);

    }

    onAddNewVersion(): void {

        let votArr: ValueOverTimeDiff[] = this.postGeoObject.attributes[this.attributeType.code].values;

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

    remove(version: any): void {

        let val = this.postGeoObject.attributes[this.attributeType.code];

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
    
    findViewByOid(oid: string): VersionDiffView
    {
      this.viewModels.forEach( (view: VersionDiffView) => {
        if (view.oid === oid)
        {
          return view;
        }
      });
      
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

      this.viewModels = [];
      
      // First, we have to create a view for every ValueOverTime object. This is done to simply display what's currently
      // on the GeoObject
      this.postGeoObject.attributes[this.attributeType.code].values.forEach((vot: ValueOverTime) => {
        let view = new VersionDiffView();
        
        view.oid = vot.oid;
        view.summaryKey = SummaryKey.UNMODIFIED;
        view.newStartDate = vot.startDate;
        view.newEndDate = vot.endDate;
        view.newValue = vot.value;
        
        // TODO
        //view.versionEditPropagator = new ValueOverTimeEditPropagator();
        //view.versionEditPropagator.diff = votDiff;
        
        this.viewModels.push(view);
      });
      
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
                  view = new VersionDiffView();
                  this.viewModels.push(view);
                  
                  view.conflictMessage = [{type: "ERROR", message: "Could not find expected reference?"}]; // TODO : Localize
                }
                
                delete view.oldValue;
                delete view.oldStartDate;
                delete view.oldEndDate;
                
                view.newStartDate = votDiff.oldStartDate;
                view.newEndDate = votDiff.oldEndDate;
                view.oid = votDiff.oid;
                view.newValue = votDiff.oldValue;
                
                view.summaryKey = SummaryKey.DELETE;
                
                view.versionEditPropagator = new ValueOverTimeEditPropagator();
                view.versionEditPropagator.diff = votDiff;
              }
              else if (votDiff.action === "UPDATE")
              {
                if (view == null)
                {
                  view = new VersionDiffView();
                  this.viewModels.push(view);
                  
                  view.conflictMessage = [{type: "ERROR", message: "Could not find expected reference?"}]; // TODO : Localize
                }
                
                view.oid = votDiff.oid;
                view.newStartDate = votDiff.newStartDate;
                view.newEndDate = votDiff.newEndDate;
                view.oldStartDate = votDiff.oldStartDate;
                view.oldEndDate = votDiff.oldEndDate;
                view.newValue = votDiff.newValue;
                view.oldValue = votDiff.oldValue;
                
                view.versionEditPropagator = new ValueOverTimeEditPropagator();
                view.versionEditPropagator.diff = votDiff;
                
                let hasTime = view.newStartDate != null || view.newEndDate != null;
                let hasValue = view.newValue != null;
                
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
                  view.versionEditPropagator = null;
                }
                else
                {
                  view = new VersionDiffView();
                  
                  view.oid = votDiff.oid;
                  view.newStartDate = votDiff.newStartDate;
                  view.newEndDate = votDiff.newEndDate;
                  view.oldStartDate = votDiff.oldStartDate;
                  view.oldEndDate = votDiff.oldEndDate;
                  view.newValue = votDiff.newValue;
                  view.oldValue = votDiff.oldValue;
                  
                  view.versionEditPropagator = new ValueOverTimeEditPropagator();
                  view.versionEditPropagator.diff = votDiff;
                  
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