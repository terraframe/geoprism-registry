
import { ValueOverTime } from "@registry/model/registry";
import { ManageVersionsComponent } from "./manage-versions.component";
import { VersionDiffView } from "./VersionDiffView";
import { CreateGeoObjectAction, UpdateAttributeAction, AbstractAction, ValueOverTimeDiff, ChangeRequest, SummaryKey } from "@registry/model/crtable";
import { v4 as uuid } from 'uuid';
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
          this.diff.oid = uuid();
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
          this.diff.oid = uuid();          
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
          this.diff.oid = uuid();          
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
    if (this.component.attributeType.type === "boolean")
    {
      return val1 === val2;        
    }
    
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
      if(val1 != null && val2 != null)
      {
        return val1.length === val2.length && val1[0] === val2[0];        
      }
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
      if(this.diff != null && this.diff.action === 'CREATE') {
        // Its a new entry, just remove the diff from the diff array
        let updateAction: UpdateAttributeAction = this.action as UpdateAttributeAction;
        
        const index = updateAction.attributeDiff.valuesOverTime.findIndex(vot => vot.oid === this.diff.oid);
        
        if(index > -1) {
          updateAction.attributeDiff.valuesOverTime.splice(index, 1);
        }
      }
      else if (this.valueOverTime != null && this.diff == null)
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
    if (this.component.editAction instanceof CreateGeoObjectAction)
    {
      let vot = new ValueOverTime();
      vot.oid = this.component.generateUUID();
      
      this.component.editAction.geoObjectJson.attributes[this.component.attributeType.code].values.push(vot);
    
      this.valueOverTime = vot;
    }
  
  
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

            this.value = terms[0].code;

        }

    }
    
    this.view.summaryKey = SummaryKey.NEW;
  }
}

