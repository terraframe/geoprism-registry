import { ActionTypes } from "@registry/model/constants";
import { AbstractAction, ChangeRequest, CreateGeoObjectAction, UpdateAttributeOverTimeAction, ValueOverTimeDiff } from "@registry/model/crtable";
import { AttributeType, GeoObjectType, HierarchyOverTime, HierarchyOverTimeEntry, ValueOverTime } from "@registry/model/registry";
import { ChangeRequestEditor } from "./change-request-editor";
import { ValueOverTimeCREditor } from "./ValueOverTimeCREditor";
import { HierarchyCREditor } from "./HierarchyCREditor";
import { v4 as uuid } from "uuid";
import { GeometryService } from "@registry/service";

export class ChangeRequestChangeOverTimeAttributeEditor {

    changeRequestEditor: ChangeRequestEditor;

    attribute: AttributeType;

    editAction: AbstractAction;

    hierarchy: HierarchyOverTime;

    editors: ValueOverTimeCREditor[];

    private _isValid: boolean;

    constructor(changeRequestEditor: ChangeRequestEditor, attribute: AttributeType, hierarchy: HierarchyOverTime) {
        this.changeRequestEditor = changeRequestEditor;
        this.attribute = attribute;
        this.hierarchy = hierarchy;

        this.getEditAction();
        this.editors = this.generateEditors();
    }

    getEditAction() {
        if (this.editAction == null) {
            let actions = ChangeRequest.getActionsForAttribute(this.changeRequestEditor.changeRequest, this.attribute.code, this.hierarchy == null ? null : this.hierarchy.code);

            if (actions.length === 0) {
                this.editAction = new UpdateAttributeOverTimeAction(this.attribute.code);

                if (this.attribute.code === "_PARENT_") {
                    (this.editAction as UpdateAttributeOverTimeAction).attributeDiff.hierarchyCode = this.hierarchy.code;
                }
            } else {
                this.editAction = actions[actions.length - 1];
            }
        }

        return this.editAction;
    }

    onChange() {
        // If our attribute action has changes it needs to be added to the ChangeRequest actions. Otherwise we can remove it.
        let hasChanges: boolean = this.hasChanges();

        let index = this.changeRequestEditor.changeRequest.actions.findIndex(action => this.editAction === action);

        if (index !== -1 && !hasChanges) {
            this.changeRequestEditor.changeRequest.actions.splice(index, 1);
        } else if (index === -1 && hasChanges) {
            this.changeRequestEditor.changeRequest.actions.push(this.editAction);
        }

        this.validate();
    }

    hasChanges(): boolean {
        let hasChanges: boolean = true;

        if (this.editAction.actionType === ActionTypes.UPDATEATTRIBUTETACTION) {
            let updateAction: UpdateAttributeOverTimeAction = this.editAction as UpdateAttributeOverTimeAction;

            if (updateAction.attributeDiff.valuesOverTime.length === 0) {
                hasChanges = false;
            }
        }

        return hasChanges;
    }

    public isValid(): boolean {
        return this._isValid;
    }

    validate(skipExists: boolean = false): boolean {
        let validEditors = this.validateEditors();
        let hasTimeConflict = this.changeRequestEditor.dateService.checkRanges(this.attribute, this.editors);

        let hasExistConflict = false;
        if (this.attribute.code !== "exists") {
            let existsAttribute: AttributeType = GeoObjectType.getAttribute(this.changeRequestEditor.geoObjectType, "exists");
            let existEditors = (this.changeRequestEditor.getEditorForAttribute(existsAttribute) as ChangeRequestChangeOverTimeAttributeEditor).getEditors();
            hasExistConflict = this.changeRequestEditor.dateService.checkExistRanges(this.editors, existEditors);
        } else if (!skipExists) {
            this.changeRequestEditor.validate(true); // If the exists attribute has changed we must revalidate all other attributes
        }

        this._isValid = validEditors && !hasTimeConflict && !hasExistConflict;

        return this._isValid;
    }

    private validateEditors(): boolean {
        let valid: boolean = true;

        this.editors.forEach(editor => {
            if (!editor.validate()) {
                valid = false;
            }
        });

        return valid;
    }

    findExistingValueOverTimeByOid(oid: string, attributeCode: string) {
        if (this.changeRequestEditor.geoObject.attributes[attributeCode]) {
            let index = this.changeRequestEditor.geoObject.attributes[attributeCode].values.findIndex((vot: ValueOverTime) => vot.oid === oid);

            if (index !== -1) {
                return this.changeRequestEditor.geoObject.attributes[attributeCode].values[index];
            }
        }

        return null;
    }

    public getEditors(includeUnmodified: boolean = true): ValueOverTimeCREditor[] {
        return this.editors.filter(editor => includeUnmodified || editor.diff != null);
    }

