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

import { Observable } from 'rxjs';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';
import { GeoObjectType, AttributeType, ValueOverTime, GeoObjectOverTime, GeoObject, AttributeTermType, ConflictMessage, HierarchyOverTime, HierarchyOverTimeEntry, HierarchyOverTimeEntryParent, SummaryKey } from "@registry/model/registry";
import { CreateGeoObjectAction, UpdateAttributeAction, AbstractAction, ValueOverTimeDiff } from "@registry/model/crtable";
import { LocalizedValue } from "@shared/model/core";
import { ConflictType, ActionTypes, GovernanceStatus } from '@registry/model/constants';
import { AuthService } from "@shared/service/auth.service";

import { DateFieldComponent } from "../../../shared/component/form-fields/date-field/date-field.component";

import { RegistryService, GeometryService } from "@registry/service";
import { ChangeRequestService } from "@registry/service/change-request.service";
import { DateService } from "@shared/service/date.service";

import { LocalizationService } from "@shared/service";

import Utils from "../../utility/Utils";

import turf_booleanequal from '@turf/boolean-equal';

export class ValueOverTimeEditPropagator {
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
  
  get oid(): string
  {
    return this.view.oid;
  }
  
  get startDate()
  {
    return this.view.startDate;
  }
  
  set startDate(startDate: string)
  {
    if (this.action.actionType === 'UpdateAttributeAction')
    {
      if (this.diff == null)
      {
        if (this.valueOverTime == null)
        {
          this.diff = new ValueOverTimeDiff();
          this.diff.action = "CREATE";
          (this.action as UpdateAttributeAction).attributeDiff.valuesOverTime.push(this.diff);
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
          (this.action as UpdateAttributeAction).attributeDiff.valuesOverTime.push(this.diff);
        }
      }
      
      if (startDate === this.diff.oldStartDate)
      {
        delete this.diff.newStartDate;
        delete this.view.oldStartDate;
      }
      else
      {
        this.diff.newStartDate = startDate;
        this.view.oldStartDate = this.diff.oldStartDate;
      }
    }
    else if (this.action.actionType === 'CreateGeoObjectAction')
    {
      this.valueOverTime.startDate = startDate;
    }
    
    this.view.startDate = startDate;
    
    this.view.calculateSummaryKey(this.diff);
    
    this.component.onActionChange(this.action);
  }
  
  get endDate()
  {
    return this.view.endDate;
  }
  
  set endDate(endDate: string)
  {
    if (this.action.actionType === 'UpdateAttributeAction')
    {
      if (this.diff == null)
      {
        if (this.valueOverTime == null)
        {
          this.diff = new ValueOverTimeDiff();
          this.diff.action = "CREATE";
          (this.action as UpdateAttributeAction).attributeDiff.valuesOverTime.push(this.diff);
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
          (this.action as UpdateAttributeAction).attributeDiff.valuesOverTime.push(this.diff);
        }
      }
      
      if (endDate === this.diff.oldEndDate)
      {
        delete this.diff.newEndDate;
        delete this.view.oldEndDate;
      }
      else
      {
        this.diff.newEndDate = endDate;
        this.view.oldEndDate = this.diff.oldEndDate;
      }
    }
    else if (this.action.actionType === 'CreateGeoObjectAction')
    {
      this.valueOverTime.endDate = endDate;
    }
    
    this.view.endDate = endDate;
    
    this.view.calculateSummaryKey(this.diff);
    
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
  
    if (this.action.actionType === 'UpdateAttributeAction')
    {
      if (this.diff == null)
      {
        if (this.valueOverTime == null)
        {
          this.diff = new ValueOverTimeDiff();
          this.diff.action = "CREATE";
          (this.action as UpdateAttributeAction).attributeDiff.valuesOverTime.push(this.diff);
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
          (this.action as UpdateAttributeAction).attributeDiff.valuesOverTime.push(this.diff);
        }
      }
      
      if (this.areValuesEqual(this.diff.oldValue, value))
      {
        delete this.diff.newValue;
        delete this.view.oldValue;
      }
      else
      {
        this.diff.newValue = value;
        this.view.oldValue = this.diff.oldValue;
      }
    }
    else if (this.action.actionType === 'CreateGeoObjectAction')
    {
      this.valueOverTime.value = value;
    }
    
    this.view.value = value;
    
    this.view.calculateSummaryKey(this.diff);
    
    this.component.onActionChange(this.action);
  }
  
