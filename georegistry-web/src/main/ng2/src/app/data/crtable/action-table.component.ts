import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Headers, Http, RequestOptions, Response, URLSearchParams } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Router } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { PageEvent, ChangeRequest } from './crtable';

import { ErrorModalComponent } from '../../core/modals/error-modal.component';

import { ChangeRequestService } from '../../service/change-request.service';
import { LocalizationService } from '../../core/service/localization.service';

@Component( {

    selector: 'action-table',
    templateUrl: './action-table.component.html',
    styleUrls: ['./crtable.css']
} )
export class ActionTableComponent implements OnInit {
    @Output() pageChange = new EventEmitter<PageEvent>();
    @Input() request: ChangeRequest;

    bsModalRef: BsModalRef;

    rows: Observable<any[]>;

    selected: any = [];

    action: any = {};

    loading: boolean = false;
    columns: any[] = [];

    constructor( private service: ChangeRequestService, private modalService: BsModalService, private localizationService: LocalizationService ) {
        this.columns = [
            { name: localizationService.decode( 'change.request.action' ), prop: 'actionLabel', sortable: false },
            { name: localizationService.decode( 'change.request.createDate' ), prop: 'createActionDate', sortable: false, width: 195 },
            { name: localizationService.decode( 'change.request.status' ), prop: 'approvalStatus', sortable: false }
        ];
    }

    ngOnInit(): void {
        this.rows = Observable.create(( subscriber: any ) => {
            this.fetch(( data: any ) => {
                subscriber.next( data );
                subscriber.complete();
            } );
        } );
    }

    onBack(): void {
        this.pageChange.emit( {
            type: 'BACK',
            data: {}
        } );
    }

    refresh() {
        this.rows = Observable.create(( subscriber: any ) => {
            this.fetch(( data: any ) => {
                subscriber.next( data );
                subscriber.complete();
            } );
        } );

        this.selected = [];
        this.action = {};
    }

    fetch( cb: any ) {
        this.loading = true;

        this.service.fetchData( cb, this.request.oid ).then(() => {
            this.loading = false;
        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
    }

    onSelect( selected: any ) {
        this.action = selected.selected[0];
    }

    public error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            let bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            bsModalRef.content.message = ( err.localizedMessage || err.message );
        }
    }

}
