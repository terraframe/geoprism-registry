/* eslint-disable padded-blocks */
import { GeoObjectOverTime, HierarchyOverTime, GeoObjectType } from "./registry";
import { LocalizedValue } from "@shared/model/core";

export class Document {
    fileName: string;
    oid: string;
}

export class AttributeValuesOverTime {
    oid: string;
    action: string;
    oldValue: LocalizedValue | string;
    newValue: LocalizedValue | string;
    newStartDate: string;
    newEndDate: string;
    oldStartDate: string;
    oldEndDate: string;
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

export enum ActionTypes {
    "CREATEGEOOBJECTACTION" = "CreateGeoObjectAction", 
    "UPDATEATTRIBUTETACTION" = "UpdateAttributeAction"
}

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
}

export class UpdateAttributeAction extends AbstractAction {
    attributeName: string;
    attributeDiff: { "valuesOverTime": AttributeValuesOverTime[] };
}

export class ChangeRequestCurrentObject {
    geoObjectType: GeoObjectType;
}

export class UpdateChangeRequestCurrentObject extends ChangeRequestCurrentObject {
    geoObjectJson: GeoObjectOverTime;
}

export class ChangeRequest {
    oid: string;
    createdBy: string;
    createDate: Date;
    approvalStatus: string;
    total: number;
    pending: number;
    documents: Document[];
    actions: CreateGeoObjectAction[] | UpdateAttributeAction[];
    current: ChangeRequestCurrentObject | UpdateChangeRequestCurrentObject;
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

//export class SetParentAction extends AbstractAction {
//    childCode: string;
//    childTypeCode: string;
//    json: HierarchyOverTime[];
//}

export class GovernanceStatus {
    key: string;
    label: string;
}

export class PageEvent {
    type: string;
    data: any;
}


