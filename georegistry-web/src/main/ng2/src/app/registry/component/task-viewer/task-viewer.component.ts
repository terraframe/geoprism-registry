import { Component, OnInit, Input } from '@angular/core';
import { DatePipe } from '@angular/common';

import { TaskService } from '@registry/service';
import { GeoObjectType, PaginationPage } from '@registry/model/registry';

import { LocalizationService } from '@shared/service';

@Component({
	selector: 'task-viewer',
	templateUrl: './task-viewer.component.html',
	styleUrls: ['./task-viewer.component.css'],
	providers: [DatePipe]
})

export class TaskViewerComponent implements OnInit {

	@Input() geoObjectType: GeoObjectType;

	inProgressTasks: PaginationPage = {
		count: 0,
		pageNumber: 1,
		pageSize: 10,
		results: []
	};

	completedTasks: PaginationPage = {
		count: 0,
		pageNumber: 1,
		pageSize: 10,
		results: []
	};

	isViewAllOpen: boolean = false;

	activeTimeCounter: number = 0;
	completeTimeCounter: number = 0;

	pollingData: any;

	constructor(private taskService: TaskService, private localizeService: LocalizationService) { }

	ngOnInit(): void {
		this.onInProgressTasksPageChange(1);
	}

	upper(str: string): string {
		if (str != null) {
			return str.toUpperCase();
		}
		else {
			return "";
		}
	}

	onInProgressTasksPageChange(pageNumber: any): void {
		this.taskService.getMyTasks(pageNumber, this.inProgressTasks.pageSize, 'UNRESOLVED').then(page => {
			this.inProgressTasks = page;
		});
	}

	onCompletedTasksPageChange(pageNumber: any): void {

		this.taskService.getMyTasks(pageNumber, this.completedTasks.pageSize, 'RESOLVED').then(page => {
			this.completedTasks = page;
		});
	}

	onCompleteTask(task: any): void {
		// this.isViewAllOpen = true;

		this.taskService.completeTask(task.id).then(() => {

			const index = this.inProgressTasks.results.findIndex(t => t.id === task.id);

			if (index !== -1) {
				this.inProgressTasks.results.splice(index, 1);
			}

			this.completedTasks.results.push(task);
			// this.onCompletedTasksPageChange(1);
		});
	}

	onMoveTaskToInProgress(task: any): void {
		this.isViewAllOpen = true;

		this.taskService.setTaskStatus(task.id, 'UNRESOLVED').then(() => {

			const index = this.completedTasks.results.findIndex(t => t.id === task.id);

			if (index !== -1) {
				this.completedTasks.results.splice(index, 1);
			}

			this.completedTasks.results.splice(index, 1);
			this.inProgressTasks.results.push(task);
		});
	}

	onViewAllCompletedTasks(): void {
		this.isViewAllOpen = true;

		this.onCompletedTasksPageChange(1);
	}
	
	formatDate(date: string): string {
		return this.localizeService.formatDateForDisplay(date);
	}
}