  public setLocalizedValue(localeValue: {locale: string, value: string})
  {
    if (this.action.actionType === 'UpdateAttributeAction')
    {
      if (this.diff == null)
      {
        if (this.valueOverTime == null)
        {
          this.diff = new ValueOverTimeDiff();
          this.diff.action = "CREATE";
          (this.action as UpdateAttributeAction).attributeDiff.valuesOverTime.push(this.diff);
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
          (this.action as UpdateAttributeAction).attributeDiff.valuesOverTime.push(this.diff);
        }
      }
      
      let areValuesEqual: boolean = false;
      if (this.diff.oldValue != null)
      {
        areValuesEqual = this.component.getValueAtLocale(this.diff.oldValue, localeValue.locale) === this.component.getValueAtLocale(this.view.value, localeValue.locale);
      }
      else
      {
        areValuesEqual = this.diff.oldValue === this.component.getValueAtLocale(this.view.value, localeValue.locale);
      }
      
      if (areValuesEqual)
      {
        delete this.diff.newValue;
        delete this.view.oldValue;
      }
      else
      {
        this.diff.newValue = this.view.value;
        this.view.oldValue = this.diff.oldValue;
      }
    }
    else if (this.action.actionType === 'CreateGeoObjectAction')
    {
      this.valueOverTime.value = this.view.value;
    }
    
    this.view.calculateSummaryKey(this.diff);
    
    this.component.onActionChange(this.action);
  }
  
  areValuesEqual(val1: any, val2: any): boolean
  {
    if (!val1 && !val2) {
      return true;
    }
    else if ( (!val1 && val2) || (!val2 && val1 ) ) {
      return false;
    }
  
    // TODO Test all attribute types here. We might need a case for geometries here.
    
    //if (this.component.attributeType.type === "local")
    //{
      // Not used anymore
    //}
    if (this.component.attributeType.type === "term")
    {
      return val1.length === val2.length && val1[0] === val2[0];
    }
    else if (this.component.attributeType.type === 'geometry')
    {
      return turf_booleanequal(val1, val2);
    }
    
    return val1 === val2;
  }
  
  public remove(): void
  {
    if (this.action.actionType === 'UpdateAttributeAction')
    {
      if (this.valueOverTime != null && this.diff == null)
      {
        this.diff = new ValueOverTimeDiff();
        this.diff.action = "DELETE";
        this.diff.oid = this.valueOverTime.oid;
        this.diff.oldValue = this.valueOverTime.value;
        this.diff.oldStartDate = this.valueOverTime.startDate;
        this.diff.oldEndDate = this.valueOverTime.endDate;
        (this.action as UpdateAttributeAction).attributeDiff.valuesOverTime.push(this.diff);
      }
      else if (this.diff != null)
      {
        if (this.diff.action === 'DELETE')
        {
          let index = (this.action as UpdateAttributeAction).attributeDiff.valuesOverTime.findIndex(diff => {return diff.oid === this.diff.oid});
        
          if (index != -1)
          {
            (this.action as UpdateAttributeAction).attributeDiff.valuesOverTime.splice(index, 1);
            this.diff = null;
          }
        }
        else if (this.valueOverTime != null)
        {
          this.diff.action = "DELETE";
          this.diff.oid = this.valueOverTime.oid;
          delete this.diff.newValue;
          delete this.diff.newStartDate;
          delete this.diff.newEndDate;
          this.diff.oldValue = this.valueOverTime.value;
          this.diff.oldStartDate = this.valueOverTime.startDate;
          this.diff.oldEndDate = this.valueOverTime.endDate;
          
          this.view.startDate = this.diff.oldStartDate;
          this.view.endDate = this.diff.oldEndDate;
          this.view.value = this.diff.oldValue;
          delete this.view.oldStartDate;
          delete this.view.oldEndDate;
          delete this.view.oldValue;
        }
      }
    }
    else if (this.action.actionType === 'CreateGeoObjectAction')
    {
      let votc = (this.action as CreateGeoObjectAction).geoObjectJson.attributes[this.component.attributeType.code].values;
      
      let index = votc.findIndex((vot) => {return vot.oid === this.valueOverTime.oid});
      
      if (index !== -1)
      {
        votc.splice(index, 1);
      }
    }
    
    this.view.calculateSummaryKey(this.diff);
    
    this.component.onActionChange(this.action);
  }
  
