import { GeoObjectType } from '../hierarchy/hierarchy';

export class ImportSheet {
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


export class TermProblem {
    label: string;
    mdAttributeId: string;
    attributeLabel: string;
    action: any;
    resolved: boolean;
}

export class LocationProblem {
    label: string;
    type: string;
    typeLabel: string;
    parent: string;
    context: { label: string, type: string }[];
    action: any;
    resolved: boolean;
}

export class Exclusion {
    code: string;
    value: string;
}

export class GeoObjectSynonym {
    label: string;
    synonymId: string;
}

export class ImportConfiguration {
    type: GeoObjectType;
    sheet: ImportSheet;
    directory: string;
    filename: string;
    hierarchy: string;
    locations: Location[];
    locationProblems: LocationProblem[];
    termProblems: TermProblem[];
    exclusions: Exclusion[];
}

