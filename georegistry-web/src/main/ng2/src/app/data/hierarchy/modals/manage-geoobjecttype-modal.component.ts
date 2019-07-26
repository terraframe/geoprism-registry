import { Component, OnInit } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { BsModalService } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs/Subject';
import { Subscription } from 'rxjs';
import { ConfirmModalComponent } from '../../../core/modals/confirm-modal.component';

import {  GeoObjectTypeModalStates, ManageGeoObjectTypeModalState, GeoObjectType } from '../../../model/registry';
// import { StepConfig, Step } from '../../../core/modals/modal';

import { HierarchyService } from '../../../service/hierarchy.service';
import { GeoObjectTypeManagementService } from '../../../service/geoobjecttype-management.service'


@Component( {
    selector: 'manage-geoobjecttype-modal',
    templateUrl: './manage-geoobjecttype-modal.component.html',
    styleUrls: ['./manage-geoobjecttype-modal.css']
} )
export class ManageGeoObjectTypeModalComponent implements OnInit {
    message: string = null;
    modalState: ManageGeoObjectTypeModalState = {"state":GeoObjectTypeModalStates.manageGeoObjectType, "attribute":"", "termOption":""};
    modalStateSubscription: Subscription;
    geoObjectType: GeoObjectType;
    public onGeoObjectTypeSubmitted: Subject<GeoObjectType>;

    constructor( public bsModalRef: BsModalRef, public confirmBsModalRef: BsModalRef, private geoObjectTypeManagementService: GeoObjectTypeManagementService ) {
      this.modalStateSubscription = geoObjectTypeManagementService.modalStepChange.subscribe( modalState => {
            this.modalState = modalState;
      });
    }

    ngOnInit(): void {
        this.onGeoObjectTypeSubmitted = new Subject();
    }

    ngOnDestroy(){
        this.modalStateSubscription.unsubscribe();
    }

    onModalStateChange(state: any): void {
        this.modalState = state;
    }

    onGeoObjectTypeChange(data: any): void {
        // send persisted geoobjecttype to the parent calling component (hierarchy.component) so the 
        // updated GeoObjectType can be reflected in the template
        this.onGeoObjectTypeSubmitted.next( data );
    }

    update(): void {

    }

    close(): void {
        this.bsModalRef.hide();
    }

    error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.localizedMessage || err.message );
            
            console.log(this.message);
        }
    }

}