  public onAddNewVersion(): void {
    if (this.component.attributeType.type === "local") {

        //   vot.value = {"localizedValue":null,"localeValues":[{"locale":"defaultLocale","value":null},{"locale":"km_KH","value":null}]};
        this.view.value = this.component.lService.create();

    } else if (this.component.attributeType.type === "geometry") {

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

    } else if (this.component.attributeType.type === "term") {

        let terms = this.component.getGeoObjectTypeTermAttributeOptions(this.component.attributeType.code);

        if (terms && terms.length > 0) {

            this.view.value = terms[0].code;

        }

    }
    
    this.view.summaryKey = SummaryKey.NEW;
    
    if (this.component.editAction instanceof CreateGeoObjectAction)
    {
      let vot = new ValueOverTime();
      vot.oid = this.component.generateUUID();
      
      this.component.editAction.geoObjectJson.attributes[this.component.attributeType.code].values.push(vot);
    
      this.valueOverTime = vot;
    }
  }
}

export class HierarchyEditPropagator extends ValueOverTimeEditPropagator {
  hierarchyEntry: HierarchyOverTimeEntry;

  constructor(component: ManageVersionsComponent, action: AbstractAction, view: VersionDiffView, hierarchyEntry: HierarchyOverTimeEntry)
  {
    super(component, action, view);
    this.hierarchyEntry = hierarchyEntry;
    
    if (this.hierarchyEntry != null)
    {
      this.hierarchyEntry.loading = {};
    }
  }
  
  set startDate(startDate: string)
  {
    if (this.action.actionType === 'UpdateAttributeAction')
    {
      if (this.diff == null)
      {
        if (this.hierarchyEntry == null)
        {
          this.diff = new ValueOverTimeDiff();
          this.diff.action = "CREATE";
          (this.action as UpdateAttributeAction).attributeDiff.hierarchyCode = this.component.hierarchy.code;
          (this.action as UpdateAttributeAction).attributeDiff.valuesOverTime.push(this.diff);
        }
        else
        {
          if (this.hierarchyEntry.startDate === startDate)
          {
            return;
          }
          
          let immediateParent: GeoObject = this.hierarchyEntry.parents[this.component.hierarchy.types[this.component.hierarchy.types.length-1].code].geoObject;
          let oldValue: string = immediateParent == null ? null : immediateParent.properties.type + "_~VST~_" + immediateParent.properties.code;
        
          this.diff = new ValueOverTimeDiff();
          this.diff.action = "UPDATE";
          this.diff.oid = this.hierarchyEntry.oid;
          this.diff.oldValue = oldValue;
          this.diff.oldStartDate = this.hierarchyEntry.startDate;
          this.diff.oldEndDate = this.hierarchyEntry.endDate;
          (this.action as UpdateAttributeAction).attributeDiff.hierarchyCode = this.component.hierarchy.code;
          (this.action as UpdateAttributeAction).attributeDiff.valuesOverTime.push(this.diff);
        }
      }
      
      if (startDate === this.diff.oldStartDate)
      {
        delete this.diff.newStartDate;
        delete this.view.oldStartDate;
      }
      else
      {
        this.diff.newStartDate = startDate;
        this.view.oldStartDate = this.diff.oldStartDate;
      }
    }
    else if (this.action.actionType === 'CreateGeoObjectAction')
    {
      this.hierarchyEntry.startDate = startDate;
    }
    
    this.view.startDate = startDate;
    
    this.view.calculateSummaryKey(this.diff);
    
    this.component.onActionChange(this.action);
  }
  
  get startDate()
  {
    return this.view.startDate;
  }
  
