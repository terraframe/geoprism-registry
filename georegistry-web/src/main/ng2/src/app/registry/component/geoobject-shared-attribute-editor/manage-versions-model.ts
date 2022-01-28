
import { ManageVersionsComponent } from "./manage-versions.component";
import { ValueOverTimeDiff, SummaryKey } from "@registry/model/crtable";
import { ValueOverTimeCREditor } from "./ValueOverTimeCREditor";
import { LayerColor } from "@registry/model/constants";
import { LocalizedValue } from "@shared/model/core";
import { AttributeTermType, ConflictMessage, Term } from "@registry/model/registry";

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
 * using the editor.
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

  // We must track our own value, so that they can be diffed when setting.
  _value: any;

  constructor(component: ManageVersionsComponent, editor: ValueOverTimeCREditor) {
      this.component = component;
      this.editor = editor;

      this.populate(editor);
      this.editor.onChangeSubject.subscribe(() => {
          this.populate(this.editor);
      });
  }

  populate(editor: ValueOverTimeCREditor) {
      if (this.component.attributeType.type === "local" && this._value != null && this.editor.value != null) {
          // The front-end glitches out if we swap to a new object. We have to update the existing object to be the same
          LocalizedValue.populate(this._value, this.editor.value);
      } else if (this.component.attributeType.code === "_PARENT_" && this._value != null && this.editor.value != null && this.editor.value.parents != null) {
          for (let i = 0; i < this.component.hierarchy.types.length; i++) {
              let current = this.component.hierarchy.types[i];

              this._value.parents[current.code].text = this.editor.value.parents[current.code].text;
              this._value.parents[current.code].geoObject = this.editor.value.parents[current.code].geoObject;
          }
      } else if (this.component.attributeType.code === "_PARENT_") {
          this._value = JSON.parse(JSON.stringify(this.editor.value));
      } else {
          this._value = this.convertValueForDisplay(this.editor.value == null ? null : JSON.parse(JSON.stringify(this.editor.value)));
      }

      if (this.component.attributeType.code === "_PARENT_") {
          this._value.loading = {};
      }

      this.calculateSummaryKey();
  }

  set oid(oid: string) {
      this.editor.oid = oid;
  }

  get oid(): string {
      return this.editor.oid;
  }

  get startDate(): string {
      if (this.editor.diff != null && this.editor.diff.action === "DELETE") {
          return this.editor.oldStartDate;
      }

      return this.editor.startDate;
  }

  set startDate(startDate: string) {
      this.editor.startDate = startDate;
      this.calculateSummaryKey();
  }

  set oldStartDate(oldStartDate: string) {
      this.editor.oldStartDate = oldStartDate;
  }

  get oldStartDate(): string {
      if (this.editor.diff != null && this.editor.diff.action === "DELETE") {
          return null;
      }

      if (this.editor.diff != null && this.editor.diff.newStartDate != null && this.editor.oldStartDate !== undefined) {
          return this.convertDateForDisplay(this.editor.oldStartDate);
      }

      return null;
  }

  get endDate(): string {
      if (this.editor.diff != null && this.editor.diff.action === "DELETE") {
          return this.editor.oldEndDate;
      }

      return this.editor.endDate;
  }

  set endDate(endDate: string) {
      this.editor.endDate = endDate;
      this.calculateSummaryKey();
  }

  set oldEndDate(oldEndDate: string) {
      this.editor.oldEndDate = oldEndDate;
  }

  get oldEndDate(): string {
      if (this.editor.diff != null && this.editor.diff.action === "DELETE") {
          return null;
      }

      if (this.editor.diff != null && this.editor.diff.newEndDate != null && this.editor.oldEndDate !== undefined) {
          return this.convertDateForDisplay(this.editor.oldEndDate);
      }

      return null;
  }

  get value(): any {
      return this._value;
  }

  set value(value: any) {
      this.editor.value = value;
  }

  set oldValue(oldValue: any) {
      this.editor.oldValue = oldValue;
  }

  get oldValue(): any {
      if (this.editor.diff != null && this.editor.diff.action === "DELETE") {
          return null;
      }

      if (this.editor.diff != null && this.editor.diff.newValue != null && this.editor.oldValue !== undefined) {
          return this.convertOldValueForDisplay(this.editor.oldValue);
      }

      return null;
  }

  convertDateForDisplay(date: string): string {
      return (date == null || date.length === 0) ? null : this.component.dateService.formatDateForDisplay(date);
  }

  convertOldValueForDisplay(val: any): any {
      if (this.component.attributeType.type === "date") {
          return this.component.dateService.formatDateForDisplay(new Date(val));
      } else if (this.component.attributeType.code === "_PARENT_" && val.includes("_~VST~_")) {
          let split = val.split("_~VST~_");
          // let parentTypeCode = split[0];
          let parentCode = split[1];

          return parentCode;
      } else if (this.component.attributeType.type === "term") {
          let code = val;
          if (code instanceof Array) {
              code = val[0];
          }

          let attrOpts = (this.component.attributeType as AttributeTermType).rootTerm.children;

          let index = attrOpts.findIndex((term:Term) => term.code === code);

          if (index !== -1) {
              return attrOpts[index].label.localizedValue;
          } else {
              return val;
          }
      } else if (this.component.attributeType.type === "classification") {
          return val.label.localizedValue;
      }

      return val;
  }

  convertValueForDisplay(val: any): any {
      if (val == null) {
          return null;
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

  private conflictMessagesHasSeverity(severity: string) {
      let has = false;

      this.editor.conflictMessages.forEach(msg => {
          if (msg.severity === severity) {
              has = true;
          }
      });

      return has;
  }

  hasError(): boolean {
      return this.editor.conflictMessages && this.editor.conflictMessages.size > 0 && this.conflictMessagesHasSeverity("ERROR");
  }

  hasWarning(): boolean {
      return !this.hasError() && this.editor.conflictMessages && this.editor.conflictMessages.size > 0 && this.conflictMessagesHasSeverity("WARNING");
  }

}
