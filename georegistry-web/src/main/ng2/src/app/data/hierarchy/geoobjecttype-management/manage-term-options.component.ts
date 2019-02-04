import { Component, OnInit, AfterViewInit, ElementRef, Input, Output, EventEmitter, ChangeDetectorRef } from '@angular/core';
import {
  trigger,
  state,
  style,
  animate,
  transition
} from '@angular/animations'
import {NgControl, Validators, FormBuilder} from '@angular/forms';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { BsModalService } from 'ngx-bootstrap/modal';

import { ConfirmModalComponent } from '../../../core/modals/confirm-modal.component';
import { ErrorModalComponent } from '../../../core/modals/error-modal.component';
import { LocalizationService } from '../../../core/service/localization.service';
import { GeoObjectTypeManagementService } from '../../../service/geoobjecttype-management.service'

import { GeoObjectType, AttributeTerm, Term, ManageGeoObjectTypeModalState, GeoObjectTypeModalStates} from '../hierarchy';
import { ModalTypes, StepConfig } from '../../../core/modals/modal';

import { HierarchyService } from '../../../service/hierarchy.service';
import { ModalStepIndicatorService } from '../../../core/service/modal-step-indicator.service';

import { GeoObjectAttributeCodeValidator } from '../../../factory/form-validation.factory';


 
@Component( {
    selector: 'manage-term-options',
    templateUrl: './manage-term-options.component.html',
    styleUrls: ['./manage-term-options.css'],
    animations: [
        trigger('toggleInputs', [
            state('none, void', 
                style({ 'opacity': 0})
              ),
              state('show', 
                style({ 'opacity': 1})
              ),
              transition('none => show', animate('300ms')),
              transition('show => none', animate('100ms'))
        ]),
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
export class ManageTermOptionsComponent implements OnInit {

    @Input() geoObjectType: GeoObjectType;
    @Input() attribute: AttributeTerm;
    @Output() attributeChange = new EventEmitter<AttributeTerm>();
    message: string = null;
    termOptionCode: string = "";
    termOptionLabel: string = "";
    termOptionDescription: string = "";
    state: string = 'none';
    enableTermOptionForm = false;
    modalStepConfig: StepConfig = {"steps": [
      {"label":this.localizeService.decode("modal.step.indicator.manage.geoobjecttype"), "active":true, "enabled":false},
        {"label":this.localizeService.decode("modal.step.indicator.manage.attributes"), "active":true, "enabled":false},
        {"label":this.localizeService.decode("modal.step.indicator.edit.attribute"), "active":true, "enabled":false},
        {"label":this.localizeService.decode("modal.step.indicator.manage.term.options"), "active":true, "enabled":true}
    ]};

    constructor( private hierarchyService: HierarchyService, public bsModalRef: BsModalRef, private cdr: ChangeDetectorRef, private geoObjectTypeManagementService: GeoObjectTypeManagementService,
            private modalService: BsModalService, private localizeService: LocalizationService, private modalStepIndicatorService: ModalStepIndicatorService ) {
    }

    ngOnInit(): void {
        this.modalStepIndicatorService.setStepConfig(this.modalStepConfig);
    }

    ngAfterViewInit() {
        this.state = 'show';
        this.cdr.detectChanges();
    }

    ngOnDestroy(){
    
    }

    handleOnSubmit(): void {
        
    }

    animate(): void {
        this.state = "none";
    }

    onAnimationDone(event: AnimationEvent): void {
        this.state = "show";
    }

    isValid(): boolean {
        if(this.termOptionCode && this.termOptionLabel){
            
            // If code has a space
            if(this.termOptionCode.indexOf(" ") !== -1){
                return false;
            }

            // If label is only spaces
            if(this.termOptionLabel.replace(/\s/g, '').length === 0) {
                return false
            }

            return true;
        }
        else if(this.termOptionCode && this.termOptionCode.indexOf(" ") !== -1){
            return false;
        }
            
        return false
    }

    addTermOption(): void {

        let termOption: Term = new Term(this.termOptionCode, this.termOptionLabel, this.termOptionDescription);


        this.hierarchyService.addAttributeTermTypeOption( this.attribute.rootTerm.code, termOption ).then( data => {
            
            this.attribute.rootTerm.children.push(data);

            this.attributeChange.emit(this.attribute);

            this.clearTermOption();

            this.enableTermOptionForm = false;

        } ).catch(( err: any ) => {
            this.error( err );
        } );

    }

    // updateTermOption(): void {

    //     let termOption: Term = new Term(this.termOptionCode, this.termOptionLabel, this.termOptionDescription);


        // this.hierarchyService.updateAttributeTermTypeOption( termOption ).then( data => {
            
        //     this.attribute.rootTerm.children.push(data);

        //     this.attributeChange.emit(this.attribute);

        //     this.termOptionCode = "";
        //     this.termOptionLabel = "";
        //     this.termOptionDescription = "";

        // } ).catch(( err: any ) => {
        //     this.error( err );
        // } );
    // }

    deleteTermOption(termOption: Term): void {

        this.hierarchyService.deleteAttributeTermTypeOption( termOption.code ).then( data => {
            
            if(this.attribute.rootTerm.children.indexOf(termOption) !== -1){
                this.attribute.rootTerm.children.splice(this.attribute.rootTerm.children.indexOf(termOption), 1);
            }

            this.attributeChange.emit(this.attribute);

            this.termOptionCode = "";
            this.termOptionLabel = "";
            this.termOptionDescription = "";

        } ).catch(( err: any ) => {
            this.error( err );
        } );

    }

    removeTermOption(termOption: Term): void {
        this.bsModalRef = this.modalService.show( ConfirmModalComponent, {
		  animated: true,
		  backdrop: true,
		  ignoreBackdropClick: true,
	  } );
	  this.bsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + '[' + termOption.localizedLabel + ']';
      this.bsModalRef.content.submitText = this.localizeService.decode("modal.button.delete");
      this.bsModalRef.content.type = ModalTypes.danger;

	  ( <ConfirmModalComponent>this.bsModalRef.content ).onConfirm.subscribe( data => {
          this.deleteTermOption( termOption );
	  } );
    }

    editTermOption(termOption: Term): void {
        this.geoObjectTypeManagementService.setModalState({"state":GeoObjectTypeModalStates.editTermOption, "attribute":this.attribute, "termOption":termOption})
    }

    clearTermOption(): void {
        this.termOptionCode = "";
        this.termOptionLabel = "";
        this.termOptionDescription = "";
    }

    cancelTermOption(): void {
        this.clearTermOption();
        this.enableTermOptionForm = false;
    }

    openAddTermOptionForm(): void {
        this.enableTermOptionForm = true;
    }

    close(): void {
        this.geoObjectTypeManagementService.setModalState({"state":GeoObjectTypeModalStates.editAttribute, "attribute":this.attribute, "termOption":""})
    }

    error( err: any ): void {
      if ( err !== null ) {
    	  // TODO: add error modal
          this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
          this.bsModalRef.content.message = ( err.localizedMessage || err.message || err.statusText );
      }
    }

}
