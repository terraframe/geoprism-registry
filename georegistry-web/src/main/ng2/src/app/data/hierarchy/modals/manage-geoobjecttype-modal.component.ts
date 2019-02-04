import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { BsModalService } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs/Subject';
import { Subscription } from 'rxjs';
import { ConfirmModalComponent } from '../../../core/modals/confirm-modal.component';

import {  GeoObjectTypeModalStates, ManageGeoObjectTypeModalState, GeoObjectType } from '../../hierarchy/hierarchy';
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
    modalState: ManageGeoObjectTypeModalState = {"state":GeoObjectTypeModalStates.manageGeoObjectType, "attribute":""};
    modalStateSubscription: Subscription;
    geoObjectType: GeoObjectType;

    constructor( public bsModalRef: BsModalRef, public confirmBsModalRef: BsModalRef, private geoObjectTypeManagementService: GeoObjectTypeManagementService ) {
      this.modalStateSubscription = geoObjectTypeManagementService.modalStepChange.subscribe( modalState => {
            this.modalState = modalState;
        })
    }

    ngOnInit(): void {

    }

    ngOnDestroy(){
        this.modalStateSubscription.unsubscribe();
    }

    // manageAttributes(): void {
    //     this.modalState = {"state":AttributeModalStates.manageAttributes, "attribute":""};
    // }

    onModalStateChange(state: any): void {
        this.modalState = state;

        console.log("state change")
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
