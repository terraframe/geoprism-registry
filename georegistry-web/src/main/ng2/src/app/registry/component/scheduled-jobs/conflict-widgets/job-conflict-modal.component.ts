import { Component, OnInit } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { ScheduledJobOverview } from '@registry/model/registry';

import { RegistryService, IOService } from '@registry/service';

import { ErrorHandler } from '@shared/component';
import { LocalizationService } from '@shared/service';

@Component( {
    selector: 'job-conflict-modal',
    templateUrl: './job-conflict-modal.component.html',
    styleUrls: []
} )
export class JobConflictModalComponent implements OnInit {
    message: string = null;
    problem: any;
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
    
    onProblemResolvedListener(problem: any): void {
      this.onConflictAction.next({action:"RESOLVED", data: problem});
    }

    onCancel(): void {
        this.bsModalRef.hide()
    }

    error( err: HttpErrorResponse ): void {
            this.message = ErrorHandler.getMessageFromError(err);
    }

}
