import { Component, OnInit, ViewChild, Input, Output, EventEmitter } from '@angular/core';
import {
  trigger,
  state,
  style,
  animate,
  transition
} from '@angular/animations'
import {NgControl, Validators, FormBuilder} from '@angular/forms';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { ButtonsModule } from 'ngx-bootstrap/buttons';

import { GeoObjectType, Attribute, ManageGeoObjectTypeModalState, GeoObjectTypeModalStates } from '../hierarchy';
import { Step, StepConfig } from '../../../core/modals/modal';

import { HierarchyService } from '../../../service/hierarchy.service';
import { ModalStepIndicatorService } from '../../../core/service/modal-step-indicator.service';
import { GeoObjectTypeManagementService } from '../../../service/geoobjecttype-management.service'

import { AttributeInputComponent} from '../geoobjecttype-management/attribute-input.component';

import { GeoObjectAttributeCodeValidator } from '../../../factory/form-validation.factory';

 
@Component( {
    selector: 'edit-attribute-modal-content',
    templateUrl: './edit-attribute-modal-content.component.html',
    styleUrls: [],
    animations: [
        trigger('openClose', 
            [
                transition(
                ':enter', [
                style({ 'opacity': 0}),
                animate('500ms', style({ 'opacity': 1}))
                ]
            ),
            transition(
                ':leave', [
                style({ 'opacity': 1}),
                animate('0ms', style({'opacity': 0})),
                
                ]
            )]
      )
    ]
} )
export class EditAttributeModalContentComponent implements OnInit {

    @Input() geoObjectType: GeoObjectType;
    @Input() attribute: Attribute;
    // @Input() modalState: ManageGeoObjectTypeModalState;
    // @Output() modalStateChange = new EventEmitter<ManageGeoObjectTypeModalState>();
    message: string = null;
    modalState: ManageGeoObjectTypeModalState = {"state":GeoObjectTypeModalStates.editAttribute, "attribute":this.attribute};
    modalStepConfig: StepConfig = {"steps": [
        {"label":"Manage GeoObjectType", "order":1, "active":true, "enabled":false},
        {"label":"Manage Attributes", "order":2, "active":true, "enabled":false},
        {"label":"Edit Attribute", "order":3, "active":true, "enabled":true}
    ]};

    @ViewChild(AttributeInputComponent) attributeInputComponent:AttributeInputComponent;

    constructor( private hierarchyService: HierarchyService, public bsModalRef: BsModalRef, private modalStepIndicatorService: ModalStepIndicatorService, private geoObjectTypeManagementService: GeoObjectTypeManagementService ) {
    }

    ngOnInit(): void {
        this.modalStepIndicatorService.setStepConfig(this.modalStepConfig);
    }

    ngAfterViewInit() {
   
    }

    ngOnDestroy(){
    }

    onModalStateChange(state: ManageGeoObjectTypeModalState): void {
        this.modalState = state;
    }

    handleOnSubmit(): void {
        
        this.hierarchyService.updateAttributeType( this.geoObjectType.code, this.attribute ).then( data => {
            
            // TODO: update attributes
            // this.modalStateChange.emit({"state":GeoObjectTypeModalStates.manageAttributes, "attribute":""});
            this.geoObjectTypeManagementService.setModalState({"state":GeoObjectTypeModalStates.manageAttributes, "attribute":""})
        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
    }

    isFormValid(): boolean {
        
        // let isAttrValid: boolean = this.attributeInputComponent.isValid();
        
        // if(isAttrValid){
        //     return true;
        // }

        // return false;
        return true
    }

    cancel(): void {
        // this.modalStateChange.emit({"state":GeoObjectTypeModalStates.manageAttributes, "attribute":""});
        this.geoObjectTypeManagementService.setModalState({"state":GeoObjectTypeModalStates.manageAttributes, "attribute":""})
    }

    back(): void {
        // this.modalState = {"state":GeoObjectTypeModalStates.editAttribute, "attribute":this.attribute}
        this.geoObjectTypeManagementService.setModalState({"state":GeoObjectTypeModalStates.manageAttributes, "attribute":""})
    }

    error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.localizedMessage || err.message );
            
            console.log(this.message);
        }
    }

}
