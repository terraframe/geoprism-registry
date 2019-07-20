import { Component, Input } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';
import { Action } from 'rxjs/scheduler/Action';
import { GeoObject, GeoObjectType } from '../../../model/registry';


@Component( {
    selector: 'action-detail-modal',
    templateUrl: './action-detail-modal.component.html',
    styleUrls: []
} )
export class ActionDetailModalComponent {

	action: any;

	@Input() 
	set curAction(action: any){
		this.action = action;
	}

    /*
     * Called on confirm
     */
    public onFormat: Subject<any>;

    constructor( public bsModalRef: BsModalRef ) { }

    ngOnInit(): void {

    }

    confirm(): void {
        this.bsModalRef.hide();
    }
}
