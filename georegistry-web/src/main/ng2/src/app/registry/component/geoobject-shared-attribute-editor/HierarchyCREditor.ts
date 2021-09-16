import { ValueOverTimeCREditor } from "./ValueOverTimeCREditor";
import { HierarchyOverTimeEntry, GeoObject, HierarchyOverTimeEntryParent, HierarchyOverTime, AttributeType } from "@registry/model/registry";
import { UpdateAttributeOverTimeAction, AbstractAction, ValueOverTimeDiff } from "@registry/model/crtable";
import { v4 as uuid } from "uuid";
import { ChangeRequestChangeOverTimeAttributeEditor } from "./change-request-change-over-time-attribute-editor";

export class HierarchyCREditor extends ValueOverTimeCREditor {

  hierarchyOverTime: HierarchyOverTime;

  hierarchyEntry: HierarchyOverTimeEntry;

  constructor(changeRequestAttributeEditor: ChangeRequestChangeOverTimeAttributeEditor, attr: AttributeType, action: AbstractAction, hierarchyEntry: HierarchyOverTimeEntry, hierarchyOverTime: HierarchyOverTime) {
      super(changeRequestAttributeEditor, attr, action);
      this.hierarchyEntry = hierarchyEntry;
      this.hierarchyOverTime = hierarchyOverTime;

      if (this.hierarchyEntry != null) {
          this.hierarchyEntry.loading = {};
      }
  }

  set startDate(startDate: string) {
      if (this.diff != null && this.diff.action === "DELETE") {
          return; // There are various view components (like the date widgets) which will invoke this method
      }

      if (this.action.actionType === "UpdateAttributeAction") {
          if (this.diff == null) {
              if (this.hierarchyEntry == null) {
                  this.diff = new ValueOverTimeDiff();
                  this.diff.oid = uuid();
                  this.diff.action = "CREATE";
                  (this.action as UpdateAttributeOverTimeAction).attributeDiff.hierarchyCode = this.hierarchyOverTime.code;
                  (this.action as UpdateAttributeOverTimeAction).attributeDiff.valuesOverTime.push(this.diff);
              } else {
                  if (this.hierarchyEntry.startDate === startDate) {
                      return;
                  }

                  let immediateParent: GeoObject = this.hierarchyEntry.parents[this.hierarchyOverTime.types[this.hierarchyOverTime.types.length - 1].code].geoObject;
                  let oldValue: string = immediateParent == null ? null : immediateParent.properties.type + "_~VST~_" + immediateParent.properties.code;

                  this.diff = new ValueOverTimeDiff();
                  this.diff.action = "UPDATE";
                  this.diff.oid = this.hierarchyEntry.oid;
                  this.diff.oldValue = oldValue;
                  this.diff.oldStartDate = this.hierarchyEntry.startDate;
                  this.diff.oldEndDate = this.hierarchyEntry.endDate;
                  (this.action as UpdateAttributeOverTimeAction).attributeDiff.hierarchyCode = this.hierarchyOverTime.code;
                  (this.action as UpdateAttributeOverTimeAction).attributeDiff.valuesOverTime.push(this.diff);
                  this.diff.parents = this.hierarchyEntry.parents;
              }
          }

          if (startDate === this.diff.oldStartDate) {
              delete this.diff.newStartDate;
          } else {
              this.diff.newStartDate = startDate;
          }

          // If no changes have been made then remove the diff
          this.removeEmptyDiff();
      } else if (this.action.actionType === "CreateGeoObjectAction") {
          this.hierarchyEntry.startDate = startDate;
      }

      this.changeRequestAttributeEditor.onChange();
  }

  get startDate() {
      if (this.diff != null) {
          if (this.diff.newStartDate != null) {
              return this.diff.newStartDate;
          } else {
              return this.diff.oldStartDate;
          }
      } else if (this.hierarchyEntry != null) {
          return this.hierarchyEntry.startDate;
      }

      return null;
  }

