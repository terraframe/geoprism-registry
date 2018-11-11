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
    id: string; // added for angular-tree-widget
}

export class TreeEntity {
    id: string;
    name: string;
    hasChildren: boolean;
}

export class Term {
	code: string;
    localizedLabel: string;
    children: Term[];
}

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

export class Attribute {
  name: string;
  type: string;
  localizedLabel: string;
  localizedDescription: string;
}