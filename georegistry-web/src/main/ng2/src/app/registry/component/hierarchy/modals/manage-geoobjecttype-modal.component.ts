import { Component, OnInit } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { BsModalService } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';
import { Subscription } from 'rxjs';
import { ConfirmModalComponent } from '../../../../shared/component/modals/confirm-modal.component';
import { HttpErrorResponse } from "@angular/common/http";

import {  GeoObjectTypeModalStates, ManageGeoObjectTypeModalState, GeoObjectType } from '../../../model/registry';

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
    readOnly: boolean = false;

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

    error( err: HttpErrorResponse ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( (err.error && (err.error.localizedMessage || err.error.message)) || err.message || "An unspecified error has occurred" );
            
            console.log(this.message);
        }
    }

}
