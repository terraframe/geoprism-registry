import { Component, OnInit, Input } from "@angular/core";
import { DatePipe } from "@angular/common";

import { TaskService } from "@registry/service";
import { DateService } from "@shared/service/date.service";
import { GeoObjectType } from "@registry/model/registry";

import { LocalizationService } from "@shared/service";
import { PageResult } from "@shared/model/core";

@Component({
    selector: "task-viewer",
    templateUrl: "./task-viewer.component.html",
    styleUrls: ["./task-viewer.component.css"],
    providers: [DatePipe]
})

export class TaskViewerComponent implements OnInit {

    @Input() geoObjectType: GeoObjectType;

    inProgressTasks: PageResult<any> = {
        count: 0,
        pageNumber: 1,
        pageSize: 10,
        resultSet: []
    };

    completedTasks: PageResult<any> = {
        count: 0,
        pageNumber: 1,
        pageSize: 10,
        resultSet: []
    };

    isViewAllOpen: boolean = false;

    activeTimeCounter: number = 0;
    completeTimeCounter: number = 0;

    pollingData: any;

    // eslint-disable-next-line no-useless-constructor
    constructor(private taskService: TaskService, private localizeService: LocalizationService, private dateService: DateService) { }

    ngOnInit(): void {
        this.onInProgressTasksPageChange(1);
    }

    onInProgressTasksPageChange(pageNumber: any): void {
        this.taskService.getMyTasks(pageNumber, this.inProgressTasks.pageSize, "UNRESOLVED").then(page => {
            this.inProgressTasks = page;
        });
    }

    onCompletedTasksPageChange(pageNumber: any): void {
        this.taskService.getMyTasks(pageNumber, this.completedTasks.pageSize, "RESOLVED").then(page => {
            this.completedTasks = page;
        });
    }

    onCompleteTask(task: any): void {
        // this.isViewAllOpen = true;

        this.taskService.completeTask(task.id).then(() => {
            const index = this.inProgressTasks.resultSet.findIndex(t => t.id === task.id);

            if (index !== -1) {
                this.inProgressTasks.resultSet.splice(index, 1);
            }

            if(this.isViewAllOpen) {
                this.onCompletedTasksPageChange(this.completedTasks.pageNumber);
            }
        });
    }

    onMoveTaskToInProgress(task: any): void {
        this.isViewAllOpen = true;

        this.taskService.setTaskStatus(task.id, "UNRESOLVED").then(() => {
            const index = this.completedTasks.resultSet.findIndex(t => t.id === task.id);

            if (index !== -1) {
                this.completedTasks.resultSet.splice(index, 1);
            }

            this.completedTasks.resultSet.splice(index, 1);
            this.inProgressTasks.resultSet.push(task);
        });
    }

    onToggleCompletedTasks(): void {
        this.isViewAllOpen = !this.isViewAllOpen;

        if (this.isViewAllOpen) {
            this.onCompletedTasksPageChange(1);
        }
    }
}
