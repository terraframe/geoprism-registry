import { GeoObjectType, GeoObject } from './registry';
import { HierarchyType } from './hierarchy';

export class LocationInformation {
	types: GeoObjectType[];
	hierarchies: HierarchyType[];
	hierarchy?: string;
	entity?: GeoObject;
	childType?: string;
	geojson: {
		type: string;
		features: GeoObject[]
	}
}

