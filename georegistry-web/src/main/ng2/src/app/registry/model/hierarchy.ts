import { LocalizedValue } from "@shared/model/core";

export class HierarchyType {
	code: string;
	description: LocalizedValue;
	label: LocalizedValue;
	rootGeoObjectTypes: HierarchyNode[];
	organizationCode: string;
	abstractDescription?: string;
	progress?: string;
	acknowledgement?: string;
	contact?: string;
	accessConstraints?: string;
	useConstraints?: string;
}

export class Hierarchy {
	id: string;
	label: string;
}

export class HierarchyNode {
	geoObjectType: string;
	children: HierarchyNode[];
	label: string;
	inheritedHierarchyCode: string;
}
