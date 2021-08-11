
import { ValueOverTime, ConflictMessage } from "@registry/model/registry";
import { ManageVersionsComponent } from "./manage-versions.component";
import { CreateGeoObjectAction, UpdateAttributeAction, AbstractAction, ValueOverTimeDiff, ChangeRequest, SummaryKey } from "@registry/model/crtable";
import { HierarchyEditPropagator } from "./HierarchyEditPropagator";
import { ValueOverTimeEditPropagator } from "./ValueOverTimeEditPropagator";

/*
 * This class exists purely for the purpose of storing what data to be rendered to the front-end. Any storage or submission of this data to the back-end must be translated
 * using the edit propagator.
 */
export class VersionDiffView extends ValueOverTime {
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