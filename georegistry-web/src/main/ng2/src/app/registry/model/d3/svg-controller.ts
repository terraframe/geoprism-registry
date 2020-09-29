import { GeoObjectType } from "../registry";
import { HierarchyType } from "../hierarchy";

export const DEFAULT_NODE_FILL = '#e6e6e6';
export const DEFAULT_NODE_BANNER_COLOR = '#A29BAB';
export const INHERITED_NODE_FILL = '#d4d4d4';
export const INHERITED_NODE_BANNER_COLOR = '#a0a0a0';
export const RELATED_NODE_BANNER_COLOR = INHERITED_NODE_BANNER_COLOR;

export class Instance {
	active: boolean;
	label: string;
}

export interface DropTarget {
	dropSelector: string;
	onDrag(dragEl: Element, dropEl: Element, event: any): void;
	onDrop(dragEl: Element, event: any): void;
	[others: string]: any;
}

export interface SvgController {
	findGeoObjectTypeByCode(gotCode: string): GeoObjectType;
	calculateRelatedHierarchies(got: GeoObjectType): string[];
	findHierarchyByCode(code: string): HierarchyType;
	localize(key: string): string;
	calculateSvgViewBox(): void;
	handleInheritHierarchy(hierarchyTypeCode: string, inheritedHierarchyTypeCode: string, geoObjectTypeCode: string): void;
	handleUninheritHierarchy(hierarchyTypeCode: string, geoObjectTypeCode: string): void;
}