  set endDate(endDate: string) {
      if (this.diff != null && this.diff.action === "DELETE") {
          return; // There are various view components (like the date widgets) which will invoke this method
      }

      if (this.action.actionType === "UpdateAttributeAction") {
          if (this.diff == null) {
              if (this.hierarchyEntry == null) {
                  this.diff = new ValueOverTimeDiff();
                  this.diff.oid = uuid();
                  this.diff.action = "CREATE";
                  (this.action as UpdateAttributeOverTimeAction).attributeDiff.hierarchyCode = this.hierarchyOverTime.code;
                  (this.action as UpdateAttributeOverTimeAction).attributeDiff.valuesOverTime.push(this.diff);
              } else {
                  if (this.hierarchyEntry.endDate === endDate) {
                      return;
                  }

                  let immediateParent: GeoObject = this.hierarchyEntry.parents[this.hierarchyOverTime.types[this.hierarchyOverTime.types.length - 1].code].geoObject;
                  let oldValue: string = immediateParent == null ? null : immediateParent.properties.type + "_~VST~_" + immediateParent.properties.code;

                  this.diff = new ValueOverTimeDiff();
                  this.diff.action = "UPDATE";
                  this.diff.oid = this.hierarchyEntry.oid;
                  this.diff.oldValue = oldValue;
                  this.diff.oldStartDate = this.hierarchyEntry.startDate;
                  this.diff.oldEndDate = this.hierarchyEntry.endDate;
                  (this.action as UpdateAttributeOverTimeAction).attributeDiff.hierarchyCode = this.hierarchyOverTime.code;
                  (this.action as UpdateAttributeOverTimeAction).attributeDiff.valuesOverTime.push(this.diff);
                  this.diff.parents = this.hierarchyEntry.parents;
              }
          }

          if (endDate === this.diff.oldEndDate) {
              delete this.diff.newEndDate;
          } else {
              this.diff.newEndDate = endDate;
          }

          // If no changes have been made then remove the diff
          this.removeEmptyDiff();
      } else if (this.action.actionType === "CreateGeoObjectAction") {
          this.hierarchyEntry.endDate = endDate;
      }

      this.changeRequestAttributeEditor.onChange();
  }

  get endDate() {
      if (this.diff != null) {
          if (this.diff.newEndDate != null) {
              return this.diff.newEndDate;
          } else {
              return this.diff.oldEndDate;
          }
      } else if (this.hierarchyEntry != null) {
          return this.hierarchyEntry.endDate;
      }

      return null;
  }

  set oldValue(val: any) {
      if (this.diff != null) {
          this.diff.oldValue = val;
      } else if (this.hierarchyEntry != null) {
          this.hierarchyEntry = val;
      }
  }

  get oldValue() {
      if (this.diff != null) {
          return this.diff.oldValue;
      } else if (this.hierarchyEntry != null) {
          return this.hierarchyEntry;
      }

      return null;
  }

  setParentValue(type: {code: string, label: string}, parents: { [k: string]: HierarchyOverTimeEntryParent }) {
      if (this.diff != null && this.diff.action === "DELETE") {
          return; // There are various view components (like the date widgets) which will invoke this method
      }

      let directParent: GeoObject = null;
      if (type != null) {
          directParent = parents[type.code].geoObject;
      }

      if (this.action.actionType === "UpdateAttributeAction") {
          if (this.diff == null) {
              if (this.hierarchyEntry == null) {
                  this.diff = new ValueOverTimeDiff();
                  this.diff.oid = uuid();
                  this.diff.action = "CREATE";
                  (this.action as UpdateAttributeOverTimeAction).attributeDiff.hierarchyCode = this.hierarchyOverTime.code;
                  (this.action as UpdateAttributeOverTimeAction).attributeDiff.valuesOverTime.push(this.diff);
              } else {
                  // let currentDirectParent: GeoObject = this.hierarchyEntry.parents[type.code].geoObject;
                  let currentDirectParent: GeoObject = this.getLowestLevelFromHierarchyEntry(this.hierarchyEntry.parents).geoObject;
                  let oldValue: string = currentDirectParent == null ? null : currentDirectParent.properties.type + "_~VST~_" + currentDirectParent.properties.code;

                  if (
                      (currentDirectParent == null && directParent == null) ||
                      ((currentDirectParent != null && directParent != null) &&
                      currentDirectParent.properties.code === directParent.properties.code)) {
                      return;
                  }

                  this.diff = new ValueOverTimeDiff();
                  this.diff.action = "UPDATE";
                  this.diff.oid = this.hierarchyEntry.oid;
                  this.diff.oldValue = oldValue;
                  this.diff.oldParents = this.hierarchyEntry.parents;
                  this.diff.oldStartDate = this.hierarchyEntry.startDate;
                  this.diff.oldEndDate = this.hierarchyEntry.endDate;
                  (this.action as UpdateAttributeOverTimeAction).attributeDiff.hierarchyCode = this.hierarchyOverTime.code;
                  (this.action as UpdateAttributeOverTimeAction).attributeDiff.valuesOverTime.push(this.diff);
              }
          }

          let newValueStrConcat: string = null;
          if (directParent != null) {
              newValueStrConcat = directParent.properties.type + "_~VST~_" + directParent.properties.code;
          }

          if (newValueStrConcat === this.diff.oldValue) {
              delete this.diff.newValue;
          } else {
              this.diff.newValue = newValueStrConcat;
          }

          this.diff.parents = parents;

          // If no changes have been made then remove the diff
          this.removeEmptyDiff();
      } else if (this.action.actionType === "CreateGeoObjectAction") {
          this.hierarchyEntry.parents = parents;
      }

      this.changeRequestAttributeEditor.onChange();
  }

