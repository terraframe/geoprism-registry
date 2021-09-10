import { ValueOverTime, AttributeType, TimeRangeEntry } from "@registry/model/registry";
import { ActionTypes } from "@registry/model/constants";
import { CreateGeoObjectAction, UpdateAttributeOverTimeAction, AbstractAction, ChangeRequest, ValueOverTimeDiff, SummaryKey } from "@registry/model/crtable";
import { v4 as uuid } from "uuid";
// eslint-disable-next-line camelcase
import turf_booleanequal from "@turf/boolean-equal";
import { LocalizedValue } from "@shared/model/core";

export class ValueOverTimeCREditor implements TimeRangeEntry {

  diff: ValueOverTimeDiff; // Any existing diff which may be associated with this view object.
  valueOverTime?: ValueOverTime; // Represents a vot on an existing GeoObject. If this is set and the action is UpdateAttribute, we must be doing an UPDATE, and valueOverTime represents the original value in the DB.
  action: AbstractAction;
  changeRequest: ChangeRequest;
  attr: AttributeType;

  constructor(changeRequest: ChangeRequest, attr: AttributeType, action: AbstractAction) {
      this.attr = attr;
      this.changeRequest = changeRequest;
      this.action = action;
  }

  set oid(oid: string) {
      if (this.diff != null) {
          this.diff.oid = oid;
      } else if (this.valueOverTime != null) {
          this.valueOverTime.oid = oid;
      }
  }

  get oid(): string {
      if (this.diff != null) {
          return this.diff.oid;
      } else if (this.valueOverTime != null) {
          return this.valueOverTime.oid;
      }

      return null;
  }

  onActionChange(action: AbstractAction) {
      let hasChanges: boolean = true;

      if (action.actionType === ActionTypes.UPDATEATTRIBUTETACTION) {
          let updateAction: UpdateAttributeOverTimeAction = action as UpdateAttributeOverTimeAction;

          if (updateAction.attributeDiff.valuesOverTime.length === 0) {
              hasChanges = false;
          }
      }

      let index = -1;

      let len = this.changeRequest.actions.length;
      for (let i = 0; i < len; ++i) {
          let loopAction = this.changeRequest.actions[i];

          if (action === loopAction) {
              index = i;
          }
      }

      if (index !== -1 && !hasChanges) {
          this.changeRequest.actions.splice(index, 1);
      } else if (index === -1 && hasChanges) {
          this.changeRequest.actions.push(action);
      }
  }

  get startDate() {
      if (this.diff != null) {
          if (this.diff.newStartDate != null) {
              return this.diff.newStartDate;
          } else {
              return this.diff.oldStartDate;
          }
      } else if (this.valueOverTime != null) {
          return this.valueOverTime.startDate;
      }

      return null;
  }

  set startDate(startDate: string) {
      if (this.diff != null && this.diff.action === "DELETE") {
          return; // There are various view components (like the date widgets) which will invoke this method
      }

      if (this.action.actionType === "UpdateAttributeAction") {
          if (this.diff == null) {
              if (this.valueOverTime == null) {
                  this.diff = new ValueOverTimeDiff();
                  this.diff.action = "CREATE";
                  (this.action as UpdateAttributeOverTimeAction).attributeDiff.valuesOverTime.push(this.diff);
              } else {
                  if (this.valueOverTime.startDate === startDate) {
                      return;
                  }

                  this.diff = new ValueOverTimeDiff();
                  this.diff.action = "UPDATE";
                  this.diff.oid = this.valueOverTime.oid;
                  this.diff.oldValue = this.valueOverTime.value;
                  this.diff.oldStartDate = this.valueOverTime.startDate;
                  this.diff.oldEndDate = this.valueOverTime.endDate;
                  (this.action as UpdateAttributeOverTimeAction).attributeDiff.valuesOverTime.push(this.diff);
              }
          }

          if (startDate === this.diff.oldStartDate) {
              delete this.diff.newStartDate;
              delete this.view.oldStartDate;
          } else {
              this.diff.newStartDate = startDate;
              this.view.oldStartDate = this.convertDateForDisplay(this.diff.oldStartDate);
          }

          // If no changes have been made then remove the diff
          this.removeEmptyDiff();
      } else if (this.action.actionType === "CreateGeoObjectAction") {
          this.valueOverTime.startDate = startDate;
      }

      this.view.startDate = startDate;

      this.view.calculateSummaryKey(this.diff);

      this.onActionChange(this.action);
  }

  get endDate() {
      if (this.diff != null) {
          if (this.diff.newEndDate != null) {
              return this.diff.newEndDate;
          } else {
              return this.diff.oldEndDate;
          }
      } else if (this.valueOverTime != null) {
          return this.valueOverTime.endDate;
      }

      return null;
  }

