export enum ActionTypes {
    "CREATEGEOOBJECTACTION" = "CreateGeoObjectAction", 
    "UPDATEATTRIBUTETACTION" = "UpdateAttributeAction"
}

export enum GovernanceStatus {
    "PENDING" = "PENDING", 
    "ACCEPTED" = "ACCEPTED", 
    "REJECTED" = "REJECTED"
}

export enum GeoObjectTypeModalStates {
    "manageAttributes" = "MANAGE-ATTRIBUTES",
    "editAttribute" = "EDIT-ATTRIBUTE",
    "defineAttribute" = "DEFINE-ATTRIBUTE",
    "manageTermOption" = "MANAGE-TERM-OPTION",
    "editTermOption" = "EDIT-TERM-OPTION",
    "manageGeoObjectType" = "MANAGE-GEO-OBJECT-TYPE"
}

export enum ImportStrategy {
    "NEW_AND_UPDATE" = "NEW_AND_UPDATE",
    "NEW_ONLY" = "NEW_ONLY",
    "UPDATE_ONLY" = "UPDATE_ONLY"
}