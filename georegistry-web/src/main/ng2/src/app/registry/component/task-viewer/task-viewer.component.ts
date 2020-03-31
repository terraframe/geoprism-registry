import { Component, OnInit, Input, Output } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from "@angular/common/http";

import { ErrorModalComponent } from '../../../shared/component/modals/error-modal.component';

import { RegistryService } from '../../service/registry.service';
import { LocalizationService } from '../../../shared/service/localization.service';
import { AuthService } from '../../../shared/service/auth.service';

import { GeoObjectType, GeoObjectOverTime, Task } from '../../model/registry';

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

    inProgressTasks: Task[] = [];
    
    completedTasks: Task[] = [];
    
    message: string;

    constructor( private registryService: RegistryService,
                 private localizeService: LocalizationService,
                 private date: DatePipe,
                 private toEpochDateTimePipe: ToEpochDateTimePipe,
                 private authService: AuthService
      ) {
      //const day = this.forDate.getUTCDate();
      //this.dateStr = this.forDate.getUTCFullYear() + "-" + ( this.forDate.getUTCMonth() + 1 ) + "-" + ( day < 10 ? "0" : "" ) + day;
    }

    ngOnInit(): void {
      this.registryService.getMyTasks('UNRESOLVED').then( tasks => {
        this.inProgressTasks = tasks;
      } );
      this.registryService.getMyTasks('RESOLVED').then( tasks => {
        this.completedTasks = tasks;
      } );
    }
    
    onCompleteTask(task: any): void {
      this.registryService.completeTask(task.id).then( () => {
        this.inProgressTasks.splice(this.inProgressTasks.indexOf(task), 1);
      } );
    }
    
    getTitle(): string {
      return this.authService.getUsername() + "'s Tasks"; // TODO : Localize
    }

    private fetchGeoObjectType( code: string ) {
      this.registryService.getGeoObjectTypes( [code] )
        .then( geoObjectType => {
          this.geoObjectType = geoObjectType[0];
        } ).catch(( err: HttpErrorResponse ) => {
          console.log( err );
          //                this.error( err );
        } );
    }
}
