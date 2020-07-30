import { Component, OnInit } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { BsModalService } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';
import { Subscription } from 'rxjs';
import { HttpErrorResponse } from "@angular/common/http";
import { ErrorHandler, ConfirmModalComponent } from '@shared/component';
import {  GeoObjectTypeModalStates, ManageGeoObjectTypeModalState, GeoObjectType } from '@registry/model/registry';

import { HierarchyService, GeoObjectTypeManagementService } from '@registry/service';


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
            this.message = ErrorHandler.getMessageFromError(err);
    }

}
