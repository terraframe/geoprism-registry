///
/// Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Geoprism Registry(tm).
///
/// Geoprism Registry(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Geoprism Registry(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { Task, SynchronizationConfig, ExportScheduledJob, GeoObject, GeoObjectType, MasterListByOrg, MasterList, MasterListVersion, PaginationPage } from "@registry/model/registry";
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
		label: "Test-Root",
		inheritedHierarchyCode: null
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

export const MASTER_LIST_BY_ORG: MasterListByOrg = {
	oid: "ORG-ID",
	label: "ORG-LABEL",
	write: true,
	lists: [
		{
			label: 'LIST-LABEL',
			oid: 'LIST-ID',
			createDate: '02/20/2020',
			lastUpdateDate: '02/20/2020',
			isMaster: false,
			write: true,
			read: true,
			visibility: 'PUBLIC'
		},
		{
			label: 'LIST-LABEL-2',
			oid: 'LIST-ID-2',
			createDate: '02/20/2020',
			lastUpdateDate: '02/20/2020',
			isMaster: false,
			write: true,
			read: true,
			visibility: 'PUBLIC'
		}
	]
}

export const MASTER_LIST_VERSION: MasterListVersion = {
	displayLabel: "Test Version",
	oid: "VERSION-OID",
	typeCode: "TEST-TYPE",
	orgCode: "ORG-ID",
	masterlist: 'LIST-ID',
	forDate: '02/20/2020',
	createDate: '02/20/2020',
	publishDate: '02/20/2020',
	attributes: [],
	isGeometryEditable: true,
	locales: ['en_us'],
	shapefile: true,
}


export const MASTER_LIST: MasterList = {
	oid: 'LIST-ID',
	typeCode: "TEST-TYPE",
	displayLabel: LOCALIZED_LABEL,
	code: 'LIST-CODE',
	representativityDate: new Date(),
	publishingStartDate: new Date(),
	publishDate: new Date(),
	listAbstract: "Test Abstract",
	process: "Test Process",
	progress: "Test Progress",
	accessConstraints: "Test Constraints",
	useConstraints: "Test Constraints",
	acknowledgements: "Test Acknowledgements",
	disclaimer: "Test Disclaimer",
	contactName: "Test Contact",
	organization: "TEST-ORG",
	telephoneNumber: "15555555555",
	email: "test@terraframe.com",
	hierarchies: [
		{
			label: "Around", code: "AROUND", parents: [
				{ label: "Country", code: "COUNTRY" },
				{ label: "Province", code: "PROVINCE" },
				{ label: "District", code: "DISTRICT" },
			]
		}
	],
	leaf: false,
	frequency: "ANNUAL",
	isMaster: false,
	visibility: "PUBLIC",
	write: true,
	read: true,
	versions: [MASTER_LIST_VERSION]
}

export function PAGINATION_PAGE(value: any, pageNumber?: number): PaginationPage {
	return {
		count: 1,
		pageNumber: pageNumber ? pageNumber : 1,
		pageSize: 10,
		results: [value]
	};
}

export const MOCK_SOCKET: WebSocket = {
	send: function() { },
} as any;

