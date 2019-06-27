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

import { ComponentCanDeactivate } from '../../core/pending-changes-guard';

@Component( {

    selector: 'action-table',
    templateUrl: './action-table.component.html',
    styleUrls: ['./crtable.css']
} )
export class ActionTableComponent implements OnInit, ComponentCanDeactivate {
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
    
    unlockAction()
    {
      if (this.action != null)
      {
        this.service.unlockAction(this.action.oid).then( response => {
        
          } ).catch(( err: Response ) => {
              this.error( err.json() );
          } );
      }
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
      this.action = selected.selected[0];
      
      var detail = this.getActiveDetailComponent();
      if (detail != null)
      {
        detail.onSelect(this.action);
      }
    }
    
    canDeactivate(): Observable<boolean> | boolean {
      return this.getActiveDetailComponent().canDeactivate();
    }
    
    afterDeactivate(isDeactivating: boolean)
    {
      return this.getActiveDetailComponent().afterDeactivate(isDeactivating);
    }
    
    getActiveDetailComponent() : any {
      // TODO: I know this scales poorly to lots of different action types but I'm not sure how to do it better
      if (this.cuDetail != null && (this.action.actionType.endsWith('CreateGeoObjectAction') || this.action.actionType.endsWith('UpdateGeoObjectAction')))
      {
        return this.cuDetail;
      }
      if (this.arDetail != null && (this.action.actionType.endsWith('AddChildAction') || this.action.actionType.endsWith('RemoveChildAction')))
      {
        return this.arDetail;
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
