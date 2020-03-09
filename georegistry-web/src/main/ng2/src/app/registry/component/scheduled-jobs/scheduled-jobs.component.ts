import { Component, OnInit, Input } from '@angular/core';
import { Router } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { HttpErrorResponse } from '@angular/common/http';

import { ConfirmModalComponent } from '../../../shared/component/modals/confirm-modal.component';

import { RegistryService } from '../../service/registry.service';
import { LocalizationService } from '../../../shared/service/localization.service';
import { AuthService } from '../../../shared/service/auth.service';

import { ScheduledJob, Step, StepConfig, ScheduledJobOverview } from '../../model/registry';

@Component( {
    selector: 'scheduled-jobs',
    templateUrl: './scheduled-jobs.component.html',
    styleUrls: ['./scheduled-jobs.css']
} )
export class ScheduledJobsComponent implements OnInit {
    message: string = null;

    activeJobsPage: any = {
        count: 0,
        pageNumber: 1,
        pageSize: 10,
        results: []
    };

    completeJobsPage: any = {
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

    constructor( public service: RegistryService, private modalService: BsModalService, private router: Router,
        private localizeService: LocalizationService, authService: AuthService ) {
        this.isAdmin = authService.isAdmin();
        this.isMaintainer = this.isAdmin || authService.isMaintainer();
        this.isContributor = this.isAdmin || this.isMaintainer || authService.isContributer();
    }

    ngOnInit(): void {

        this.onActiveJobsPageChange( 1 );

    }

    formatStepConfig(jobs: ScheduledJob[]): ScheduledJobOverview[] {

        let config: ScheduledJobOverview[] = [];
        jobs.forEach(job => {
            let jobConfig = {
                fileName: job.fileName,
                historyId: job.historyId,
                stage: job.stage,
                status: job.status,
                author: job.author,
                createDate: job.createDate,
                lastUpdateDate: job.lastUpdateDate,
                workProgress: job.workProgress,
                workTotal: job.workTotal,
                "stepConfig": {"steps": [
                    {"label":"File Import", "complete":true, "enabled":false},

                    {"label":"Staging",
                        "complete":job.stage === "NEW" ? false : true,
                        "enabled":job.stage === "NEW" ? true : false
                    },

                    {"label":"Validation",
                        "complete":job.stage === "VALIDATE" || job.stage === "VALIDATION_RESOLVE" ? false : true,
                        "enabled":job.stage === "VALIDATE" || job.stage === "VALIDATION_RESOLVE" ? true : false
                    },

                    {"label":"Database Import",
                        "complete":job.stage === "IMPORT" || job.stage === "IMPORT_RESOLVE" || job.stage === "RESUME_IMPORT" ? true : false,
                        "enabled":job.stage === "IMPORT" || job.stage === "IMPORT_RESOLVE" || job.stage === "RESUME_IMPORT" ? true : false
                    }
                ]}
            }

            config.push(jobConfig);
        });

        return config;
    }


    onViewAllCompleteJobs(): void {
        this.onCompleteJobsPageChange(1);
    }


    onView( code: string ): void {
        this.router.navigate( ['/registry/master-list-history/', code] )
    }

    onActiveJobsPageChange( pageNumber: any ): void {

        this.message = null;

        this.service.getScheduledJobs(this.activeJobsPage.pageSize, pageNumber, "createDate", true).then( response => {

            this.activeJobsPage.results = this.formatStepConfig(response);
            this.activeJobsPage.count = this.activeJobsPage.results.length;

            // if(pageNumber > this.activeJobsPage.pageNumber){
            //     this.activeJobsPage.pageNumber = this.activeJobsPage.pageNumber + 1;
            // }
            // else if(pageNumber < this.activeJobsPage.pageNumber){
            //      this.activeJobsPage.pageNumber = this.activeJobsPage.pageNumber - 1;
            // }

        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );
    }

    onCompleteJobsPageChange( pageNumber: any ): void {

        this.message = null;

        this.service.getScheduledJobs(this.completeJobsPage.pageSize, pageNumber, "createDate", true).then( response => {

            this.completeJobsPage.results = response;
            this.completeJobsPage.count = this.completeJobsPage.results.length;

            // if(pageNumber > this.completeJobsPage.pageNumber){
            //     this.completeJobsPage.pageNumber = this.completeJobsPage.pageNumber + 1;
            // }
            // else if(pageNumber < this.completeJobsPage.pageNumber){
            //      this.completeJobsPage.pageNumber = this.completeJobsPage.pageNumber - 1;
            // }

        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );
    }


    onDelete( list: { label: string, oid: string } ): void {
        this.bsModalRef = this.modalService.show( ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        this.bsModalRef.content.message = this.localizeService.decode( "confirm.modal.verify.delete" ) + ' [' + list.label + ']';
        this.bsModalRef.content.submitText = this.localizeService.decode( "modal.button.delete" );

         this.bsModalRef.content.onConfirm.subscribe( data => {
             
            // this.service.deleteMasterList( list.oid ).then( response => {
            //      this.lists = this.lists.filter(( value, index, arr ) => {
            //          return value.oid !== list.oid;
            //      } );

            //  } ).catch(( err: HttpErrorResponse ) => {
            //      this.error( err );
            //  } );

         } );
    }

    error( err: HttpErrorResponse ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.error.localizedMessage || err.error.message || err.message );
        }
    }

}
