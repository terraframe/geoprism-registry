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

import { GeoObjectType, Attribute, AttributeTerm, Term} from '../hierarchy';
import { HierarchyService } from '../../../service/hierarchy.service';

import { GeoObjectAttributeCodeValidator } from '../../../factory/form-validation.factory';



@Component( {
    selector: 'term-option-input',
    templateUrl: './term-option-input.component.html',
    styleUrls: ['./term-option-input.css'],
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
export class TermOptionInputComponent implements OnInit {

    @Input() geoObjectType: GeoObjectType;
    @Input() attribute: AttributeTerm;
    @Output() attributeChange = new EventEmitter<AttributeTerm>();
    message: string = null;
    termOptionCode: string = "";
    termOptionLabel: string = "";
    termOptionDescription: string = "";
    state: string = 'none';

    constructor( private hierarchyService: HierarchyService, public bsModalRef: BsModalRef, private cdr: ChangeDetectorRef ) {
    }

    ngOnInit(): void {

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
    //   if(this.attribute instanceof AttributeTerm){

        let termOption: Term = new Term(this.termOptionCode, this.termOptionLabel, this.termOptionDescription);


        this.hierarchyService.addAttributeTermTypeOption( this.attribute.rootTerm.code, termOption ).then( data => {
            
            this.attribute.rootTerm.children.push(data);

            this.attributeChange.emit(this.attribute);

            this.termOptionCode = "";
            this.termOptionLabel = "";
            this.termOptionDescription = "";

        } ).catch(( err: any ) => {
            this.error( err );
        } );

    //   }
    }

    removeTermOption(termOption: Term): void {

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

    editTermOption(termOption: Term): void {
        
    }
    
    error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.localizedMessage || err.message );
            
            console.log(this.message);
        }
    }

}
