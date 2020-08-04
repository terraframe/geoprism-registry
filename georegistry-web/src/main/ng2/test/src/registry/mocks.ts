import { Task, SynchronizationConfig, ExportScheduledJob, GeoObject, GeoObjectType } from "@registry/model/registry";
import { LocationInformation } from "@registry/model/location-manager";
import { HierarchyType } from "@registry/model/hierarchy";
import { LOCALIZED_LABEL } from "@test/shared/mocks";

export const TASK: Task = {
	id: "test-task",
	templateKey: "TEST",
	msg: "Task Message",
	title: "Task Title",
	status: "Task Status",
	createDate: Date.now(),
	completedDate: Date.now(),
}

export const SYNCH_CONFIG: SynchronizationConfig = {
	oid: "config-id",
	type: "DHIS2ExternalSystem",
	systemLabel: "Test System",
	organization: "test-org",
	system: "test-system",
	hierarchy: "test-hierarchy",
	label: LOCALIZED_LABEL,
	configuration: {}
}

export const EXPORT_JOB: ExportScheduledJob = {
	jobId: "TEST-JOB-ID",
	historyId: "HISTORY-ID",
	stage: "CONNECTING",
	status: "QUEUED",
	author: "TEST-AUTHOR",
	createDate: "12-30-2020",
	lastUpdateDate: "12-30-2020",
	workProgress: 5,
	workTotal: 10,
	startDate: "12-30-2020",
	endDate: "12-30-2020"
}

export const GEO_OBJECT: GeoObject = {
	type: "TEST-TYPE",
	geometry: {
		"type": "Point",
		"coordinates": [100.0, 0.0]
	},
	properties: {
		uid: "TEST-UID",
		code: "TEST-CODE",
		displayLabel: LOCALIZED_LABEL,
		type: "TEST-TYPE",
		status: ["PENDING"],
		sequence: "0",
		createDate: "12-30-2020",
		lastUpdateDate: "12-30-2020",
	},
}

export const GEO_OBJECT_TYPE: GeoObjectType = {
	code: "TEST-TYPE",
	label: LOCALIZED_LABEL,
	description: LOCALIZED_LABEL,
	geometryType: "POINT",
	isLeaf: false,
	isGeometryEditable: true,
	organizationCode: "TEST-ORG",
	attributes: []
}

export const HIERARCHY_TYPE: HierarchyType = {
	code: "TEST-HIERARCHY",
	description: LOCALIZED_LABEL,
	label: LOCALIZED_LABEL,
	rootGeoObjectTypes: [{
		geoObjectType: "TEST-CHILD-TYPE",
		children: [],
		label: "Test-Root"
	}],
	organizationCode: "TEST-ORG"
}


export const LOCATION_INFORMATION: LocationInformation = {
	types: [GEO_OBJECT_TYPE],
	hierarchies: [HIERARCHY_TYPE],
	hierarchy: "TEST-HIERARCHY",
	entity: null,
	childType: "Test-Root",
	geojson: {
		type: "Test-Root",
		features: [GEO_OBJECT]
	}
}


