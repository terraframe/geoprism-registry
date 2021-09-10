
import { ValueOverTime, ConflictMessage } from "@registry/model/registry";
import { ManageVersionsComponent } from "./manage-versions.component";
import { AbstractAction, ValueOverTimeDiff, SummaryKey } from "@registry/model/crtable";
import { HierarchyEditPropagator } from "./HierarchyEditPropagator";
import { ValueOverTimeCREditor } from "./ValueOverTimeCREditor";
import { LayerColor } from "@registry/model/constants";

export class Layer {

  oid: string;
  isEditing: boolean;
  isRendering: boolean;
  color: LayerColor;
  zindex: number;
  geojson: any;
  editPropagator: ValueOverTimeCREditor;

}

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
  newLayer: Layer = null;
  oldLayer: Layer = null;
  coordinate?: any;
  newCoordinateX?: any;
  newCoordinateY? : any;
  editPropagator: ValueOverTimeCREditor;

  constructor(component: ManageVersionsComponent, action: AbstractAction) {
      super();

      this.component = component;

      if (component.attributeType.type === "_PARENT_") {
          this.editPropagator = new HierarchyEditPropagator(component, action, this, null, null);
      } else {
          this.editPropagator = new ValueOverTimeCREditor(component, action, this);
      }
  }

  convertDateForDisplay(date: string): string {
      return this.component.dateService.formatDateForDisplay(date);
  }

  convertValueForDisplay(val: any): any {
      if (this.component.attributeType.type === "date") {
          return this.component.dateService.formatDateForDisplay(new Date(val));
      }

      return val;
  }

  calculateSummaryKey(diff: ValueOverTimeDiff) {
      if (diff == null) {
          this.summaryKey = SummaryKey.UNMODIFIED;
          return;
      }

      if (diff.action === "CREATE") {
          this.summaryKey = SummaryKey.NEW;
          return;
      } else if (diff.action === "DELETE") {
          this.summaryKey = SummaryKey.DELETE;
          return;
      }

      let hasTime = diff.newStartDate != null || diff.newEndDate != null;
      let hasValue = Object.prototype.hasOwnProperty.call(diff, "newValue");

      if (hasTime && hasValue) {
          this.summaryKey = SummaryKey.UPDATE;
      } else if (hasTime) {
          this.summaryKey = SummaryKey.TIME_CHANGE;
      } else if (hasValue) {
          this.summaryKey = SummaryKey.VALUE_CHANGE;
      } else {
          this.summaryKey = SummaryKey.UNMODIFIED;
      }
  }

  set summaryKey(newKey: SummaryKey) {
      this.summaryKeyData = newKey;
      this.localizeSummaryKey();
  }

  get summaryKey(): SummaryKey {
      return this.summaryKeyData;
  }

  private localizeSummaryKey(): void {
      this.summaryKeyLocalized = this.component.lService.decode("changeovertime.manageVersions.summaryKey." + this.summaryKeyData);
  }

}
