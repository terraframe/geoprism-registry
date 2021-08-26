
import { ChangeRequest, AbstractAction, UpdateAttributeAction } from "@registry/model/crtable";
import { StandardAttributeEditorComponent } from "./standard-attribute-editor.component";
import { ActionTypes } from "@registry/model/constants";

export class StandardAttributeCRModel {

    changeRequest: ChangeRequest;

    diff: { oldValue?: any, newValue?: any };

    component: StandardAttributeEditorComponent;

    editAction: AbstractAction;

    constructor(component: StandardAttributeEditorComponent, cr: ChangeRequest) {
        this.component = component;
        this.changeRequest = cr;
        this.initialize();
    }

    initialize(): void {
        let actions = this.changeRequest.actions;

        if (this.changeRequest.type === "CreateGeoObject") {
            if (actions.length > 0 && actions[0].actionType === ActionTypes.CREATEGEOOBJECTACTION) {
                this.editAction = actions[0];
            }
        } else {
            actions.forEach((action: AbstractAction) => {
                if (action.actionType === ActionTypes.UPDATEATTRIBUTETACTION) {
                    let updateAttrAction: UpdateAttributeAction = action as UpdateAttributeAction;

                    if (this.component.attributeType.code === updateAttrAction.attributeName) {
                        this.editAction = action;
                    }
                }
            });

            if (this.editAction == null) {
                this.editAction = new UpdateAttributeAction(this.component.attributeType.code);
            }
        }

        let len = this.changeRequest.actions.length;
        for (let i = 0; i < len; ++i) {
            let action: AbstractAction = actions[i];

            if (action.actionType === ActionTypes.UPDATEATTRIBUTETACTION) {
                let updateAttrAction: UpdateAttributeAction = action as UpdateAttributeAction;

                if (this.component.attributeType.code === updateAttrAction.attributeName) {
                    this.diff = updateAttrAction.attributeDiff;
                }
            } else if (action.actionType === ActionTypes.CREATEGEOOBJECTACTION) {
              // Nothing to do here. Create actions don't have diffs.
            } else {
                console.log("Unexpected action : " + action.actionType, action);
            }
        }
    }

    set value(val: any) {
        if (this.changeRequest.type === "CreateGeoObject") {
            this.component.postGeoObject.attributes[this.component.attributeType.code] = val;
        } else {
            if (this.diff != null) {
                if (this.areValuesEqual(this.diff.oldValue, val)) {
                    delete (this.editAction as UpdateAttributeAction).attributeDiff;
                    this.diff = null;

                    let index = this.changeRequest.actions.findIndex(findAction => findAction === this.editAction);
                    if (index !== -1) {
                        this.changeRequest.actions.splice(index, 1);
                    }
                } else {
                    this.diff.newValue = val;
                }
            } else {
                this.diff = { oldValue: this.component.postGeoObject.attributes[this.component.attributeType.code], newValue: val };

                (this.editAction as UpdateAttributeAction).attributeDiff = this.diff;
                this.changeRequest.actions.push(this.editAction);
            }
        }
    }

    get value(): any {
        if (this.changeRequest.type === "CreateGeoObject") {
            return this.component.postGeoObject.attributes[this.component.attributeType.code];
        } else {
            if (this.diff != null) {
                return this.diff.newValue;
            } else {
                return this.component.postGeoObject.attributes[this.component.attributeType.code];
            }
        }
    }

    areValuesEqual(val1: any, val2: any): boolean {
        if (this.component.attributeType.type === "boolean") {
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

        return val1 === val2;
    }

}