    generateEditors(): ValueOverTimeCREditor[] {
        let actions = ChangeRequest.getActionsForAttribute(this.changeRequestEditor.changeRequest, this.attribute.code, this.hierarchy == null ? null : this.hierarchy.code);

        let editors: ValueOverTimeCREditor[] = [];

        // First, we have to create a view for every ValueOverTime object. This is done to simply display what's currently
        // on the GeoObject
        if (this.changeRequestEditor.changeRequest == null || this.changeRequestEditor.changeRequest.type === "CreateGeoObject" ||
          (this.changeRequestEditor.changeRequest.approvalStatus !== "ACCEPTED" && this.changeRequestEditor.changeRequest.approvalStatus !== "PARTIAL" && this.changeRequestEditor.changeRequest.approvalStatus !== "REJECTED")) {
            if (this.attribute.code === "_PARENT_") {
                this.hierarchy.entries.forEach((entry: HierarchyOverTimeEntry) => {
                    let editor = new HierarchyCREditor(this, this.attribute, this.editAction, entry, this.hierarchy);

                    /*
                    editor.oid = entry.oid;
                    editor.startDate = entry.startDate;
                    editor.endDate = entry.endDate;
                    editor.value = JSON.parse(JSON.stringify(entry));
                    editor.value.loading = {};
                    */

                    editors.push(editor);
                });
            } else {
                if (this.changeRequestEditor.geoObject.attributes[this.attribute.code]) {
                    this.changeRequestEditor.geoObject.attributes[this.attribute.code].values.forEach((vot: ValueOverTime) => {
                        let editor = new ValueOverTimeCREditor(this, this.attribute, this.editAction);

                        editor.valueOverTime = vot;

                        editors.push(editor);
                    });
                }
            }
        }

        // Next, we must apply all changes which may exist in the actions.
        if (this.changeRequestEditor.changeRequest.type === "UpdateGeoObject") {
            let len = actions.length;
            for (let i = 0; i < len; ++i) {
                let updateAttrAction: UpdateAttributeOverTimeAction = actions[i] as UpdateAttributeOverTimeAction;

                updateAttrAction.attributeDiff.valuesOverTime.forEach((votDiff: ValueOverTimeDiff) => {
                    let index = editors.findIndex(editor => editor.oid === votDiff.oid);
                    let editor = (index === -1) ? null : editors[index];

                    if (editor == null) {
                        if (this.attribute.code === "_PARENT_") {
                            editor = new HierarchyCREditor(this, this.attribute, this.editAction, null, this.hierarchy);
                        } else {
                            editor = new ValueOverTimeCREditor(this, this.attribute, this.editAction);
                        }

                        editor.diff = votDiff;

                        editors.push(editor);
                    } else {
                        editor.diff = votDiff;

                        if (this.attribute.code === "_PARENT_") {
                            (editor as HierarchyCREditor).hierarchyEntry.parents = votDiff.parents;
                        }
                    }
                });
            }
        }

        return editors;
    }

    public createNewVersion(): ValueOverTimeCREditor {
        let editor: ValueOverTimeCREditor;

        // Create an instance of the appropriate editor object
        if (this.attribute.code === "_PARENT_") {
            editor = new HierarchyCREditor(this, this.attribute, this.editAction, null, this.hierarchy);
        } else {
            editor = new ValueOverTimeCREditor(this, this.attribute, this.editAction);
        }

        // If we're creating a new GeoObject, add it to that GeoObject
        if (this.changeRequestEditor.changeRequest.type === "CreateGeoObject") {
            if (this.attribute.code === "_PARENT_") {
                (editor as HierarchyCREditor).hierarchyEntry = (editor as HierarchyCREditor).createEmptyHierarchyEntry();
                (editor as HierarchyCREditor).hierarchyOverTime.entries.push((editor as HierarchyCREditor).hierarchyEntry);
            } else {
                let vot = new ValueOverTime();
                vot.oid = uuid();

                (this.editAction as CreateGeoObjectAction).geoObjectJson.attributes[this.attribute.code].values.push(vot);

                editor.valueOverTime = vot;
            }
        } else {
            editor.constructNewDiff("CREATE");
        }

        // Set any default values
        if (this.attribute.type === "local") {
            editor.value = this.changeRequestEditor.localizationService.create();
        } else if (this.attribute.type === "geometry") {
            let editors = this.getEditors(true);

            if (editors.length > 0) {
                editor.value = JSON.parse(JSON.stringify(editors[editors.length - 1].value));
            } else {
                editor.value = GeometryService.createEmptyGeometryValue(this.changeRequestEditor.geoObjectType.geometryType);
            }
        } else if (this.attribute.type === "term") {
            let terms = GeoObjectType.getGeoObjectTypeTermAttributeOptions(this.changeRequestEditor.geoObjectType, this.attribute.code);

            if (terms && terms.length > 0) {
                editor.value = terms[0].code;
            }
        } else if (this.attribute.code === "_PARENT_" && this.changeRequestEditor.changeRequest.type === "UpdateGeoObject") {
            (editor as HierarchyCREditor).hierarchyEntry = (editor as HierarchyCREditor).createEmptyHierarchyEntry();
        } else if (this.attribute.code !== "_PARENT_") {
            editor.value = null;
        }

        /*
        if (this.changeRequestEditor.changeRequest.type === "CreateGeoObject") {
            if (this.attribute.code === "_PARENT_") {
                this.hierarchy.entries.push((editor as HierarchyCREditor).hierarchyEntry);
            } else {
                this.changeRequestEditor.geoObject.attributes[this.attribute.code].values.push(editor.valueOverTime);
            }
        } else {
            editor.diff = new ValueOverTimeDiff();
            editor.diff.action = "CREATE";
            (this.editAction as UpdateAttributeOverTimeAction).attributeDiff.valuesOverTime.push(editor.diff);
        }
        */

        this.editors.push(editor);

        return editor;
    }

    public remove(editor: ValueOverTimeCREditor) {
        if ((editor.diff != null && editor.diff.action === "CREATE") || this.changeRequestEditor.changeRequest.type === "CreateGeoObject") {
            let index = this.editors.findIndex(find => find.oid === editor.oid);

            if (index !== -1) {
                this.editors.splice(index, 1);
            }
        }

        editor.remove();

        this.validate();
    }

}
