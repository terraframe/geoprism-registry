import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { HttpErrorResponse } from '@angular/common/http';

import { ConfirmModalComponent } from '../../../shared/component/modals/confirm-modal.component';
import { JobConflictModalComponent } from './conflict-widgets/job-conflict-modal.component';
import { ReuploadModalComponent } from './conflict-widgets/reupload-modal.component';

import Utils from '../../utility/Utils'

import { RegistryService } from '../../service/registry.service';
import { LocalizationService } from '../../../shared/service/localization.service';
import { AuthService } from '../../../shared/service/auth.service';

import { ScheduledJob } from '../../model/registry';
import { ModalTypes } from '../../../shared/model/modal';
import { IOService } from '../../service/io.service';

import {Observable} from 'rxjs/Rx';

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
    
    timeCounter: number = 0;
    
    /*
     * Reference to the modal current showing
    */
    bsModalRef: BsModalRef;

    isAdmin: boolean;
    isMaintainer: boolean;
    isContributor: boolean;
    
    pollingData: any;
    isPolling: boolean = false;
    hasRowValidationProblem: boolean = false;

    constructor( public service: RegistryService, private modalService: BsModalService,
        private router: Router, private route: ActivatedRoute,
        private localizeService: LocalizationService, authService: AuthService, public ioService: IOService ) {
        this.isAdmin = authService.isAdmin();
        this.isMaintainer = this.isAdmin || authService.isMaintainer();
        this.isContributor = this.isAdmin || this.isMaintainer || authService.isContributer();
    }

    ngOnInit(): void {

        this.historyId = this.route.snapshot.params["oid"];

        this.onPageChange(1);
        
    }
    
    ngOnDestroy() {
      this.stopPolling();
    }
    
    formatAffectedRows(rows: string)
    {
      return rows.replace(/,/g, ", ");
    }
    
    formatValidationResolve(obj: any)
    {
      return JSON.stringify(obj);
    }

    onProblemResolved(problem: any): void {
      for (let i = 0; i < this.page.results.length; ++i)
      {
        let pageConflict = this.page.results[i];
        
        if (pageConflict.id === problem.id)
        {
          this.page.results.splice(i, 1);
        }
      }
    }

    getFriendlyProblemType(type: string): string {
        return Utils.getFriendlyProblemType(type)
    }


    onEdit( problem: any ): void {
        // this.router.navigate( ['/registry/master-list-history/', code] )

         this.bsModalRef = this.modalService.show( JobConflictModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        this.bsModalRef.content.problem = problem;
        this.bsModalRef.content.job = this.job;
        this.bsModalRef.content.onConflictAction.subscribe( data => {
          if (data.action === 'RESOLVED')
          {
            this.onProblemResolved(data.data);
          }
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
              
              for (let i = 0; i < this.page.results.length; ++i)
              {
                let problem = this.page.results[i];
                
                if (problem.type === 'RowValidationProblem')
                {
                  this.hasRowValidationProblem = true;
                }
              }
            }
            
            if (!this.isPolling && this.job.status === 'RUNNING')
            {
              this.startPolling();
            }
            else if (this.isPolling && this.job.status != 'RUNNING')
            {
              this.stopPolling();
            }

        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );

    }
    
    stopPolling(): void {
      if (this.isPolling && this.pollingData != null)
      {
        this.pollingData.unsubscribe();
      }
    }
    
    startPolling(): void {
      this.timeCounter = 0;
    
      this.pollingData = Observable.interval(1000).subscribe(() => {
        this.timeCounter++
        
        if (this.timeCounter >= 2)
        {
          this.onPageChange(this.page.pageNumber);
          
          this.timeCounter = 0;
        }
      });
      
      this.isPolling = true;
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

    onReuploadAndResume(historyId: string): void {
      this.bsModalRef = this.modalService.show( ReuploadModalComponent, {
          animated: true,
          backdrop: true,
          ignoreBackdropClick: true,
      } );
      
      this.bsModalRef.content.job = this.job;
  
      this.bsModalRef.content.onConfirm.subscribe( data => {
        this.router.navigate( ['/registry/scheduled-jobs'] )
      } );
    }

    onResolveScheduledJob(historyId: string): void {
      if (this.page.results.length == 0)
      {
        this.service.resolveScheduledJob( historyId ).then( response => {
          this.router.navigate( ['/registry/scheduled-jobs'] );
        } ).catch(( err: HttpErrorResponse ) => {
          this.error( err );
        } );
      }
      else
      {
        this.bsModalRef = this.modalService.show( ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        
        if (this.job.stage === 'VALIDATION_RESOLVE')
        {
          // this.bsModalRef.content.message = this.localizeService.decode( "etl.import.resume.modal.description" );
          this.bsModalRef.content.message = "Are you sure you want to resume the import?"; // TODO : Localize
          this.bsModalRef.content.submitText = this.localizeService.decode( "etl.import.resume.modal.button" );
        }
        else
        {
          this.bsModalRef.content.message = "Are you sure you want to complete the import? All unresolved problems will be ignored."; // TODO : Localize
          this.bsModalRef.content.submitText = "Complete Import"; // TODO : Localize
        }
        
        this.bsModalRef.content.type = ModalTypes.danger;
    
        this.bsModalRef.content.onConfirm.subscribe( data => {
    
          this.service.resolveScheduledJob( historyId ).then( response => {
    
              this.router.navigate( ['/registry/scheduled-jobs'] )
              
           } ).catch(( err: HttpErrorResponse ) => {
               this.error( err );
           } );
    
        } );
      }
    }
    
    onCancelScheduledJob(historyId: string): void {
      this.bsModalRef = this.modalService.show( ConfirmModalComponent, {
          animated: true,
          backdrop: true,
          ignoreBackdropClick: true,
      } );
      
      this.bsModalRef.content.message = this.localizeService.decode( "etl.import.cancel.modal.description" );
      this.bsModalRef.content.submitText = this.localizeService.decode( "etl.import.cancel.modal.button" );
      
      this.bsModalRef.content.type = ModalTypes.danger;
      
      this.bsModalRef.content.onConfirm.subscribe( data => {
      
        this.ioService.cancelImport( this.job.configuration ).then( response => {
          //this.bsModalRef.hide()
          this.router.navigate( ['/registry/scheduled-jobs'] )
        } ).catch(( err: HttpErrorResponse ) => {
          this.error( err );
        } );
  
      } );
    }

    error( err: HttpErrorResponse ): void {
      console.log("Encountered error", err);
    
        // Handle error
        if ( err !== null ) {
            this.message = ( err.error.localizedMessage || err.error.message || err.message );
        }
    }

}
