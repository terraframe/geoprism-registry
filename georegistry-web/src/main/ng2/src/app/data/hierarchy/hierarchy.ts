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
