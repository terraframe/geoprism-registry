export class HierarchyType {
  code: string;
  localizedDescription: string;
  localizedLabel: string;
  rootGeoObjectTypes: HierarchyNode[];
}

export class Hierarchy {
  id: string;
  label: string;
}

//export class HierarchyNodeType {
//  id: string;
//  name: string;
//}

export class HierarchyNode {
	geoObjectType: string;
    children: HierarchyNode[];
    label: string; // added for angular-tree-widget
//    id: string; // added for angular-tree-widget
}

export class TreeEntity {
    id: string;
    name: string;
    hasChildren: boolean;
}

export class Term {

    constructor(public code:string, public localizedLabel:string, public localizedDescription:string){  
        this.code = code;
        this.localizedLabel = localizedLabel;
        this.localizedDescription = localizedDescription;
    }
    children: Term[] = [];

    addChild(term:Term) {
      this.children.push(term);
    }
}


// export class TermOption {
//     constructor(public code:string, public localizedLabel:string, public localizedDescription:string){  
//         this.code = code;
//         this.localizedLabel = localizedLabel;
//         this.localizedDescription = localizedDescription;
//     }
// }

export class GeoObject {
	type: string;
	geometry: any;
	properties: {
	  uid: string,
	  code: string,
	  localizedDisplayLabel: string,
	  type: string,
	  status: string
	};
}

export class GeoObjectType {
  code: string;
  localizedLabel: string;
  localizedDescription: string;
  geometryType: string;
  isLeaf: boolean;
  attributes: Attribute[];
}

// export class Attribute {
    
//   name: string;
//   type: string;
//   localizedLabel: string;
//   localizedDescription: string;
//   isDefault: boolean;
// }

// export class AttributeTerm extends Attribute {
//     descendants: Attribute[];
//     rootTerm: string;
// }

export class Attribute {

  constructor(public code:string, public type:string, public localizedLabel:string, public localizedDescription:string, public isDefault:boolean){
  
    this.code = code;
    this.type = type;
    this.localizedLabel = localizedLabel;
    this.localizedDescription = localizedDescription;
    this.isDefault = isDefault;
  }
  
}

export class AttributeTerm extends Attribute {
    //descendants: Attribute[];
    
    constructor(public code:string, public type:string, public localizedLabel:string, public localizedDescription:string, public isDefault:boolean){
      super(code, type, localizedLabel, localizedDescription, isDefault);
    }

    rootTerm: Term = new Term(null, null, null);

    termOptions :Term[] = [];

    setRootTerm(term:Term){
        this.rootTerm = term;
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