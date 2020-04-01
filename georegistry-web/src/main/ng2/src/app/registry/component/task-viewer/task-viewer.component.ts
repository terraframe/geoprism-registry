import { Component, OnInit, Input, Output } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from "@angular/common/http";

import { ErrorModalComponent } from '../../../shared/component/modals/error-modal.component';

import { RegistryService } from '../../service/registry.service';
import { TaskService } from '../../service/task.service';
import { LocalizationService } from '../../../shared/service/localization.service';
import { AuthService } from '../../../shared/service/auth.service';

import { GeoObjectType, GeoObjectOverTime, Task, PaginationPage } from '../../model/registry';

import { ToEpochDateTimePipe } from '../../pipe/to-epoch-date-time.pipe';

import { Observable } from 'rxjs';

declare var acp: string;


@Component( {
    selector: 'task-viewer',
    templateUrl: './task-viewer.component.html',
    styleUrls: ['./task-viewer.component.css'],
    providers: [DatePipe]
} )

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
    
    message: string;
    
    isViewAllOpen: boolean = false;
    
    activeTimeCounter: number = 0;
    completeTimeCounter: number = 0;
    
    pollingData: any;

    constructor( private registryService: RegistryService,
                 private taskService: TaskService,
                 private localizeService: LocalizationService,
                 private date: DatePipe,
                 private toEpochDateTimePipe: ToEpochDateTimePipe,
                 private authService: AuthService
      ) {
      //const day = this.forDate.getUTCDate();
      //this.dateStr = this.forDate.getUTCFullYear() + "-" + ( this.forDate.getUTCMonth() + 1 ) + "-" + ( day < 10 ? "0" : "" ) + day;
    }

    ngOnInit(): void {
      this.onInProgressTasksPageChange( 1 );
      
      this.pollingData = Observable.interval(1000).subscribe(() => {
        this.activeTimeCounter++
        this.completeTimeCounter++
      
        if (this.isViewAllOpen)
        {
          if (this.activeTimeCounter >= 4)
          {
            this.onInProgressTasksPageChange(this.inProgressTasks.pageNumber);
            
            this.activeTimeCounter = 0;
          }
          if (this.completeTimeCounter >= 7)
          {
            this.onCompletedTasksPageChange(this.completedTasks.pageNumber);
            
            this.completeTimeCounter = 0;
          }
        }
        else
        {
          if (this.activeTimeCounter >= 2)
          {
            this.onInProgressTasksPageChange(this.inProgressTasks.pageNumber);
            
            this.activeTimeCounter = 0;
          }
        }
      });
    }
    
    upper(str: string): string {
      if (str != null)
      {
        return str.toUpperCase();
      }
      else
      {
        return "";
      }
    }
    
    onInProgressTasksPageChange( pageNumber: any ): void {
      this.message = null;

      this.taskService.getMyTasks( pageNumber, this.inProgressTasks.pageSize, 'UNRESOLVED').then( page => {
        this.inProgressTasks = page;
      } );
    }
    
    onCompletedTasksPageChange( pageNumber: any ): void {
      this.message = null;

      this.taskService.getMyTasks( pageNumber, this.completedTasks.pageSize, 'RESOLVED').then( page => {
        this.completedTasks = page;
      } );
    }
    
    onCompleteTask(task: any): void {
      this.isViewAllOpen = true;
    
      this.taskService.completeTask(task.id).then( () => {
        this.inProgressTasks.results.splice(this.inProgressTasks.results.indexOf(task), 1);
        this.completedTasks.results.push(task);
        this.onCompletedTasksPageChange(1);
      } );
    }
    
    onMoveTaskToInProgress(task: any): void {
      this.isViewAllOpen = true;
    
      this.taskService.setTaskStatus(task.id, 'UNRESOLVED').then( () => {
        this.completedTasks.results.splice(this.completedTasks.results.indexOf(task), 1);
        this.inProgressTasks.results.push(task);
        this.onInProgressTasksPageChange(1);
      } );
    }
    
    onViewAllCompletedTasks(): void {
      this.isViewAllOpen = true;
      
      this.onCompletedTasksPageChange(1);
    }
}
