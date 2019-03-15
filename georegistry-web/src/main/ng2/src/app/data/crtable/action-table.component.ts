import { Component, OnInit, ViewChild, Input, Output, EventEmitter } from '@angular/core';
import { Headers, Http, RequestOptions, Response, URLSearchParams } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Router } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { PageEvent, ChangeRequest, AbstractAction } from './crtable';

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
    
    @ViewChild('cuDetail') private cuDetail;
    @ViewChild('arDetail') private arDetail;

    bsModalRef: BsModalRef;

    rows: Observable<any[]>;

    selected: any = [];

    action: AbstractAction;
    
    loading: boolean = false;
    columns: any[] = [];

    constructor( private service: ChangeRequestService, private modalService: BsModalService, private localizationService: LocalizationService ) {
        this.columns = [
            { name: localizationService.decode( 'change.request.action' ), prop: 'actionLabel', sortable: false },
            { name: localizationService.decode( 'change.request.createDate' ), prop: 'createActionDate', sortable: false, width: 195 },
            { name: localizationService.decode( 'change.request.status' ), prop: 'statusLabel', sortable: false }
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
        this.action = null;
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
      var action: AbstractAction = selected.selected[0];
      
      this.lockAction( action );
      
      this.updateDetailAction(action);
    }
    
    updateDetailAction(action: AbstractAction)
    {
      // TODO: I know this scales poorly to lots of different action types but I'm not sure how to do it better
      if (this.cuDetail != null && (action.actionType.endsWith('CreateGeoObjectAction') || action.actionType.endsWith('UpdateGeoObjectAction')))
      {
        this.cuDetail.onSelect(action);
      }
      if (this.arDetail != null && (action.actionType.endsWith('AddChildAction') || action.actionType.endsWith('RemoveChildAction')))
      {
        this.arDetail.onSelect(action);
      }
    }
    
    lockAction( action: any )
    {
      if (this.action != null && this.action.oid != null)
      {
        this.service.unlockAction(this.action.oid).then( response => {
            
          } ).then( () => { this.service.lockAction(action.oid); } ).then( response => {
        	  this.action = action;
              
          } ).catch(( err: Response ) => {
              this.error( err.json() );
          } );
      }
      else
      {
        this.service.lockAction(action.oid).then( response => {
        	console.log("setting action to ", action);
            this.action = action;
          } ).catch(( err: Response ) => {
              this.error( err.json() );
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
