import { Observable } from "rxjs";
import { TypeaheadMatch } from "ngx-bootstrap/typeahead";
import { ValueOverTimeEditPropagator } from "./ValueOverTimeEditPropagator";
import { HierarchyOverTimeEntry, GeoObject, HierarchyOverTimeEntryParent, HierarchyOverTime } from "@registry/model/registry";
import { ManageVersionsComponent } from "./manage-versions.component";
import { VersionDiffView } from "./manage-versions-model";
import { CreateGeoObjectAction, UpdateAttributeOverTimeAction, AbstractAction, ValueOverTimeDiff, SummaryKey } from "@registry/model/crtable";
import { v4 as uuid } from "uuid";

export class HierarchyEditPropagator extends ValueOverTimeEditPropagator {

  hierarchyOverTime: HierarchyOverTime;

  hierarchyEntry: HierarchyOverTimeEntry;

  constructor(component: ManageVersionsComponent, action: AbstractAction, view: VersionDiffView, hierarchyEntry: HierarchyOverTimeEntry, hierarchyOverTime: HierarchyOverTime) {
      super(component, action, view);
      this.hierarchyEntry = hierarchyEntry;
      this.hierarchyOverTime = this.component.hierarchy;

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
                  (this.action as UpdateAttributeOverTimeAction).attributeDiff.hierarchyCode = this.component.hierarchy.code;
                  (this.action as UpdateAttributeOverTimeAction).attributeDiff.valuesOverTime.push(this.diff);
              } else {
                  if (this.hierarchyEntry.startDate === startDate) {
                      return;
                  }

                  let immediateParent: GeoObject = this.hierarchyEntry.parents[this.component.hierarchy.types[this.component.hierarchy.types.length - 1].code].geoObject;
                  let oldValue: string = immediateParent == null ? null : immediateParent.properties.type + "_~VST~_" + immediateParent.properties.code;

                  this.diff = new ValueOverTimeDiff();
                  this.diff.action = "UPDATE";
                  this.diff.oid = this.hierarchyEntry.oid;
                  this.diff.oldValue = oldValue;
                  this.diff.oldStartDate = this.hierarchyEntry.startDate;
                  this.diff.oldEndDate = this.hierarchyEntry.endDate;
                  (this.action as UpdateAttributeOverTimeAction).attributeDiff.hierarchyCode = this.component.hierarchy.code;
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
          this.hierarchyEntry.startDate = startDate;
      }

      this.view.startDate = startDate;

      this.view.calculateSummaryKey(this.diff);