  set endDate(endDate: string)
  {
    if (this.action.actionType === 'UpdateAttributeAction')
    {
      if (this.diff == null)
      {
        if (this.hierarchyEntry == null)
        {
          this.diff = new ValueOverTimeDiff();
          this.diff.action = "CREATE";
          (this.action as UpdateAttributeAction).attributeDiff.hierarchyCode = this.component.hierarchy.code;
          (this.action as UpdateAttributeAction).attributeDiff.valuesOverTime.push(this.diff);
        }
        else
        {
          if (this.hierarchyEntry.endDate === endDate)
          {
            return;
          }
          
          let immediateParent: GeoObject = this.hierarchyEntry.parents[this.component.hierarchy.types[this.component.hierarchy.types.length-1].code].geoObject;
          let oldValue: string = immediateParent == null ? null : immediateParent.properties.type + "_~VST~_" + immediateParent.properties.code;
        
          this.diff = new ValueOverTimeDiff();
          this.diff.action = "UPDATE";
          this.diff.oid = this.hierarchyEntry.oid;
          this.diff.oldValue = oldValue;
          this.diff.oldStartDate = this.hierarchyEntry.startDate;
          this.diff.oldEndDate = this.hierarchyEntry.endDate;
          (this.action as UpdateAttributeAction).attributeDiff.hierarchyCode = this.component.hierarchy.code;
          (this.action as UpdateAttributeAction).attributeDiff.valuesOverTime.push(this.diff);
        }
      }
      
      if (endDate === this.diff.oldEndDate)
      {
        delete this.diff.newEndDate;
        delete this.view.oldEndDate;
      }
      else
      {
        this.diff.newEndDate = endDate;
        this.view.oldEndDate = this.diff.oldEndDate;
      }
    }
    else if (this.action.actionType === 'CreateGeoObjectAction')
    {
      this.hierarchyEntry.endDate = endDate;
    }
    
    this.view.endDate = endDate;
    
    this.view.calculateSummaryKey(this.diff);
    
    this.component.onActionChange(this.action);
  }
  
  get endDate()
  {
    return this.view.endDate;
  }
  
  setParentValue(parent: HierarchyOverTimeEntryParent)
  {
    let incomingImmediateParent: GeoObject = parent.geoObject;
    let immediateType: string = this.component.hierarchy.types[this.component.hierarchy.types.length-1].code;
  
    if (this.action.actionType === 'UpdateAttributeAction')
    {
      if (this.diff == null)
      {
        if (this.hierarchyEntry == null)
        {
          this.diff = new ValueOverTimeDiff();
          this.diff.action = "CREATE";
          (this.action as UpdateAttributeAction).attributeDiff.hierarchyCode = this.component.hierarchy.code;
          (this.action as UpdateAttributeAction).attributeDiff.valuesOverTime.push(this.diff);
        }
        else
        {
          let currentImmediateParent: GeoObject = this.hierarchyEntry.parents[immediateType].geoObject;
          let oldValue: string = currentImmediateParent == null ? null : currentImmediateParent.properties.type + "_~VST~_" + currentImmediateParent.properties.code;
        
          if (
            (currentImmediateParent == null && incomingImmediateParent == null)
            || ((currentImmediateParent != null && incomingImmediateParent != null)
            && currentImmediateParent.properties.code === incomingImmediateParent.properties.code))
          {
            return;
          }
          
          this.diff = new ValueOverTimeDiff();
          this.diff.action = "UPDATE";
          this.diff.oid = this.hierarchyEntry.oid;
          this.diff.oldValue = oldValue;
          this.diff.oldStartDate = this.valueOverTime.startDate;
          this.diff.oldEndDate = this.valueOverTime.endDate;
          (this.action as UpdateAttributeAction).attributeDiff.valuesOverTime.push(this.diff);
        }
      }
      
      let newValueStrConcat: string = incomingImmediateParent.properties.type + "_~VST~_" + incomingImmediateParent.properties.code
      
      if (newValueStrConcat === this.diff.oldValue)
      {
        delete this.diff.newValue;
        delete this.view.oldValue;
      }
      else
      {
        this.diff.newValue = newValueStrConcat;
        this.view.oldValue = this.diff.oldValue == null ? null : this.diff.oldValue.split("_~VST~_")[1];
      }
    }
    else if (this.action.actionType === 'CreateGeoObjectAction')
    {
      this.hierarchyEntry.parents[immediateType] = parent;
    }
    
    this.view.value.parents[parent.geoObject.properties.type] = parent;
    
    this.view.calculateSummaryKey(this.diff);
    
    this.component.onActionChange(this.action);
  }
  
  set value(val: any)
  {
    this.view.value = val;
  }
  
  get value()
  {
    return this.view.value;
  }
  
