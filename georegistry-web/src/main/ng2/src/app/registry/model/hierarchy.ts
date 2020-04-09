import { LocalizedValue } from "../../shared/model/core";

export class HierarchyType {
  code: string;
  description: LocalizedValue;
  label: LocalizedValue;
  rootGeoObjectTypes: HierarchyNode[];
  organizationCode: string;
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
