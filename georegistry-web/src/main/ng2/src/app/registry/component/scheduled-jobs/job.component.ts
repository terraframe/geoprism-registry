import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { HttpErrorResponse } from '@angular/common/http';

import { ConfirmModalComponent } from '../../../shared/component/modals/confirm-modal.component';
import { JobConflictModalComponent } from './conflict-widgets/job-conflict-modal.component'

import { RegistryService } from '../../service/registry.service';
import { LocalizationService } from '../../../shared/service/localization.service';
import { AuthService } from '../../../shared/service/auth.service';

import { ScheduledJobDetail, Conflict } from '../../model/registry';

@Component( {
    selector: 'job',
    templateUrl: './job.component.html',
    styleUrls: ['./scheduled-jobs.css']
} )
export class JobComponent implements OnInit {
    message: string = null;
    job: ScheduledJobDetail;
    allSelected: boolean = false;
    
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

        // this.service.getScheduledJobs().then( response => {

        //     this.jobs = response;

        // } ).catch(( err: HttpErrorResponse ) => {
        //     this.error( err );
        // } );

        this.job = this.service.getScheduledJob();

    }


    onEdit( conflict: Conflict ): void {
        // this.router.navigate( ['/registry/master-list-history/', code] )

         this.bsModalRef = this.modalService.show( JobConflictModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        this.bsModalRef.content.conflict = conflict;
        this.bsModalRef.content.onConflictAction.subscribe( data => {
            // do something with the return
        } );
    }

    onViewAllActiveJobs(): void {

    }

    onViewAllCompleteJobs(): void {
        
    }

    toggleAll(): void {
        this.allSelected = !this.allSelected;

        this.job.rows.forEach(row => {
            row.selected = this.allSelected;
        })
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
