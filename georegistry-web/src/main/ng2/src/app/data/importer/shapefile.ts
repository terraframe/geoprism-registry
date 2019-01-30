import { GeoObjectType } from '../hierarchy/hierarchy';

export class ShapefileSheet {
    name: string;
    attributes: {
        boolean: string[];
        date: string[];
        numeric: string[];
        text: string[];
    }
}

export class Location {
    label: string;
    code: string;
    target: string;
}

export class ShapefileConfiguration {
    type: GeoObjectType;
    sheet: ShapefileSheet;
    directory: string;
    filename: string;
    hierarchy: string;
    locations: Location[];
}

export class TermProblem {
    label: string;
    mdAttributeId: string;
    attributeLabel: string;
    action: any;
    resolved: boolean;
}

