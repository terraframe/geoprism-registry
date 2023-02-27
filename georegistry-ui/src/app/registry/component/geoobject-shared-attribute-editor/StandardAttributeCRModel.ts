///
/// Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Geoprism Registry(tm).
///
/// Geoprism Registry(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Geoprism Registry(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
///


import { ChangeRequest, AbstractAction, UpdateAttributeAction } from "@registry/model/crtable";
import { AttributeType, GeoObjectOverTime } from "@registry/model/registry";
import { ActionTypes } from "@registry/model/constants";

export class StandardAttributeCRModel {

    changeRequest: ChangeRequest;

    diff: { oldValue?: any, newValue?: any };

    attribute: AttributeType;

    geoObject: GeoObjectOverTime;

    editAction: AbstractAction;

    code: string;

    private _isValid: boolean = true;

    constructor(attr: AttributeType, geoObject: GeoObjectOverTime, cr: ChangeRequest) {
        this.attribute = attr;
        this.geoObject = geoObject;
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

                    if (this.attribute.code === updateAttrAction.attributeName) {
                        this.editAction = action;
                    }
                }
            });

            if (this.editAction == null) {
                this.editAction = new UpdateAttributeAction(this.attribute.code);
            }
        }

        let len = this.changeRequest.actions.length;
        for (let i = 0; i < len; ++i) {
            let action: AbstractAction = actions[i];

            if (action.actionType === ActionTypes.UPDATEATTRIBUTETACTION) {
                let updateAttrAction: UpdateAttributeAction = action as UpdateAttributeAction;

                if (this.attribute.code === updateAttrAction.attributeName) {
                    this.diff = updateAttrAction.attributeDiff;
                }
            } else if (action.actionType === ActionTypes.CREATEGEOOBJECTACTION) {
              // Nothing to do here. Create actions don't have diffs.
            } else {
                console.log("Unexpected action : " + action.actionType, action);
            }
        }
    }

    public hasChanges(): boolean {
        return this.diff != null;
    }

    isValid(): boolean {
        return this._isValid;
    }

    validate(): boolean {
        return this._isValid;
    }

    set value(val: any) {
        if (this.changeRequest.type === "CreateGeoObject") {
            this.geoObject.attributes[this.attribute.code] = val;
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
                this.diff = { oldValue: this.geoObject.attributes[this.attribute.code], newValue: val };

                (this.editAction as UpdateAttributeAction).attributeDiff = this.diff;
                this.changeRequest.actions.push(this.editAction);
            }
        }
    }

    get value(): any {
        if (this.changeRequest.type === "CreateGeoObject") {
            return this.geoObject.attributes[this.attribute.code];
        } else {
            if (this.diff != null) {
                return this.diff.newValue;
            } else {
                return this.geoObject.attributes[this.attribute.code];
            }
        }
    }

    areValuesEqual(val1: any, val2: any): boolean {
        if (this.attribute.type === "boolean") {
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
