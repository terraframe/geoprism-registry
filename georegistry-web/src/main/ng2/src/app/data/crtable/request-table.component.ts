import { Component, OnInit, EventEmitter, Output } from '@angular/core';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { ErrorModalComponent } from '../../core/modals/error-modal.component';
import { ChangeRequest, PageEvent } from './crtable';

import { ChangeRequestService } from '../../service/change-request.service';
import { LocalizationService } from '../../core/service/localization.service';

@Component( {

    selector: 'request-table',
    templateUrl: './request-table.component.html',
    styleUrls: ['./crtable.css']
} )
export class RequestTableComponent {

    @Output() pageChange = new EventEmitter<PageEvent>();

    bsModalRef: BsModalRef;

    rows: Observable<ChangeRequest[]>;

    request: ChangeRequest = null;

    loading: boolean = false;

    columns: any[] = [];

    constructor( private service: ChangeRequestService, private modalService: BsModalService, private localizationService: LocalizationService ) {
        this.columns = [
            { name: localizationService.decode('change.request.user'), prop: 'createdBy', sortable: false },
            { name: localizationService.decode('change.request.createDate'), prop: 'createDate', sortable: false, width: 195 },
            { name: localizationService.decode('change.request.status'), prop: 'approvalStatus', sortable: false }
        ];

        this.rows = Observable.create(( subscriber: any ) => {

            this.loading = true;

            this.service.getAllRequests().then( requests => {
                subscriber.next( requests );

                this.loading = false;
            } ).catch(( response: Response ) => {
                this.error( response.json() );
            } )
        } );
    }

    onClick(): void {
        if ( this.request != null ) {
            this.pageChange.emit( {
                type: 'NEXT',
                data: this.request
            } );
        }
    }

    onSelect( selected: any ): void {

        if ( selected != null && selected.selected != null ) {
            const sel = selected.selected[0];

            this.service.getRequestDetails( sel.oid ).then( request => {
                this.request = request;
            } ).catch(( response: Response ) => {
                this.error( response.json() );
            } );
        }
    }

    onExecute(): void {

        if ( this.request != null ) {
            this.service.execute( this.request.oid ).then( request => {
                // Do nothing
            } ).catch(( response: Response ) => {
                this.error( response.json() );
            } );
        }
    }

    onApproveAll(): void {

        if ( this.request != null ) {
            this.service.approveAllActions( this.request.oid ).then( request => {
                this.request = request;
            } ).catch(( response: Response ) => {
                this.error( response.json() );
            } );
        }
    }

    onRejectAll(): void {
        if ( this.request != null ) {
            this.service.rejectAllActions( this.request.oid ).then( request => {
                this.request = request;
            } ).catch(( response: Response ) => {
                this.error( response.json() );
            } );
        }
    }

    public error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            let bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            bsModalRef.content.message = ( err.localizedMessage || err.message );
        }
    }

}
