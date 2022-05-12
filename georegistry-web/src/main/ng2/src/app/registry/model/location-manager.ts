/* eslint-disable padded-blocks */
import { GeoObjectType, GeoObject } from "./registry";
import { HierarchyType } from "./hierarchy";

export class LocationInformation {
    types: GeoObjectType[];
    hierarchies: HierarchyType[];
    hierarchy?: string;
    entity?: GeoObject;
    childType?: string;
    geojson: {
        type: string;
        features: GeoObject[]
    }
}

export class ModalState {
    SEARCH: number;
    VIEW: number;
    BUSINESS: number;
}

export const PANEL_SIZE_STATE = {
    MINIMIZED: 0,
    WINDOWED: 1,
    FULLSCREEN: 2
};
