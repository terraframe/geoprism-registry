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

/* eslint-disable no-unused-vars */
/* eslint-disable padded-blocks */
import { GeoObjectOverTime, HierarchyOverTime, GeoObjectType, AttributeType, GeoObject } from "./registry";
import { ActionTypes } from "./constants";
import { ValueOverTimeCREditor } from "@registry/component/geoobject-shared-attribute-editor/ValueOverTimeCREditor";

export enum SummaryKey {
    NEW = "NEW",
    UNMODIFIED = "UNMODIFIED",
    DELETE = "DELETE",
    UPDATE = "UPDATE",
    TIME_CHANGE = "TIME_CHANGE",
    VALUE_CHANGE = "VALUE_CHANGE",
}

export class Document {
    fileName: string;
    oid: string;
}

export class Geometry {
    type: string;
    coordinates: number[][] | number[];
}

export class ValueOverTimeDiff {
    oid: string;
    action: "DELETE" | "UPDATE" | "CREATE";
    oldValue: any;
    newValue: any;
    newStartDate: string;
    newEndDate: string;
    oldStartDate: string;
    oldEndDate: string;
    parents?: any;
    oldParents?: any;
}

// export class AbstractAction {
//    approvalStatus: string;
//    createActionDate: Date;
//    label: string;
//    oid: string;
//    actionType: string;
//    actionLabel: string;
//    decisionMaker?: string;
//    documents: Document[];
// }

export class AbstractAction {
    oid: string;
    actionType: ActionTypes;
    actionLabel: string;
    createActionDate: string;
    contributorNotes?: string;
    maintainerNotes?: string;
    additionalNotes?: string;
    approvalStatus: string;
    statusLabel: string;
    createdBy: string;
    documents: any[];
    permissions: string[];
    geoObjectJson?: GeoObjectOverTime;
    parentJson?: HierarchyOverTime[];
}

export class CreateGeoObjectAction extends AbstractAction {
    geoObjectJson: GeoObjectOverTime;
    parentJson: HierarchyOverTime[];

    constructor() {
        super();
        this.actionType = ActionTypes.CREATEGEOOBJECTACTION;
    }
}

export class UpdateAttributeOverTimeAction extends AbstractAction {
    attributeName: string;
    attributeDiff: { "valuesOverTime": ValueOverTimeDiff[], hierarchyCode?: string };

    constructor(attributeName: string) {
        super();
        this.actionType = ActionTypes.UPDATEATTRIBUTETACTION;
        this.attributeName = attributeName;
        this.attributeDiff = { valuesOverTime: [] };
    }
}

export class UpdateAttributeAction extends AbstractAction {
    attributeName: string;
    attributeDiff: { oldValue?: any, newValue?: any };

    constructor(attributeName: string) {
        super();
        this.actionType = ActionTypes.UPDATEATTRIBUTETACTION;
        this.attributeName = attributeName;
        this.attributeDiff = {};
    }
}

export class ChangeRequestCurrentObject {
    geoObjectType: GeoObjectType;
    hierarchies?: any;
}

export class UpdateChangeRequestCurrentObject {
    geoObjectType: GeoObjectType;
    geoObject: GeoObjectOverTime;
    hierarchies?: any;
}

export class ChangeRequest {
    oid: string;
    createdBy: string;
    createDate: Date;
    approvalStatus: string;
    total: number;
    pending: number;
    documents: Document[];
    actions: AbstractAction[];
    current?: ChangeRequestCurrentObject & UpdateChangeRequestCurrentObject;
    type: string; // Can be one of ["CreateGeoObject", "UpdateGeoObject"]
    statusLabel?: string;
    phoneNumber?: string;
    email?: string;
    permissions?: string[];
    isNew?: boolean;
    geoObject?: {
        code: string,
        label: string
    };
    geoObjectType?: {
        code?: string,
        label: string
    };
    contributorNotes: string;
    maintainerNotes: string;
    additionalNotes: string;
    
    constructor() {
        this.isNew = true;
    }

    public static getActionsForAttribute(cr: ChangeRequest, attributeName: string, hierarchyCode: string): AbstractAction[] {
        if (cr.type === "CreateGeoObject") {
            return cr.actions;
        } else {
            let newActions = [];

            for (let i = 0; i < cr.actions.length; ++i) {
                let action = cr.actions[i];

                if (action.actionType === "UpdateAttributeAction") {
                    let updateAttrAction = action as UpdateAttributeOverTimeAction;

                    if (updateAttrAction.attributeName === attributeName &&
                        (attributeName !== "_PARENT_" || updateAttrAction.attributeDiff.hierarchyCode === hierarchyCode)) {
                        newActions.push(cr.actions[i]);
                    }
                }
            }

            return newActions;
        }
    }
}

// export class UpdateGeoObjectAction extends AbstractAction {
//    geoObjectJson: GeoObjectOverTime;
// }
//
// export class CreateGeoObjectAction extends AbstractAction {
//    geoObjectJson: GeoObjectOverTime;
// }
//
// export class AddChildAction extends AbstractAction {
//    childId: string;
//    childTypeCode: string;
//    parentId: string;
//    parentTypeCode: string;
//    hierarchyTypeCode: string;
//    contributorNotes: string;
//    maintainerNotes: string;
//    createdBy: string;
// }

// export class RemoveChildAction extends AbstractAction {
//    childId: string;
//    childTypeCode: string;
//    parentId: string;
//    parentTypeCode: string;
//    hierarchyCode: string;
// }

// export class SetParentAction extends AbstractAction {
//    childCode: string;
//    childTypeCode: string;
//    json: HierarchyOverTime[];
// }

export class GovernanceStatus {
    key: string;
    label: string;
}

export class PageEvent {
    type: string;
    data: any;
}
