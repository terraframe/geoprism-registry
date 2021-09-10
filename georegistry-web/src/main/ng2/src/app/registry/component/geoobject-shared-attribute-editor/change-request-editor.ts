import { ActionTypes } from "@registry/model/constants";
import { AbstractAction, ChangeRequest, SummaryKey, UpdateAttributeOverTimeAction, ValueOverTimeDiff } from "@registry/model/crtable";
import { AttributeType, GeoObjectOverTime, HierarchyOverTime, HierarchyOverTimeEntry, ValueOverTime } from "@registry/model/registry";
import { VersionDiffView } from "./manage-versions-model";
import { ValueOverTimeCREditor } from "./ValueOverTimeCREditor";

export class ChangeRequestEditor {

    changeRequest: ChangeRequest;

    geoObject: GeoObjectOverTime;

    constructor(changeRequest: ChangeRequest, geoObject: GeoObjectOverTime) {
        this.changeRequest = changeRequest;
        this.geoObject = geoObject;
    }

    public getEditorsForAttribute(attribute: AttributeType, hierarchy: HierarchyOverTime, includeUnmodified: boolean = false): ValueOverTimeCREditor[] {
        let actions = this.changeRequest.getActionsForAttribute(attribute.code, hierarchy.code);

        let editAction: AbstractAction;
        if (actions.length === 0) {
            editAction = new UpdateAttributeOverTimeAction(attribute.code);
        } else {
            editAction = actions[actions.length - 1];
        }

        let editors: ValueOverTimeCREditor[] = [];

        // First, we have to create a view for every ValueOverTime object. This is done to simply display what's currently
        // on the GeoObject
        if (includeUnmodified || this.changeRequest == null || this.changeRequest.type === "CreateGeoObject" ||
          (this.changeRequest.approvalStatus !== "ACCEPTED" && this.changeRequest.approvalStatus !== "PARTIAL" && this.changeRequest.approvalStatus !== "REJECTED")) {
            if (attribute.code === "_PARENT_") {
                hierarchy.entries.forEach((entry: HierarchyOverTimeEntry) => {
                    let editor = new ValueOverTimeCREditor(this, editAction);

                    editor.oid = entry.oid;
                    editor.startDate = entry.startDate;
                    editor.endDate = entry.endDate;
                    editor.value = JSON.parse(JSON.stringify(entry));
                    editor.value.loading = {};

                    // In the corner case where this object isn't assigned to the lowest level, we may have
                    // empty values in our parents array for some of the types. Our front-end assumes there
                    // will always be an entry for all the types.
                    let len = hierarchy.types.length;
                    for (let i = 0; i < len; ++i) {
                        let type = hierarchy.types[i];

                        if (!Object.prototype.hasOwnProperty.call(view.value.parents, type.code)) {
                            view.value.parents[type.code] = { text: "", geoObject: null };
                        }
                    }

                    editors.push(editor);
                });
            } else {
                if (this.geoObject.attributes[attribute.code]) {
                    this.geoObject.attributes[attribute.code].values.forEach((vot: ValueOverTime) => {
                        let editor = new ValueOverTimeCREditor(this.changeRequest, attribute, editAction);

                        editor.oid = vot.oid;
                        editor.startDate = vot.startDate;
                        editor.endDate = vot.endDate;
                        editor.value = vot.value == null ? null : JSON.parse(JSON.stringify(vot.value));

                        editors.push(editor);
                    });
                }
            }
        }

        // Next, we must apply all changes which may exist in the actions.
        let len = actions.length;
        for (let i = 0; i < len; ++i) {
            let updateAttrAction: UpdateAttributeOverTimeAction = actions[i] as UpdateAttributeOverTimeAction;

            updateAttrAction.attributeDiff.valuesOverTime.forEach((votDiff: ValueOverTimeDiff) => {
                let index = editors.findIndex(editor => editor.oid === votDiff.oid);
                let editor = (index === -1) ? null : editors[index];

                if (votDiff.action === "DELETE") {
                    if (editor == null) {
                        editor = new ValueOverTimeCREditor(this.changeRequest, attribute, editAction);
                        editors.push(editor);

                        if (this.changeRequest == null || (this.changeRequest.approvalStatus !== "ACCEPTED" && this.changeRequest.approvalStatus !== "PARTIAL" && this.changeRequest.approvalStatus !== "REJECTED")) {
                            view.conflictMessage = [{ severity: "ERROR", message: this.lService.decode("changeovertime.manageVersions.missingReference"), type: ConflictType.MISSING_REFERENCE }];
                        }
                    }

                    this.populateViewFromDiff(attribute.code, hierarchy, view, votDiff);

                    delete view.oldValue;
                    delete view.oldStartDate;
                    delete view.oldEndDate;

                    // view.startDate = votDiff.oldStartDate;
                    // view.endDate = votDiff.oldEndDate;
                    // view.oid = votDiff.oid;
                    // view.value = votDiff.oldValue;

                    view.summaryKey = SummaryKey.DELETE;

                    view.editPropagator.diff = votDiff;
                } else if (votDiff.action === "UPDATE") {
                    if (view == null) {
                        view = new VersionDiffView(this, action);
                        viewModels.push(view);

                        if (this.changeRequest == null || (this.changeRequest.approvalStatus !== "ACCEPTED" && this.changeRequest.approvalStatus !== "PARTIAL" && this.changeRequest.approvalStatus !== "REJECTED")) {
                            view.conflictMessage = [{ severity: "ERROR", message: this.lService.decode("changeovertime.manageVersions.missingReference"), type: ConflictType.MISSING_REFERENCE }];
                        }
                    }

                    this.populateViewFromDiff(attribute.code, hierarchy, view, votDiff);

                    view.calculateSummaryKey(votDiff);
                } else if (votDiff.action === "CREATE") {
                    if (view != null) {
                        console.log("This action doesn't make sense. We're trying to create something that already exists?", votDiff);
                    } else {
                        view = new VersionDiffView(this, action);

                        this.populateViewFromDiff(attribute.code, hierarchy, view, votDiff);

                        editors.push(editor);
                    }
                }
            });
        }

        return editors;
    }

