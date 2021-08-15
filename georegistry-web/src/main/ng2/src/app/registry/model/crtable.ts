/* eslint-disable no-unused-vars */
/* eslint-disable padded-blocks */
import { GeoObjectOverTime, HierarchyOverTime, GeoObjectType } from "./registry";
import { ActionTypes } from "./constants";

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
    action: string; // Can be one of DELETE, UPDATE, CREATE
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
}

export class CreateGeoObjectAction extends AbstractAction {
    geoObjectJson: GeoObjectOverTime;
    parentJson: HierarchyOverTime;

    constructor() {
        super();
        this.actionType = ActionTypes.CREATEGEOOBJECTACTION;
    }
}

export class UpdateAttributeAction extends AbstractAction {
    attributeName: string;
    attributeDiff: { "valuesOverTime": ValueOverTimeDiff[], hierarchyCode?: string };

    constructor(attributeName: string) {
        super();
        this.actionType = ActionTypes.UPDATEATTRIBUTETACTION;
        this.attributeName = attributeName;
        this.attributeDiff = { valuesOverTime: [] };
    }
}

export class ChangeRequestCurrentObject {
    geoObjectType: GeoObjectType;
}

export class UpdateChangeRequestCurrentObject {
    geoObjectType: GeoObjectType;
    geoObject: GeoObjectOverTime;
}

export class ChangeRequest {
    oid: string;
    createdBy: string;
    createDate: Date;
    approvalStatus: string;
    total: number;
    pending: number;
    documents: Document[];
    actions: CreateGeoObjectAction[] & UpdateAttributeAction[];
    current: ChangeRequestCurrentObject & UpdateChangeRequestCurrentObject;
    statusLabel?: string;
    phoneNumber?: string;
    email?: string;
    permissions?: string[];
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
