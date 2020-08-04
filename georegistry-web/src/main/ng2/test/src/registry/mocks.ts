import { Task, SynchronizationConfig, ExportScheduledJob } from "@registry/model/registry";
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