    populateViewFromDiff(typeCode: string, hierarchy: HierarchyOverTime, view: VersionDiffView, votDiff: ValueOverTimeDiff) {
        if (typeCode === "_PARENT_") {
            view.value = (view.editPropagator as HierarchyEditPropagator).createEmptyHierarchyEntry();
            view.value.oid = votDiff.oid;
            view.value.startDate = votDiff.newStartDate || votDiff.oldStartDate;
            view.value.endDate = votDiff.newEndDate || votDiff.oldEndDate;

            view.value.parents = votDiff.parents;

            if (!view.value.parents) {
                view.value.parents = {};
            }

            // In the corner case where this object isn't assigned to the lowest level, we may have
            // empty values in our parents array for some of the types. Our front-end assumes there
            // will always be an entry for all the types.
            let len = hierarchy.types.length;
            for (let i = 0; i < len; ++i) {
                let type = hierarchy.types[i];

                if (!Object.prototype.hasOwnProperty.call(view.value.parents, type.code)) {
                    view.value.parents[type.code] = { text: "", geoObject: null };
                }
            }

            view.value.loading = {};

            if (votDiff.oldValue != null) {
                let oldCodeArray: string[] = votDiff.oldValue.split("_~VST~_");
                // let oldTypeCode: string = oldCodeArray[0];
                let oldGoCode: string = oldCodeArray[1];

                view.oldValue = oldGoCode;

                let len = hierarchy.types.length;
                for (let i = len - 1; i >= 0; --i) {
                    let type = hierarchy.types[i];

                    if (votDiff.parents && Object.prototype.hasOwnProperty.call(votDiff.parents, type.code)) {
                        let lowestLevel = votDiff.parents[type.code];

                        if (lowestLevel.text == null || lowestLevel.text.length === 0) {
                            lowestLevel.text = oldGoCode;
                            break;
                        }
                    }
                }
            }
        } else {
            if (votDiff.newValue != null) {
                view.value = JSON.parse(JSON.stringify(votDiff.newValue));
                view.oldValue = votDiff.oldValue == null ? null : JSON.parse(JSON.stringify(votDiff.oldValue));
            } else {
                view.value = votDiff.oldValue == null ? null : JSON.parse(JSON.stringify(votDiff.oldValue));
            }
        }

        view.oid = votDiff.oid;
        view.startDate = votDiff.newStartDate || votDiff.oldStartDate;
        view.endDate = votDiff.newEndDate || votDiff.oldEndDate;
        if (votDiff.newStartDate !== votDiff.oldStartDate) {
            view.oldStartDate = votDiff.newStartDate == null ? null : votDiff.oldStartDate;
        }
        if (votDiff.newEndDate !== votDiff.oldEndDate) {
            view.oldEndDate = votDiff.newEndDate == null ? null : votDiff.oldEndDate;
        }
        view.editPropagator.diff = votDiff;
    }

}