  public removeType(type): void
  {
    this.view.value.parents[type.code].text = '';
    delete this.view.value.parents[type.code].geoObject;
    delete this.view.value.parents[type.code].goCode;
  }
  
  getTypeAheadObservable(date: string, type: any, entry: any, index: number): Observable<any> {

    let geoObjectTypeCode = type.code;

    let parentCode = null;
    let parentTypeCode = null;
    let hierarchyCode = null;

    if (index > 0) {
      let pType = this.component.hierarchy.types[index - 1];
      const parent = entry.parents[pType.code];

      if (parent.geoObject != null && parent.geoObject.properties.code != null) {
        hierarchyCode = this.component.hierarchy.code;
        parentCode = parent.geoObject.properties.code;
        parentTypeCode = parent.geoObject.properties.type;
      }
    }

    return Observable.create((observer: any) => {
      if (parentCode == null)
      {
        let loopI = index;
      
        while (parentCode == null && loopI > 0)
        {
          loopI = loopI - 1;
          
          let parent = entry.parents[this.component.hierarchy.types[loopI].code];
          
          if (parent != null)
          {
            if (parent.geoObject != null && parent.geoObject.properties.code != null)
            {
              parentCode = parent.geoObject.properties.code;
              hierarchyCode = this.component.hierarchy.code;
              parentTypeCode = this.component.hierarchy.types[loopI].code;
            }
            else if (parent.goCode != null)
            {
              parentCode = parent.goCode;
              hierarchyCode = this.component.hierarchy.code;
              parentTypeCode = this.component.hierarchy.types[loopI].code;
            }
          }
        }
      }
    
      this.component.service.getGeoObjectSuggestions(entry.parents[type.code].text, geoObjectTypeCode, parentCode, parentTypeCode, hierarchyCode, date).then(results => {
        observer.next(results);
      });
    });
  }

  typeaheadOnSelect(e: TypeaheadMatch, type: any, entry: any, date: string): void {
    //        let ptn: ParentTreeNode = parent.ptn;

    entry.parents[type.code].text = e.item.name + " : " + e.item.code;
    entry.parents[type.code].goCode = e.item.code;

    let parentTypes = [];

    for (let i = 0; i < this.component.hierarchy.types.length; i++) {
      let current = this.component.hierarchy.types[i];

      parentTypes.push(current.code);

      if (current.code === type.code) {
        break;
      }
    }

    this.component.service.getParentGeoObjects(e.item.uid, type.code, parentTypes, true, date).then(ancestors => {

      delete entry.parents[type.code].goCode;
      entry.parents[type.code].geoObject = ancestors.geoObject;
      entry.parents[type.code].text = ancestors.geoObject.properties.displayLabel.localizedValue + ' : ' + ancestors.geoObject.properties.code;
      
      this.setParentValue(entry.parents[type.code]);

      for (let i = 0; i < this.component.hierarchy.types.length; i++) {
        let current = this.component.hierarchy.types[i];
        let ancestor = ancestors;

        while (ancestor != null && ancestor.geoObject.properties.type != current.code) {
          if (ancestor.parents.length > 0) {
            ancestor = ancestor.parents[0];
          }
          else {
            ancestor = null;
          }
        }

        if (ancestor != null) {
          entry.parents[current.code].geoObject = ancestor.geoObject;
          entry.parents[current.code].text = ancestor.geoObject.properties.displayLabel.localizedValue + ' : ' + ancestor.geoObject.properties.code;
        }
      }
      
      this.diff.parents = entry.parents;

    });
  }
  
  createEmptyHierarchyEntry(): HierarchyOverTimeEntry {
    let hierarchyEntry = new HierarchyOverTimeEntry();
    hierarchyEntry.loading = {};
    hierarchyEntry.oid = this.component.generateUUID();
    
    hierarchyEntry.parents = {};
    
    for (let i = 0; i < this.component.hierarchy.types.length; i++) {
      let current = this.component.hierarchy.types[i];

      hierarchyEntry.parents[current.code] = { text: '', geoObject: null };

      hierarchyEntry.loading = {};
    }
    
    return hierarchyEntry;
  }
  
  onAddNewVersion(): void {
    this.view.summaryKey = SummaryKey.NEW;
  
    this.view.value = this.createEmptyHierarchyEntry();
  
    if (this.component.editAction instanceof CreateGeoObjectAction)
    {
      this.hierarchyEntry = this.createEmptyHierarchyEntry();
    
      this.component.editAction.parentJson.entries.push(this.hierarchyEntry);
    }
  }
  
