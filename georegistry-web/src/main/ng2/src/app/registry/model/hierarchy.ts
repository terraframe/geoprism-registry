import { LocalizedValue } from "@shared/model/core";

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

export class HierarchyNode {
	geoObjectType: string;
	children: HierarchyNode[];
	label: string;
	inherited: boolean;
}
