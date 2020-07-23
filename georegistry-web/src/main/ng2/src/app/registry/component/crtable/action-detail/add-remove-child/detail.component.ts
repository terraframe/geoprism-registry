import { Input, Component, OnInit, OnDestroy, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef, ViewEncapsulation, HostListener } from '@angular/core';
import { HttpHeaders, HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Router } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { ErrorModalComponent } from '../../../../../shared/component/modals/error-modal.component';

import { AddChildAction } from '../../../../model/crtable';
import { ChangeRequestService } from '../../../../service/change-request.service';
import { ComponentCanDeactivate } from "../../../../../shared/service/pending-changes-guard";

import { ErrorHandler } from '../../../../../shared/component/error-handler/error-handler';
import { ActionDetailComponent } from '../action-detail-modal.component';

declare var acp: any;
declare var $: any;

@Component( {

    selector: 'crtable-detail-add-remove-child',
    templateUrl: './detail.component.html',
    styleUrls: []
} )
export class AddRemoveChildDetailComponent implements ComponentCanDeactivate, ActionDetailComponent {

    @Input() action: AddChildAction;

    original: AddChildAction;

    readOnly: boolean = true;


    private bsModalRef: BsModalRef;

    constructor( private router: Router, private changeRequestService: ChangeRequestService, private modalService: BsModalService ) {

    }

    ngOnInit(): void {
        this.original = Object.assign( {}, this.action );
    }

    applyAction() {
        this.changeRequestService.applyAction( this.action ).then( response => {
            this.original = Object.assign( {}, this.action );

            this.unlockAction();
        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );
    }

    // Big thanks to https://stackoverflow.com/questions/35922071/warn-user-of-unsaved-changes-before-leaving-page
    @HostListener( 'window:beforeunload' )
    canDeactivate(): Observable<boolean> | boolean {
        if ( !this.readOnly ) {
            //event.preventDefault();
            //event.returnValue = 'Are you sure?';
            //return 'Are you sure?';

            return false;
        }

        return true;
    }

    afterDeactivate( isDeactivating: boolean ) {
        if ( isDeactivating && !this.readOnly ) {
            this.unlockActionSync();
        }
    }

    startEdit(): void {
        this.lockAction();
    }

    public endEdit(): void {
        this.unlockAction();
    }

    lockAction() {
        this.changeRequestService.lockAction( this.action.oid ).then( response => {
            this.readOnly = false;
        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );
    }

    unlockAction() {
        this.changeRequestService.unlockAction( this.action.oid ).then( response => {
            this.readOnly = true;

            this.action = this.original;
        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );
    }

    // https://stackoverflow.com/questions/4945932/window-onbeforeunload-ajax-request-in-chrome
    unlockActionSync() {
        $.ajax( {
            url: acp + '/changerequest/unlockAction',
            method: "POST",
            data: { actionId: this.action.oid },
            success: function( a ) {

            },
            async: false
        } );
    }

    onSelect( action: AddChildAction ) {
        this.action = action;
    }

    public error( err: HttpErrorResponse ): void {
            this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            this.bsModalRef.content.message = ErrorHandler.getMessageFromError(err);
    }

}
