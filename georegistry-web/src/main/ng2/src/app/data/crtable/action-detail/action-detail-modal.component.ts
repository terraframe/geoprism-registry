import { Component, Input, ViewChild } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';
import { Action } from 'rxjs/scheduler/Action';
import { GeoObject, GeoObjectType } from '../../../model/registry';
import { CreateUpdateGeoObjectDetailComponent } from './create-update-geo-object/detail.component';

@Component( {
    selector: 'action-detail-modal',
    templateUrl: './action-detail-modal.component.html',
    styleUrls: []
} )
export class ActionDetailModalComponent {

	action: any;
	
	@ViewChild("cuDetail") cuDetail: CreateUpdateGeoObjectDetailComponent;

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
    
    cancel(): void {
      this.cuDetail.endEdit();
    
      this.bsModalRef.hide();
    }

    confirm(): void {
        this.bsModalRef.hide();
    }
}
