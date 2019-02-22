
export class TreeEntity {
    id: string;
    name: string;
    hasChildren: boolean;
}

export class LocaleValue {
    locale: string;
    value: string;
}

export class LocalizedValue {
    localizedValue: string;
    localeValues: LocaleValue[];
}

export class Term {
    code: string;
    label: LocalizedValue;
    description: LocalizedValue;

    constructor( code: string, label: string, description: string ) {
        this.code = code;
        this.label = { localizedValue: label, localeValues: [] };
        this.description = { localizedValue: description, localeValues: [] };
    }
    children: Term[] = [];

    addChild( term: Term ) {
        this.children.push( term );
    }
}

export class GeoObject {
    type: string;
    geometry: any;
    properties: {
        uid: string,
        code: string,
        displayLabel: LocalizedValue,
        type: string,
        status: string[],
        sequence: string
        createDate: string,
        lastUpdateDate: string,
    };
}

export class GeoObjectType {
    code: string;
    label: LocalizedValue;
    description: LocalizedValue;
    geometryType: string;
    isLeaf: boolean;
    attributes: Array<Attribute | AttributeTerm | AttributeDecimal> = [];
}

// export class Attribute {

//   name: string;
//   type: string;
//   label: string;
//   description: string;
//   isDefault: boolean;
// }

// export class AttributeTerm extends Attribute {
//     descendants: Attribute[];
//     rootTerm: string;
// }

export class Attribute {
    code: string;
    type: string;
    label: LocalizedValue;
    description: LocalizedValue;
    isDefault: boolean;

    constructor( code: string, type: string, label: LocalizedValue, description: LocalizedValue, isDefault: boolean ) {

        this.code = code;
        this.type = type;
        this.label = label;
        this.description = description;
        this.isDefault = isDefault;
    }

}

export class AttributeTerm extends Attribute {
    //descendants: Attribute[];

    constructor( code: string, type: string, label: LocalizedValue, description: LocalizedValue, isDefault: boolean ) {
        super( code, type, label, description, isDefault );
    }

    rootTerm: Term = new Term( null, null, null );

    termOptions: Term[] = [];

    setRootTerm( term: Term ) {
        this.rootTerm = term;
    }
}

export class AttributeDecimal extends Attribute {
    //descendants: Attribute[];

    constructor( code: string, type: string, label: LocalizedValue, description: LocalizedValue, isDefault: boolean ) {
        super( code, type, label, description, isDefault );
    }
}

export enum GeoObjectTypeModalStates {
    "manageAttributes" = "MANAGE-ATTRIBUTES",
    "editAttribute" = "EDIT-ATTRIBUTE",
    "defineAttribute" = "DEFINE-ATTRIBUTE",
    "manageTermOption" = "MANAGE-TERM-OPTION",
    "editTermOption" = "EDIT-TERM-OPTION",
    "manageGeoObjectType" = "MANAGE-GEO-OBJECT-TYPE"
}

export class ManageGeoObjectTypeModalState {
    state: string;
    attribute: any;
    termOption: any;
}