  set endDate(endDate: string) {
      if (this.diff != null && this.diff.action === "DELETE") {
          return; // There are various view components (like the date widgets) which will invoke this method
      }

      if (this.action.actionType === "UpdateAttributeAction") {
          if (this.diff == null) {
              if (this.valueOverTime == null) {
                  this.diff = new ValueOverTimeDiff();
                  this.diff.oid = uuid();
                  this.diff.action = "CREATE";
                  (this.action as UpdateAttributeOverTimeAction).attributeDiff.valuesOverTime.push(this.diff);
              } else {
                  if (this.valueOverTime.endDate === endDate) {
                      return;
                  }

                  this.diff = new ValueOverTimeDiff();
                  this.diff.action = "UPDATE";
                  this.diff.oid = this.valueOverTime.oid;
                  this.diff.oldValue = this.valueOverTime.value;
                  this.diff.oldStartDate = this.valueOverTime.startDate;
                  this.diff.oldEndDate = this.valueOverTime.endDate;
                  (this.action as UpdateAttributeOverTimeAction).attributeDiff.valuesOverTime.push(this.diff);
              }
          }

          if (endDate === this.diff.oldEndDate) {
              delete this.diff.newEndDate;
              delete this.view.oldEndDate;
          } else {
              this.diff.newEndDate = endDate;
              this.view.oldEndDate = this.convertDateForDisplay(this.diff.oldEndDate);
          }

          // If no changes have been made then remove the diff
          this.removeEmptyDiff();
      } else if (this.action.actionType === "CreateGeoObjectAction") {
          this.valueOverTime.endDate = endDate;
      }

      this.view.endDate = endDate;

      this.view.calculateSummaryKey(this.diff);

      this.onActionChange(this.action);
  }

  get value() {
      if (this.diff != null) {
          if (this.diff.newValue != null) {
              return this.diff.newValue;
          } else {
              return this.diff.oldValue;
          }
      } else if (this.valueOverTime != null) {
          return this.valueOverTime.value;
      }

      return null;
  }

  set value(value: any) {
      if (this.diff != null && this.diff.action === "DELETE") {
          return; // There are various view components (like the date widgets) which will invoke this method
      }

      if (value != null) {
          if (this.attr.type === "term") {
              value = [value];
          } else if (this.attr.type === "date") {
              value = new Date(value).getTime();
          }
      } else if (value == null) {
          if (this.attr.type === "geometry") {
              value = this.component.geomService.createEmptyGeometryValue();
          } else if (this.attr.type === "character") {
              value = "";
          }
      }

      if (this.action.actionType === "UpdateAttributeAction") {
          if (this.diff == null) {
              if (this.valueOverTime == null) {
                  this.diff = new ValueOverTimeDiff();
                  this.diff.oid = uuid();
                  this.diff.action = "CREATE";
                  (this.action as UpdateAttributeOverTimeAction).attributeDiff.valuesOverTime.push(this.diff);
              } else {
                  if (this.areValuesEqual(this.valueOverTime.value, value)) {
                      return;
                  }

                  this.diff = new ValueOverTimeDiff();
                  this.diff.action = "UPDATE";
                  this.diff.oid = this.valueOverTime.oid;
                  this.diff.oldValue = this.valueOverTime.value;
                  this.diff.oldStartDate = this.valueOverTime.startDate;
                  this.diff.oldEndDate = this.valueOverTime.endDate;
                  (this.action as UpdateAttributeOverTimeAction).attributeDiff.valuesOverTime.push(this.diff);
              }
          }

          if (this.diff.action !== "CREATE" && this.areValuesEqual(this.diff.oldValue, value)) {
              delete this.diff.newValue;
              delete this.view.oldValue;
          } else {
              this.diff.newValue = JSON.parse(JSON.stringify(value));
              this.view.oldValue = this.diff.oldValue == null ? null : this.convertValueForDisplay(this.diff.oldValue);
          }

          // If no changes have been made then remove the diff
          this.removeEmptyDiff();
      } else if (this.action.actionType === "CreateGeoObjectAction") {
          this.valueOverTime.value = value;
      }

      this.view.value = value;

      this.view.calculateSummaryKey(this.diff);

      this.onActionChange(this.action);

      if (this.attr.code === "exists") {
          this.component.onDateChange();
      }
  }

  public setLocalizedValue(localeValue: {locale: string, value: string}) {
      // eslint-disable-next-line no-self-assign
      this.value = this.value;
  }

  removeEmptyDiff(): void {
      if (this.diff != null && this.diff.newValue === undefined && this.diff.newStartDate === undefined && this.diff.newEndDate === undefined) {
          const diffs = (this.action as UpdateAttributeOverTimeAction).attributeDiff.valuesOverTime;

          const index = diffs.findIndex(d => d.oid === this.diff.oid);

          if (index !== -1) {
              diffs.splice(index, 1);
          }

          this.diff = null;
      }
  }

