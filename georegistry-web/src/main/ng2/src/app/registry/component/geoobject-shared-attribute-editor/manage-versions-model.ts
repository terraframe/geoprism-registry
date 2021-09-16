
import { ManageVersionsComponent } from "./manage-versions.component";
import { ValueOverTimeDiff, SummaryKey } from "@registry/model/crtable";
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
export class VersionDiffView {

  component: ManageVersionsComponent;
  summaryKeyData: SummaryKey;
  summaryKeyLocalized: string; // If we try to localize this in the html with a localize element then it won't update as frequently as we need so we're doing stuff manually here.
  newLayer: Layer = null;
  oldLayer: Layer = null;
  coordinate?: any;
  newCoordinateX?: any;
  newCoordinateY? : any;
  editor: ValueOverTimeCREditor;

  _value: any;

  constructor(component: ManageVersionsComponent, editor: ValueOverTimeCREditor) {
      this.component = component;
      this.editor = editor;
      this._value = this.convertValueForDisplay(JSON.parse(JSON.stringify(this.editor.value)));
      //this.editor.onChangeSubject.subscribe(() => {
      //    this.calculateSummaryKey();
      //});
  }

  set oid(oid: string) {
      this.editor.oid = oid;
  }

  get oid(): string {
      return this.editor.oid;
  }

  get startDate(): string {
      return this.convertDateForDisplay(this.editor.startDate);
  }

  set startDate(startDate: string) {
      this.editor.startDate = startDate;
      this.calculateSummaryKey();
  }

  set oldStartDate(oldStartDate: string) {
      this.editor.oldStartDate = oldStartDate;
  }

  get oldStartDate(): string {
      return this.convertDateForDisplay(this.editor.oldStartDate);
  }

  get endDate(): string {
      return this.convertDateForDisplay(this.editor.endDate);
  }

  set endDate(endDate: string) {
      this.editor.endDate = endDate;
      this.calculateSummaryKey();
  }

  set oldEndDate(oldEndDate: string) {
      this.editor.oldEndDate = oldEndDate;
  }

  get oldEndDate(): string {
      return this.convertDateForDisplay(this.editor.oldEndDate);
  }

  get value(): any {
      //return this.convertValueForDisplay(JSON.parse(JSON.stringify(this.editor.value)));
      //let stringy = JSON.parse(JSON.stringify(this.editor.value));
      //return this.convertValueForDisplay(stringy);
      //return this.convertValueForDisplay(this.editor.value);
      //return this.editor.value;
      //return null;

      return this._value;
  }

  set value(value: any) {
      this.editor.value = value;
      this._value = this.convertValueForDisplay(JSON.parse(JSON.stringify(this.editor.value)));
      this.calculateSummaryKey();
  }

  set oldValue(oldValue: any) {
      this.editor.oldValue = oldValue;
  }

  get oldValue(): any {
      return this.convertValueForDisplay(JSON.parse(JSON.stringify(this.editor.oldValue)));
  }

  convertDateForDisplay(date: string): string {
      return (date == null || date.length === 0) ? null : this.component.dateService.formatDateForDisplay(date);
  }

  convertValueForDisplay(val: any): any {
      if (this.component.attributeType.type === "date") {
          return val == null ? null : this.component.dateService.formatDateForDisplay(new Date(val));
      }

      return val;
  }

  calculateSummaryKey() {
      if (this.editor.diff == null) {
          this.summaryKey = SummaryKey.UNMODIFIED;
          return;
      }

      if (this.editor.diff.action === "CREATE") {
          this.summaryKey = SummaryKey.NEW;
          return;
      } else if (this.editor.diff.action === "DELETE") {
          this.summaryKey = SummaryKey.DELETE;
          return;
      }

      let hasTime = this.editor.diff.newStartDate != null || this.editor.diff.newEndDate != null;
      let hasValue = Object.prototype.hasOwnProperty.call(this.editor.diff, "newValue");

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
