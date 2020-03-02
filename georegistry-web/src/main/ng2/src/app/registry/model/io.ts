import { GeoObjectType } from './registry';

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

export class Term {
	code: string;
	label: string;
}

export class TermProblem {
	label: string;
	parentCode: string;
	mdAttributeId: string;
	attributeCode: string;
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

export class Synonym {
	label: string;
	synonymId: string;
	vOid?: string;
}

export class ImportConfiguration {
	type: GeoObjectType;
	sheet: ImportSheet;
	directory: string;
	filename: string;
	hierarchy: string;
	postalCode: boolean;
	hasPostalCode: boolean;
	locations: Location[];
	locationProblems: LocationProblem[];
	termProblems: TermProblem[];
	exclusions: Exclusion[];
	hierarchies: { code: string, label: string }[];
	startDate: string;
	endDate: string;
	parentLookupType: string;
}

