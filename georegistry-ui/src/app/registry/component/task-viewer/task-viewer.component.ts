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

import { Component, OnInit, Input } from "@angular/core";
import { DatePipe } from "@angular/common";

import { TaskService } from "@registry/service";
import { DateService } from "@shared/service/date.service";
import { GeoObjectType } from "@registry/model/registry";

import { LocalizationService } from "@shared/service/localization.service";
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
