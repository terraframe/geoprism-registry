import { ExternalSystem, Organization } from "@shared/model/core";
import { LOCALIZED_LABEL } from "@test/shared/mocks";

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

