import { ValueOverTimeCREditor } from "./ValueOverTimeCREditor";
import { HierarchyOverTimeEntry, GeoObject, HierarchyOverTimeEntryParent, HierarchyOverTime, AttributeType, TimeRangeEntry } from "@registry/model/registry";
import { UpdateAttributeOverTimeAction, AbstractAction, ValueOverTimeDiff } from "@registry/model/crtable";
import { v4 as uuid } from "uuid";
import { ChangeRequestChangeOverTimeAttributeEditor } from "./change-request-change-over-time-attribute-editor";
import { ChangeType } from "@registry/model/constants";
import { HttpErrorResponse } from "@angular/common/http";

export class HierarchyCREditor extends ValueOverTimeCREditor {

  hierarchyOverTime: HierarchyOverTime;

  hierarchyEntry: HierarchyOverTimeEntry;

  existRangeStale: boolean = false;

  constructor(changeRequestAttributeEditor: ChangeRequestChangeOverTimeAttributeEditor, attr: AttributeType, action: AbstractAction, hierarchyEntry: HierarchyOverTimeEntry, hierarchyOverTime: HierarchyOverTime) {
      super(changeRequestAttributeEditor, attr, action);
      this.hierarchyEntry = hierarchyEntry;
      this.hierarchyOverTime = hierarchyOverTime;

      if (this.hierarchyEntry != null) {
          this.hierarchyEntry.loading = {};
      }
  }

  // @Override
  getGeoObjectTimeRangeStorage(): TimeRangeEntry {
      return this.hierarchyEntry;
  }

  // @Override
  getValueFromGeoObjectForDiff(): any {
      let immediateParent: GeoObject = this.hierarchyEntry == null ? null : this.hierarchyEntry.parents[this.hierarchyOverTime.types[this.hierarchyOverTime.types.length - 1].code].geoObject;
      let goVal: string = immediateParent == null ? null : immediateParent.properties.type + "_~VST~_" + immediateParent.properties.code;
      return goVal;
  }

  // @Override
  onChange(type: ChangeType) {
      if (type === ChangeType.END_DATE || type === ChangeType.START_DATE) {
          this.existRangeStale = true;
      }

      super.onChange(type);
  }

  // @Override
  validate(): boolean {
      super.validate();

      if (this._isValid && this.hierarchyEntry != null) {
          let invalidParent = this.changeRequestAttributeEditor.changeRequestEditor.dateService.invalidParent;
          let parentDoesNotExist = this.changeRequestAttributeEditor.changeRequestEditor.dateService.parentDoesNotExist;
          let service = this.changeRequestAttributeEditor.changeRequestEditor.registryService;

          let len = this.hierarchyOverTime.types.length;
          for (let i = len - 1; i >= 0; --i) {
              let type = this.hierarchyOverTime.types[i];

              if (Object.prototype.hasOwnProperty.call(this.hierarchyEntry.parents, type.code) && this.hierarchyEntry.parents[type.code].geoObject) {
                  let goParent = this.hierarchyEntry.parents[type.code].geoObject;

                  if (!this.existRangeStale) {
                      if (goParent.properties.invalid) {
                          this._isValid = false;
                          this.conflictMessages.add(invalidParent);
                      }
                      if (!goParent.properties.exists) {
                          this._isValid = false;
                          this.conflictMessages.add(parentDoesNotExist);
                      }
                  } else {
                      service.doesGeoObjectExistAtRange(this.startDate, this.endDate, type.code, goParent.properties.code).then(stats => {
                          goParent.properties.invalid = stats.invalid;
                          goParent.properties.exists = stats.exists;

                          this.conflictMessages.delete(invalidParent);
                          this.conflictMessages.delete(parentDoesNotExist);

                          if (goParent.properties.invalid) {
                              this._isValid = false;
                              this.conflictMessages.add(invalidParent);
                          }
                          if (!goParent.properties.exists) {
                              this._isValid = false;
                              this.conflictMessages.add(parentDoesNotExist);
                          }
                      }).catch((err: HttpErrorResponse) => {
                      // eslint-disable-next-line no-console
                          console.log(err);
                      });
                  }
              }
          }

          this.existRangeStale = false;
      }

      return this._isValid;
  }

  // @Override
  validateUpdateReference() {
      // We could potentially try to check for this, but it won't be easy. So for now we're doing nothing.
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
                  this.diff.oldParents = JSON.parse(JSON.stringify(this.hierarchyEntry.parents));
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

      this.onChange(ChangeType.VALUE);
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
      throw new Error("Invoke setParentValue instead");
  }

  get value() {
      if (this.diff != null && this.diff.parents !== undefined) {
          return this.diff;
      } else if (this.hierarchyEntry != null) {
          return this.hierarchyEntry;
      }

      return null;
  }

  public removeType(type): void {
      // this.value.parents[type.code] = { text: "", geoObject: null };

      let newParents = JSON.parse(JSON.stringify(this.value.parents));
      newParents[type.code] = { text: "", geoObject: null };

      // Set the value to be the next existing ancestor.
      let entry = this.value;
      let len = this.hierarchyOverTime.types.length;
      for (let i = len - 1; i >= 0; --i) {
          let parentType = this.hierarchyOverTime.types[i];

          if (Object.prototype.hasOwnProperty.call(entry.parents, parentType.code) && entry.parents[parentType.code].geoObject) {
              this.setParentValue(parentType, newParents);
              return;
          }
      }

      // If we do not have a next existing ancestor, then we must set the value to null.
      this.setParentValue(null, newParents);
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
              this.onChange(ChangeType.REMOVE);
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

      this.onChange(ChangeType.REMOVE);
  }

}