      this.component.onActionChange(this.action);
  }

  get startDate() {
      return this.view.startDate;
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
                  (this.action as UpdateAttributeOverTimeAction).attributeDiff.hierarchyCode = this.component.hierarchy.code;
                  (this.action as UpdateAttributeOverTimeAction).attributeDiff.valuesOverTime.push(this.diff);
              } else {
                  if (this.hierarchyEntry.endDate === endDate) {
                      return;
                  }

                  let immediateParent: GeoObject = this.hierarchyEntry.parents[this.component.hierarchy.types[this.component.hierarchy.types.length - 1].code].geoObject;
                  let oldValue: string = immediateParent == null ? null : immediateParent.properties.type + "_~VST~_" + immediateParent.properties.code;

                  this.diff = new ValueOverTimeDiff();
                  this.diff.action = "UPDATE";
                  this.diff.oid = this.hierarchyEntry.oid;
                  this.diff.oldValue = oldValue;
                  this.diff.oldStartDate = this.hierarchyEntry.startDate;
                  this.diff.oldEndDate = this.hierarchyEntry.endDate;
                  (this.action as UpdateAttributeOverTimeAction).attributeDiff.hierarchyCode = this.component.hierarchy.code;
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
          this.hierarchyEntry.endDate = endDate;
      }

      this.view.endDate = endDate;

      this.view.calculateSummaryKey(this.diff);

      this.component.onActionChange(this.action);
  }

  get endDate() {
      return this.view.endDate;
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
                  (this.action as UpdateAttributeOverTimeAction).attributeDiff.hierarchyCode = this.component.hierarchy.code;
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
                  (this.action as UpdateAttributeOverTimeAction).attributeDiff.hierarchyCode = this.component.hierarchy.code;
                  (this.action as UpdateAttributeOverTimeAction).attributeDiff.valuesOverTime.push(this.diff);
              }
          }

          let newValueStrConcat: string = null;
          if (directParent != null) {
              newValueStrConcat = directParent.properties.type + "_~VST~_" + directParent.properties.code;
          }

          if (newValueStrConcat === this.diff.oldValue) {
              delete this.diff.newValue;
              delete this.view.oldValue;
          } else {
              this.diff.newValue = newValueStrConcat;
              this.view.oldValue = this.diff.oldValue == null ? null : this.diff.oldValue.split("_~VST~_")[1];
          }

          this.diff.parents = parents;

          // If no changes have been made then remove the diff
          this.removeEmptyDiff();
      } else if (this.action.actionType === "CreateGeoObjectAction") {
          this.hierarchyEntry.parents = parents;
      }

      this.view.value.parents = parents;

      this.view.calculateSummaryKey(this.diff);

      this.component.onActionChange(this.action);
  }

  public getLowestLevelFromHierarchyEntry(parents: any): {geoObject: GeoObject, text: string} {
      let len = this.component.hierarchy.types.length;
      for (let i = len - 1; i >= 0; --i) {
          let type = this.component.hierarchy.types[i];

          if (Object.prototype.hasOwnProperty.call(parents, type.code) && parents[type.code].geoObject) {
              return parents[type.code];
          }
      }

      return null;
  }

  set value(val: any) {
      this.view.value = val;
  }

  get value() {
      return this.view.value;
  }

  public removeType(type): void {
      this.view.value.parents[type.code] = { text: "", geoObject: null };

      // Set the value to be the next existing ancestor.
      let entry = this.view.value;
      let len = this.component.hierarchy.types.length;
      for (let i = len - 1; i >= 0; --i) {
          let type = this.component.hierarchy.types[i];

          if (Object.prototype.hasOwnProperty.call(entry.parents, type.code) && entry.parents[type.code].geoObject) {
              this.setParentValue(type, this.view.value.parents);
              return;
          }
      }

      // If we do not have a next existing ancestor, then we must set the value to null.
      this.setParentValue(null, this.view.value.parents);
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
          if (parentCode == null) {
              let loopI = index;

              while (parentCode == null && loopI > 0) {
                  loopI = loopI - 1;

                  let parent = entry.parents[this.component.hierarchy.types[loopI].code];

                  if (parent != null) {
                      if (parent.geoObject != null && parent.geoObject.properties.code != null) {
                          parentCode = parent.geoObject.properties.code;
                          hierarchyCode = this.component.hierarchy.code;
                          parentTypeCode = this.component.hierarchy.types[loopI].code;
                      } else if (parent.goCode != null) {
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
          // First filter the response for ancestors of only the correct hierarchy
          ancestors.parents = ancestors.parents.filter(p => p.hierarchyType === this.component.hierarchy.code);

          delete entry.parents[type.code].goCode;
          entry.parents[type.code].geoObject = ancestors.geoObject;
          entry.parents[type.code].text = ancestors.geoObject.properties.displayLabel.localizedValue + " : " + ancestors.geoObject.properties.code;

          for (let i = 0; i < this.component.hierarchy.types.length; i++) {
              let current = this.component.hierarchy.types[i];
              let ancestor = ancestors;

              while (ancestor != null && ancestor.geoObject.properties.type !== current.code) {
                  if (ancestor.parents.length > 0) {
                      ancestor = ancestor.parents[0];
                  } else {
                      ancestor = null;
                  }
              }

              if (ancestor != null) {
                  entry.parents[current.code].geoObject = ancestor.geoObject;
                  entry.parents[current.code].text = ancestor.geoObject.properties.displayLabel.localizedValue + " : " + ancestor.geoObject.properties.code;
              }
          }

          this.setParentValue(type, entry.parents);
      });
  }

  createEmptyHierarchyEntry(): HierarchyOverTimeEntry {
      let hierarchyEntry = new HierarchyOverTimeEntry();
      hierarchyEntry.loading = {};
      hierarchyEntry.oid = uuid();

      hierarchyEntry.parents = {};

      for (let i = 0; i < this.component.hierarchy.types.length; i++) {
          let current = this.component.hierarchy.types[i];

          hierarchyEntry.parents[current.code] = { text: "", geoObject: null };

          hierarchyEntry.loading = {};
      }

      return hierarchyEntry;
  }

  onAddNewVersion(): void {
      this.view.summaryKey = SummaryKey.NEW;

      this.view.value = this.createEmptyHierarchyEntry();

      if (this.component.editAction instanceof CreateGeoObjectAction) {
          this.hierarchyEntry = this.createEmptyHierarchyEntry();

          this.hierarchyOverTime.entries.push(this.hierarchyEntry);
      }
  }

  recalculateView(): void {
      if (this.diff === null) {
          if (this.action.actionType === "UpdateAttributeAction") {
              if (this.hierarchyEntry != null) {
                  this.view.value = JSON.parse(JSON.stringify({ parents: this.hierarchyEntry.parents, loading: {} }));
                  this.view.startDate = this.hierarchyEntry.startDate;
                  this.view.endDate = this.hierarchyEntry.endDate;

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
              this.component.onActionChange(this.action);
              this.recalculateView();
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
              (this.action as UpdateAttributeOverTimeAction).attributeDiff.hierarchyCode = this.component.hierarchy.code;
          }
      } else if (this.action.actionType === "CreateGeoObjectAction") {
          let index = this.hierarchyOverTime.entries.findIndex(vot => vot.oid === this.hierarchyEntry.oid);

          if (index !== -1) {
              this.hierarchyOverTime.entries.splice(index, 1);
          }
      }

      this.view.calculateSummaryKey(this.diff);

      this.component.onActionChange(this.action);
  }

}