  public remove(): void
  {
    let immediateType: string = this.component.hierarchy.types[this.component.hierarchy.types.length-1].code;
  
    if (this.action.actionType === 'UpdateAttributeAction')
    {
      if (this.hierarchyEntry != null && this.diff == null)
      {
        let currentImmediateParent: GeoObject = this.hierarchyEntry.parents[immediateType].geoObject;
        let oldValue: string = currentImmediateParent == null ? null : currentImmediateParent.properties.type + "_~VST~_" + currentImmediateParent.properties.code;
      
        this.diff = new ValueOverTimeDiff();
        this.diff.action = "DELETE";
        this.diff.oid = this.hierarchyEntry.oid;
        this.diff.oldValue = oldValue;
        this.diff.oldStartDate = this.hierarchyEntry.startDate;
        this.diff.oldEndDate = this.hierarchyEntry.endDate;
        (this.action as UpdateAttributeAction).attributeDiff.valuesOverTime.push(this.diff);
        (this.action as UpdateAttributeAction).attributeDiff.hierarchyCode = this.component.hierarchy.code;
      }
      else if (this.diff != null)
      {
        if (this.diff.action === 'DELETE')
        {
          let index = (this.action as UpdateAttributeAction).attributeDiff.valuesOverTime.findIndex(diff => {return diff.oid === this.diff.oid});
        
          if (index != -1)
          {
            (this.action as UpdateAttributeAction).attributeDiff.valuesOverTime.splice(index, 1);
            this.diff = null;
          }
        }
        else if (this.hierarchyEntry != null)
        {
          let currentImmediateParent: GeoObject = this.hierarchyEntry.parents[immediateType].geoObject;
          let oldValue: string = currentImmediateParent == null ? null : currentImmediateParent.properties.type + "_~VST~_" + currentImmediateParent.properties.code;
        
          this.diff.action = "DELETE";
          this.diff.oid = this.hierarchyEntry.oid;
          delete this.diff.newValue;
          delete this.diff.newStartDate;
          delete this.diff.newEndDate;
          this.diff.oldValue = oldValue;
          this.diff.oldStartDate = this.hierarchyEntry.startDate;
          this.diff.oldEndDate = this.hierarchyEntry.endDate;
          
          this.view.startDate = this.diff.oldStartDate;
          this.view.endDate = this.diff.oldEndDate;
          this.view.value = this.diff.oldValue;
          delete this.view.oldStartDate;
          delete this.view.oldEndDate;
          delete this.view.oldValue;
        }
      }
    }
    else if (this.action.actionType === 'CreateGeoObjectAction')
    {
      let votc = (this.action as CreateGeoObjectAction).parentJson.entries[immediateType];
      
      let index = votc.findIndex((vot) => {return vot.oid === this.hierarchyEntry.oid});
      
      if (index != -1)
      {
        votc.splice(index, 1);
      }
    }
    
    this.view.calculateSummaryKey(this.diff);
    
    this.component.onActionChange(this.action);
  }
}

/*
 * This class exists purely for the purpose of storing what data to be rendered to the front-end. Any storage or submission of this data to the back-end must be translated
 * using the edit propagator.
 */
class VersionDiffView extends ValueOverTime {
  component: ManageVersionsComponent;
  summaryKeyData: SummaryKey;
  summaryKeyLocalized: string; // If we try to localize this in the html with a localize element then it won't update as frequently as we need so we're doing stuff manually here.
  conflictMessage?: [ConflictMessage];
  oldValue?: any;
  oldStartDate?: string;
  oldEndDate?: string;
  isEditingGeometries: boolean = false;
  isRenderingLayer: boolean = false;
  editPropagator: ValueOverTimeEditPropagator;
  
  constructor(component: ManageVersionsComponent, action: AbstractAction)
  {
    super();
    
    this.component = component;
    
    if (component.attributeType.type === '_PARENT_')
    {
      this.editPropagator = new HierarchyEditPropagator(component, action, this, null);
    }
    else
    {
      this.editPropagator = new ValueOverTimeEditPropagator(component, action, this);
    }
  }
  
