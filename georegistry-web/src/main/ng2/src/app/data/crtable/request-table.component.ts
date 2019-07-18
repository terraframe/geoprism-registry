import { Component, OnInit, EventEmitter, Output, ViewEncapsulation } from '@angular/core';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { ErrorModalComponent } from '../../core/modals/error-modal.component';
import { ChangeRequest, PageEvent, AbstractAction, AddChildAction, CreateGeoObjectAction, RemoveChildAction, UpdateGeoObjectAction } from './crtable';

import { ChangeRequestService } from '../../service/change-request.service';
import { LocalizationService } from '../../core/service/localization.service';

@Component( {

    selector: 'request-table',
    templateUrl: './request-table.component.html',
    styleUrls: ['./request-table.css'],
    encapsulation: ViewEncapsulation.None
} )
export class RequestTableComponent {

	objectKeys = Object.keys;

    @Output() pageChange = new EventEmitter<PageEvent>();

    bsModalRef: BsModalRef;

    rows: Observable<ChangeRequest[]>;

	request: ChangeRequest = null;
	
	requests: ChangeRequest[] = [];

	actions: AbstractAction[] | AddChildAction[] | CreateGeoObjectAction[] | RemoveChildAction[] | UpdateGeoObjectAction[];

    loading: boolean = false;

	columns: any[] = [];
	
	toggleId: string;

	filterCriteria: string = 'ALL';

    constructor( private service: ChangeRequestService, private modalService: BsModalService, private localizationService: LocalizationService ) {
        this.columns = [
            { name: localizationService.decode( 'change.request.user' ), prop: 'createdBy', sortable: false },
            { name: localizationService.decode( 'change.request.createDate' ), prop: 'createDate', sortable: false, width: 195 },
            { name: localizationService.decode( 'change.request.status' ), prop: 'approvalStatus', sortable: false }
        ];

        this.refresh();
    }

    refresh(): void {

        // this.rows = Observable.create(( subscriber: any ) => {

            // this.loading = true;

            this.service.getAllRequests("ALL").then( requests => {

				this.requests = requests;

                // this.loading = false;
            } ).catch(( response: Response ) => {
                this.error( response.json() );
            } )
        // } );

    }

    // onClick(): void {
    //     if ( this.request != null ) {
    //         this.pageChange.emit( {
    //             type: 'NEXT',
    //             data: this.request
    //         } );
    //     }
    // }

    onSelect( selected: any ): void {

		// this.request = selected.selected;

        this.service.getAllActions( selected.selected[0].oid ).then(actions => {
			
			this.actions = actions;
        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
    }

    onExecute(): void {

        if ( this.request != null ) {
            this.service.execute( this.request.oid ).then( request => {
                this.request = request;

                // TODO: Determine if there is a way to update an individual record
                this.refresh();
            } ).catch(( response: Response ) => {
                this.error( response.json() );
            } );
        }
	}
	
	onConfirmChangeRequest(request: any): void {

        if ( request != null ) {
            this.service.confirmChangeRequest( request.oid ).then( request => {
                this.request = request;

                // TODO: Determine if there is a way to update an individual record
                this.refresh();
            } ).catch(( response: Response ) => {
                this.error( response.json() );
            } );
        }
	}
	
	applyActionStatusProperties(action: any ): void {
		// var action = JSON.parse(JSON.stringify(this.action));
		// action.geoObjectJson = this.attributeEditor.getGeoObject();

		this.service.applyActionStatusProperties(action).then( response => {
			// this.crtable.refresh()
		} ).catch(( err: Response ) => {
			this.error( err.json() );
		} );
	}

    onApproveAll(request: any): void {

        if ( request != null ) {
            this.service.approveAllActions( request.oid ).then( request => {
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

                // TODO: Determine if there is a way to update an individual record
                this.refresh();
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
	
	requestTrackBy(index: number, request: ChangeRequest) {
    	return request.oid;
	}
	
	toggle(event: any, oid: string): void {

		if(!event.target.parentElement.className.includes("btn") && !event.target.className.includes("btn")){
			if(this.toggleId === oid){
				this.toggleId = null;
			}
			else {
				this.toggleId = oid;
				this.onSelect({selected:[{oid:oid}]});
			}
		}
	}

	filter(criteria: string): void {

		   this.service.getAllRequests(criteria).then( requests => {
				this.requests = requests;
            } ).catch(( response: Response ) => {
                this.error( response.json() );
			} )
			
		this.filterCriteria = criteria;
	}

	removeAction(action: AbstractAction): void {
		action.approvalStatus = "REJECTED";

		this.applyActionStatusProperties(action);
	}

	reinstateAction(action: AbstractAction): void {
		action.approvalStatus = "PENDING";

		this.applyActionStatusProperties(action);
	}

}