  public getLowestLevelFromHierarchyEntry(parents: any): {geoObject: GeoObject, text: string} {
      let len = this.hierarchyOverTime.types.length;
      for (let i = len - 1; i >= 0; --i) {
          let type = this.hierarchyOverTime.types[i];

          if (Object.prototype.hasOwnProperty.call(parents, type.code) && parents[type.code].geoObject) {
              return parents[type.code];
          }
      }

      return null;
  }

  set value(val: any) {
      this.hierarchyEntry = val;
  }

  get value() {
      return this.hierarchyEntry;
  }

  public removeType(type): void {
      this.value.parents[type.code] = { text: "", geoObject: null };

      // Set the value to be the next existing ancestor.
      let entry = this.value;
      let len = this.hierarchyOverTime.types.length;
      for (let i = len - 1; i >= 0; --i) {
          let type = this.hierarchyOverTime.types[i];

          if (Object.prototype.hasOwnProperty.call(entry.parents, type.code) && entry.parents[type.code].geoObject) {
              this.setParentValue(type, this.value.parents);
              return;
          }
      }

      // If we do not have a next existing ancestor, then we must set the value to null.
      this.setParentValue(null, this.value.parents);
  }

  createEmptyHierarchyEntry(): HierarchyOverTimeEntry {
      let hierarchyEntry = new HierarchyOverTimeEntry();
      hierarchyEntry.loading = {};
      hierarchyEntry.oid = uuid();

      hierarchyEntry.parents = {};

      if (this.hierarchyOverTime) {
          for (let i = 0; i < this.hierarchyOverTime.types.length; i++) {
              let current = this.hierarchyOverTime.types[i];

              hierarchyEntry.parents[current.code] = { text: "", geoObject: null };

              hierarchyEntry.loading = {};
          }
      }

      return hierarchyEntry;
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
              this.changeRequestAttributeEditor.onChange();
              return;
          } else if (this.hierarchyEntry != null && this.diff == null) {
              let currentImmediateParent: GeoObject = this.getLowestLevelFromHierarchyEntry(this.hierarchyEntry.parents).geoObject;
              let oldValue: string = currentImmediateParent == null ? null : currentImmediateParent.properties.type + "_~VST~_" + currentImmediateParent.properties.code;

              this.diff = new ValueOverTimeDiff();
              this.diff.action = "DELETE";
              this.diff.oid = this.hierarchyEntry.oid;
              this.diff.oldValue = oldValue;
              this.diff.oldStartDate = this.hierarchyEntry.startDate;
              this.diff.oldEndDate = this.hierarchyEntry.endDate;
              (this.action as UpdateAttributeOverTimeAction).attributeDiff.valuesOverTime.push(this.diff);
              (this.action as UpdateAttributeOverTimeAction).attributeDiff.hierarchyCode = this.hierarchyOverTime.code;
          }
      } else if (this.action.actionType === "CreateGeoObjectAction") {
          let index = this.hierarchyOverTime.entries.findIndex(vot => vot.oid === this.hierarchyEntry.oid);

          if (index !== -1) {
              this.hierarchyOverTime.entries.splice(index, 1);
          }
      }

      this.changeRequestAttributeEditor.onChange();
  }

}
