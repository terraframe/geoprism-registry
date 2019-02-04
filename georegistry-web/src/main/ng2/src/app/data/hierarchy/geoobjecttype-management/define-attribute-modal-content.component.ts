import { Component, OnInit, AfterViewInit, ViewChild, ElementRef, Input, Output, EventEmitter } from '@angular/core';
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

import { ContextMenuService, ContextMenuComponent } from 'ngx-contextmenu';

import { GeoObjectType, Attribute, AttributeTerm, ManageGeoObjectTypeModalState, GeoObjectTypeModalStates } from '../hierarchy';
import { Step, StepConfig } from '../../../core/modals/modal';

import { HierarchyService } from '../../../service/hierarchy.service';
import { ModalStepIndicatorService } from '../../../core/service/modal-step-indicator.service';
import { GeoObjectTypeManagementService } from '../../../service/geoobjecttype-management.service'

import { AttributeInputComponent} from '../geoobjecttype-management/attribute-input.component';

import { GeoObjectAttributeCodeValidator } from '../../../factory/form-validation.factory';




@Component( {
    selector: 'define-attribute-modal-content',
    templateUrl: './define-attribute-modal-content.component.html',
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
export class DefineAttributeModalContentComponent implements OnInit {

    @Input() geoObjectType: GeoObjectType;
    // @Input() modalState: ManageGeoObjectTypeModalState;
    // @Output() modalStateChange = new EventEmitter<ManageGeoObjectTypeModalState>();
    message: string = null;
    newAttribute: Attribute = null;
    modalStepConfig: StepConfig = {"steps": [
        {"label":"Manage GeoObjectType", "order":1, "active":true, "enabled":false},
        {"label":"Manage Attributes", "order":2, "active":true, "enabled":false},
        {"label":"Create Attribute", "order":3, "active":true, "enabled":true}
    ]};
    modalState: ManageGeoObjectTypeModalState = {"state":GeoObjectTypeModalStates.defineAttribute, "attribute":""};

    @ViewChild(AttributeInputComponent) attributeInputComponent:AttributeInputComponent;


    constructor( private hierarchyService: HierarchyService, public bsModalRef: BsModalRef, private modalStepIndicatorService: ModalStepIndicatorService, private geoObjectTypeManagementService: GeoObjectTypeManagementService ) {
    
    }

    ngOnInit(): void {
        this.setAttribute("character");
        this.modalStepIndicatorService.setStepConfig(this.modalStepConfig);
    }

    ngAfterViewInit() {
   
    }

    ngOnDestroy(){
    }

    handleOnSubmit(): void {
        
        this.hierarchyService.addAttributeType( this.geoObjectType.code, this.newAttribute ).then( data => {
            this.geoObjectType.attributes.push(data);

            // this.modalStateChange.emit({"state":GeoObjectTypeModalStates.manageAttributes, "attribute":""});
            this.geoObjectTypeManagementService.setModalState({"state":GeoObjectTypeModalStates.manageAttributes, "attribute":""})
        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
    }

    setAttribute(type:string): void {
        if(type === 'term'){
            this.newAttribute = new AttributeTerm("", type, "", "", false);
        }
        else{
            this.newAttribute = new Attribute("", type, "", "", false);
        }

        this.attributeInputComponent.animate();
    }

    isFormValid(): boolean {
        
        let isAttrValid: boolean = this.attributeInputComponent.isValid();
        
        if(isAttrValid){
            return true;
        }

        return false;
    }
    
    cancel(): void {
        // this.modalStateChange.emit({"state":GeoObjectTypeModalStates.manageAttributes, "attribute":""});
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