  calculateSummaryKey(diff: ValueOverTimeDiff)
  {
    if (diff == null)
    {
      this.summaryKey = SummaryKey.UNMODIFIED;
      return;
    }
    
    if (diff.action === 'CREATE')
    {
      this.summaryKey = SummaryKey.NEW;
      return;
    }
    else if (diff.action === 'DELETE')
    {
      this.summaryKey = SummaryKey.DELETE;
      return;
    }
    
    let hasTime = diff.newStartDate != null || diff.newEndDate != null;
    let hasValue = diff.newValue != null;
    
    if (hasTime && hasValue)
    {
      this.summaryKey = SummaryKey.UPDATE;
    }
    else if (hasTime)
    {
      this.summaryKey = SummaryKey.TIME_CHANGE;
    }
    else if (hasValue)
    {
      this.summaryKey = SummaryKey.VALUE_CHANGE;
    }
    else
    {
      this.summaryKey = SummaryKey.UNMODIFIED;
    }
  }
  
  set summaryKey(newKey: SummaryKey)
  {
    this.summaryKeyData = newKey;
    this.localizeSummaryKey();
  }
  
  get summaryKey(): SummaryKey
  {
    return this.summaryKeyData;
  }
  
  private localizeSummaryKey(): void
  {
    this.summaryKeyLocalized = this.component.lService.decode('changeovertime.manageVersions.summaryKey.' + this.summaryKeyData);
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

    @Input() isGeometryInlined: boolean = false;

    // Observable subject for MasterList changes.  Called when an update is successful // TODO : This probably doesn't work anymore
    @Output() onChange = new EventEmitter<GeoObjectOverTime>();
    
    attributeType: AttributeType;
    actions: AbstractAction[] = [];

    // eslint-disable-next-line accessor-pairs
    @Input() set attributeData(value: {"attributeType":AttributeType, "actions":AbstractAction[], geoObject:GeoObjectOverTime}) {

        this.attributeType = value.attributeType;

        this.actions = value.actions;

        this.originalGeoObjectOverTime = JSON.parse(JSON.stringify(value.geoObject));
        this.postGeoObject = value.geoObject;
    }

    @Input() geoObjectType: GeoObjectType;

    originalGeoObjectOverTime: GeoObjectOverTime;
    postGeoObject: GeoObjectOverTime;

    @Input() isNewGeoObject: boolean = false;
    
    @Input() hierarchy: HierarchyOverTime = null;

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
    constructor(private geomService: GeometryService, public cdr: ChangeDetectorRef, public service: RegistryService, public lService: LocalizationService, public changeDetectorRef: ChangeDetectorRef, private dateService: DateService, private authService: AuthService,
        private requestService: ChangeRequestService) { }

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
    
    // https://stackoverflow.com/questions/105034/how-to-create-a-guid-uuid
    generateUUID() {
      return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
      });
    }

    toggleGeometryEditing(view: VersionDiffView) {
      //view.layer.editing = !view.layer.editing;
      
      //if (this.geometryEditor != null)
      //{
      //  this.geometryEditor.reload();
      //}
      
      //this.geomService.setLayers(this.renderedLayers);
      
      if (view.isEditingGeometries)
      {
        this.geomService.stopEditing(view.editPropagator);
      }
      else
      {
        this.geomService.startEditing(view.editPropagator);
      }
      
      view.isEditingGeometries = !view.isEditingGeometries;
    }
    
    toggleGeometryView(view: VersionDiffView) {
      
      view.isRenderingLayer = !view.isRenderingLayer;
      
      this.geomService.setLayers(this.getRenderedLayers());
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

    onApprove(): void
    {

        this.requestService.setActionStatus(this.editAction.oid, GovernanceStatus.ACCEPTED).then(results => {

            console.log("accepted");

        });
//        .catch((err: HttpErrorResponse) => {
//            this.error(err);
//        });

    }

    onReject(): void {

        this.requestService.setActionStatus(this.editAction.oid, GovernanceStatus.REJECTED).then(results => {

            console.log("rejected");

        });
//        .catch((err: HttpErrorResponse) => {
//            this.error(err);
//        });

    }

    onPending(): void {

        this.requestService.setActionStatus(this.editAction.oid, GovernanceStatus.PENDING).then(results => {

            console.log("pending");

        });
//        .catch((err: HttpErrorResponse) => {
//            this.error(err);
//        });

    }

}