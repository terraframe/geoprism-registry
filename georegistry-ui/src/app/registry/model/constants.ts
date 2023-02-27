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
