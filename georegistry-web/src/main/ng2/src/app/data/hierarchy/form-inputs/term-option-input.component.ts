import { Component, OnInit, AfterViewInit, ElementRef, Input, Output, EventEmitter } from '@angular/core';
import {
  trigger,
  state,
  style,
  animate,
  transition
} from '@angular/animations'
import {NgControl, Validators, FormBuilder} from '@angular/forms';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { GeoObjectType, Attribute, AttributeTerm, TermOption} from '../hierarchy';
import { HierarchyService } from '../../../service/hierarchy.service';

import { GeoObjectAttributeCodeValidator } from '../../../factory/form-validation.factory';



@Component( {
    selector: 'term-option-input',
    templateUrl: './term-option-input.component.html',
    styleUrls: ['./term-option-input.css'],
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
export class TermOptionInputComponent implements OnInit {

    @Input() geoObjectType: GeoObjectType;
    @Input() attribute: AttributeTerm;
    @Output() attributeChange = new EventEmitter<AttributeTerm>();
    message: string = null;
    termOptionCode: string = "";
    termOptionLabel: string = "";
    termOptionDescription: string = "";

    constructor( private hierarchyService: HierarchyService, public bsModalRef: BsModalRef ) {
    }

    ngOnInit(): void {

    }

    ngAfterViewInit() {
    
    }

    ngOnDestroy(){
    
    }

    handleOnSubmit(): void {
        
    }

    addTermOption(): void {
      if(this.attribute instanceof AttributeTerm){
        let termOption:TermOption = new TermOption(this.termOptionCode, this.termOptionLabel, this.termOptionDescription);
        this.attribute.termOptions.push(termOption);

        this.attributeChange.emit(this.attribute);

        this.termOptionCode = "";
        this.termOptionLabel = "";
        this.termOptionDescription = "";
      }
    }
    
    error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.localizedMessage || err.message );
            
            console.log(this.message);
        }
    }

}