  areValuesEqual(val1: any, val2: any): boolean {
      if (this.attr.type === "boolean") {
          return val1 === val2;
      }

      if ((val1 === "" && val2 == null) || (val2 === "" && val1 == null)) {
          return true;
      }

      if (!val1 && !val2) {
          return true;
      } else if ((!val1 && val2) || (!val2 && val1)) {
          return false;
      }

    // if (this.attr.type === "local")
    // {
      // Not used anymore
    // }
      if (this.attr.type === "term") {
          if (val1 != null && val2 != null) {
              return val1.length === val2.length && val1[0] === val2[0];
          }
      } else if (this.attr.type === "geometry") {
          return turf_booleanequal(val1, val2);
      } else if (this.attr.type === "date") {
          let casted1 = (typeof val1 === "string") ? parseInt(val1) : val1;
          let casted2 = (typeof val2 === "string") ? parseInt(val2) : val2;

          return casted1 === casted2;
      } else if (this.attr.type === "local") {
          if ((!val1.localeValues || !val2.localeValues) || val1.localeValues.length !== val2.localeValues.length) {
              return false;
          }

          let len = val1.localeValues.length;
          for (let i = 0; i < len; ++i) {
              let localeValue = val1.localeValues[i];

              let lv2 = this.getValueAtLocale(val2, localeValue.locale);
              let lv1 = localeValue.value;

              if ((lv1 === "" && lv2 == null) || (lv2 === "" && lv1 == null)) {
                  continue;
              } else if (lv1 !== lv2) {
                  return false;
              }
          }

          return true;
      }

      return val1 === val2;
  }

  getValueAtLocale(lv: LocalizedValue, locale: string) {
      return new LocalizedValue(lv.localizedValue, lv.localeValues).getValue(locale);
  }

  recalculateView(): void {
      if (this.diff === null) {
          if (this.action.actionType === "UpdateAttributeAction") {
              if (this.valueOverTime != null) {
                  this.view.value = this.valueOverTime.value != null ? JSON.parse(JSON.stringify(this.valueOverTime.value)) : null;
                  this.view.startDate = this.valueOverTime.startDate;
                  this.view.endDate = this.valueOverTime.endDate;

                  delete this.view.oldValue;
                  delete this.view.oldStartDate;
                  delete this.view.oldEndDate;
              }
          } else {
              // TODO
          }
      }

      this.view.calculateSummaryKey(this.diff);
  }

  public remove(): void {
      if (this.action.actionType === "UpdateAttributeAction") {
          if (this.diff != null && this.diff.action === "CREATE") {
              // Its a new entry, just remove the diff from the diff array
              let updateAction: UpdateAttributeOverTimeAction = this.action as UpdateAttributeOverTimeAction;

              const index = updateAction.attributeDiff.valuesOverTime.findIndex(vot => vot.oid === this.diff.oid);

              if (index > -1) {
                  updateAction.attributeDiff.valuesOverTime.splice(index, 1);
              }
          } else if (this.diff != null) {
              delete this.diff.newValue;
              delete this.diff.newStartDate;
              delete this.diff.newEndDate;
              this.removeEmptyDiff();
              this.onActionChange(this.action);
              this.recalculateView();
              return;
          } else if (this.valueOverTime != null && this.diff == null) {
              this.diff = new ValueOverTimeDiff();
              this.diff.action = "DELETE";
              this.diff.oid = this.valueOverTime.oid;
              this.diff.oldValue = this.valueOverTime.value;
              this.diff.oldStartDate = this.valueOverTime.startDate;
              this.diff.oldEndDate = this.valueOverTime.endDate;
              (this.action as UpdateAttributeOverTimeAction).attributeDiff.valuesOverTime.push(this.diff);
          }
      } else if (this.action.actionType === "CreateGeoObjectAction") {
          let votc = (this.action as CreateGeoObjectAction).geoObjectJson.attributes[this.attr.code].values;

          let index = votc.findIndex((vot) => { return vot.oid === this.valueOverTime.oid; });

          if (index !== -1) {
              votc.splice(index, 1);
          }
      }

      this.view.calculateSummaryKey(this.diff);

      this.onActionChange(this.action);
  }

  public onAddNewVersion(): void {
      if (this.action instanceof CreateGeoObjectAction) {
          let vot = new ValueOverTime();
          vot.oid = uuid();

          this.action.geoObjectJson.attributes[this.attr.code].values.push(vot);

          this.valueOverTime = vot;
      }

      if (this.attr.type === "local") {
          this.value = this.component.lService.create();
      } else if (this.attr.type === "geometry") {
          if (this.component.viewModels.length > 0) {
              this.value = JSON.parse(JSON.stringify(this.component.viewModels[this.component.viewModels.length - 1].value));
          } else {
              this.value = this.component.geomService.createEmptyGeometryValue();
          }
      } else if (this.attr.type === "term") {
          let terms = this.component.getGeoObjectTypeTermAttributeOptions(this.attr.code);

          if (terms && terms.length > 0) {
              this.value = terms[0].code;
          }
      } else {
          this.value = null;
      }

      this.view.summaryKey = SummaryKey.NEW;
  }

}
