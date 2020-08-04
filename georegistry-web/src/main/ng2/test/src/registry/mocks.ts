import { Task } from "@registry/model/registry";

export const TASK: Task = {
	id: "test-task",
	templateKey: "TEST",
	msg: "Task Message",
	title: "Task Title",
	status: "Task Status",
	createDate: Date.now(),
	completedDate: Date.now(),
}