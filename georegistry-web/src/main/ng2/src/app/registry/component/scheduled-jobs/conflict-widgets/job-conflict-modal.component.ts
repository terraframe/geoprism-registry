import { Component, OnInit, EventEmitter, Output } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';
import { HttpErrorResponse } from '@angular/common/http';

import { ParentReferenceProblemWidgetComponent } from './parent-reference-problem-widget.component'
import { TermReferenceProblemWidgetComponent } from './term-reference-problem-widget.component'

import { GeoObjectType, MasterList, Conflict, ScheduledJob, ScheduledJobOverview } from '../../../model/registry';

import { RegistryService } from '../../../service/registry.service';

import { IOService } from '../../../service/io.service';
import { LocalizationService } from '../../../../shared/service/localization.service';

@Component( {
    selector: 'job-conflict-modal',
    templateUrl: './job-conflict-modal.component.html',
    styleUrls: []
} )
export class JobConflictModalComponent implements OnInit {
    message: string = null;
    conflict: Conflict;
    job: ScheduledJobOverview;
    
    /*
     * Observable subject for submission.  Called when an update is successful 
     */
    onConflictAction: Subject<any>;

    readonly: boolean = false;
    edit: boolean = false;


    constructor( private service: RegistryService, private iService: IOService, private lService: LocalizationService, public bsModalRef: BsModalRef ) {
      this.onConflictAction = new Subject();
    }

    ngOnInit(): void {
      
    }
    
    onProblemResolvedListener(conflict: any): void {
      this.onConflictAction.next({action:"RESOLVED", data: conflict});
    }

    onSubmit(): void {
        this.service.submitConflict( this.conflict ).then( response => {

            this.onConflictAction.next( response );
            this.bsModalRef.hide();
        } ).catch(( err: HttpErrorResponse) => {
            this.error( err );
        } );
    }

    onCancel(): void {
        this.bsModalRef.hide()
    }

    error( err: HttpErrorResponse ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.error.localizedMessage || err.error.message || err.message );
        }
    }

}
