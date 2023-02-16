export enum ActionTypes {
    "CREATEGEOOBJECTACTION" = "CreateGeoObjectAction",
    "UPDATEATTRIBUTETACTION" = "UpdateAttributeAction",
    "UPDATEGEOOBJECTACTION" = "UpdateGeoObjectAction" // Deprecated DO NOT USE
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

export enum ChangeType {
    END_DATE = "endDate",
    START_DATE = "startDate",
    VALUE = "value",
    REMOVE = "remove",
    ADD = "add"
}

export const OverlayerIdentifier = {
    LAYER_PANEL: "layer-panel",
    FEATURE_PANEL: "feature-panel",
    SEARCH_PANEL: "search-panel",
    VISUALIZER_PANEL: "visualizer-panel",
    LIST_MODAL: "list-modal"
} 