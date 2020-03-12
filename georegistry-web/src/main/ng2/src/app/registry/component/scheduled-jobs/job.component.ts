import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { HttpErrorResponse } from '@angular/common/http';

import { ConfirmModalComponent } from '../../../shared/component/modals/confirm-modal.component';
import { JobConflictModalComponent } from './conflict-widgets/job-conflict-modal.component'

import Utils from '../../utility/Utils'

import { RegistryService } from '../../service/registry.service';
import { LocalizationService } from '../../../shared/service/localization.service';
import { AuthService } from '../../../shared/service/auth.service';

import { Conflict, ScheduledJob } from '../../model/registry';
import { ModalTypes } from '../../../shared/model/modal';

@Component( {
    selector: 'job',
    templateUrl: './job.component.html',
    styleUrls: ['./scheduled-jobs.css']
} )
export class JobComponent implements OnInit {
    message: string = null;
    job: ScheduledJob;
    allSelected: boolean = false;
    historyId: string = "";
    
    page: any = {
        count: 0,
        pageNumber: 1,
        pageSize: 10,
        results: []
    };
    
    /*
     * Reference to the modal current showing
    */
    bsModalRef: BsModalRef;

    isAdmin: boolean;
    isMaintainer: boolean;
    isContributor: boolean;

    constructor( public service: RegistryService, private modalService: BsModalService,
        private router: Router, private route: ActivatedRoute,
        private localizeService: LocalizationService, authService: AuthService ) {
        this.isAdmin = authService.isAdmin();
        this.isMaintainer = this.isAdmin || authService.isMaintainer();
        this.isContributor = this.isAdmin || this.isMaintainer || authService.isContributer();
    }

    ngOnInit(): void {

        this.historyId = this.route.snapshot.params["oid"];

        this.onPageChange(1);

    }
    
    
    formatValidationResolve(obj: any)
    {
      return JSON.stringify(obj);
    }


    getFriendlyProblemType(type: string): string {
        return Utils.getFriendlyProblemType(type)
    }


    onEdit( conflict: Conflict ): void {
        // this.router.navigate( ['/registry/master-list-history/', code] )

         this.bsModalRef = this.modalService.show( JobConflictModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        this.bsModalRef.content.conflict = conflict;
        this.bsModalRef.content.job = this.job;
        this.bsModalRef.content.onConflictAction.subscribe( data => {
            // do something with the return
        } );
    }

    onPageChange( pageNumber: any ): void {

        this.message = null;

        this.service.getScheduledJob(this.historyId, this.page.pageSize, pageNumber, true).then( response => {

            this.job = response;
            
            if (this.job.stage === 'IMPORT_RESOLVE')
            {
              this.page = this.job.importErrors;
            }
            else if (this.job.stage === 'VALIDATION_RESOLVE')
            {
              this.page = this.job.problems;
            }

        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );

    }

    onViewAllActiveJobs(): void {

    }

    onViewAllCompleteJobs(): void {

    }

    toggleAll(): void {
        this.allSelected = !this.allSelected;

        this.job.importErrors.results.forEach(row => {
            row.selected = this.allSelected;
        })
    }


    onResolveScheduledJob(historyId: string): void {
        this.bsModalRef = this.modalService.show( ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        this.bsModalRef.content.message = this.localizeService.decode( "confirm.modal.verify.delete" ) + ' [' + this.job.fileName + ']';
        this.bsModalRef.content.submitText = "Resolve all pending issues";
        this.bsModalRef.content.type = ModalTypes.danger;

         this.bsModalRef.content.onConfirm.subscribe( data => {

            this.service.resolveScheduledJob( historyId ).then( response => {

                this.page = {
                                count: 0,
                                pageNumber: 1,
                                pageSize: 10,
                                results: []
                            };

             } ).catch(( err: HttpErrorResponse ) => {
                 this.error( err );
             } );

         } );
    }

    error( err: HttpErrorResponse ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.error.localizedMessage || err.error.message || err.message );
        }
    }

}
