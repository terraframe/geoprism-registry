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

import { ExternalSystem, Organization } from "@shared/model/core";
import { LOCALIZED_LABEL } from "@test/shared/mocks";
import { Email } from "@admin/model/email";
import { User, Account, Role } from "@admin/model/account";

export const EXTERNAL_SYSTEM: ExternalSystem = {
	id: "my-test-id",
	type: 'DHIS2ExternalSystem',
	organization: "test-org",
	label: LOCALIZED_LABEL,
	description: LOCALIZED_LABEL,
};

export const ORGANIZATION: Organization = {
	code: "test-org",
	label: LOCALIZED_LABEL,
	contactInfo: LOCALIZED_LABEL
};

export const EMAIL: Email = {
	oid: 'test-email-oid',
	server: 'test.terraframe.com',
	username: 'test.user',
	password: 'test.password',
	port: 8234,
	from: 'Test@terraframe.com',
	to: 'Test@terraframe.com'
};

export const USER: User = {
	oid: "TEST-USER-ID",
	username: "test",
	password: "test",
	firstName: "test",
	lastName: "test",
	email: "test@terraframe.com",
	phoneNumber: "15555555555",
	inactive: false,
	newInstance: false,
	roles: []
}

export const ACCOUNT: Account = {
	changePassword: false,
	user: USER,
	roles: []
}

export const ROLE: Role = {
	type: "RA",
	name: "Test.Role",
	label: LOCALIZED_LABEL,
	orgCode: "Role.Code",
	orgLabel: LOCALIZED_LABEL,
	geoObjectTypeCode: "TestType",
	geoObjectTypeLabel: LOCALIZED_LABEL,
	assigned: false
}
