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
    selector: 'attribute-input',
    templateUrl: './attribute-input.component.html',
    styleUrls: ['./attribute-input.css'],
    animations: [
        trigger('toggleInputs', [
            state('none, void', 
                style({ 'opacity': 0})
              ),
              state('show', 
                style({ 'opacity': 1})
              ),
              transition('none => show', animate('300ms'))
            //   transition('show => none', animate('100ms'))
        ])
    ]
} )
export class AttributeInputComponent implements OnInit {

    @Input() disableCodeField: boolean = false;
    @Input() excludeDescription: boolean = false;
    @Input() geoObjectType: GeoObjectType;
    @Input() attribute: Attribute;
    @Output() attributeChange = new EventEmitter<Attribute>();
    message: string = null;

    state: string = 'none';

    constructor( private hierarchyService: HierarchyService, public bsModalRef: BsModalRef, private cdr: ChangeDetectorRef ) {
    }

    ngOnInit(): void {

    }

    ngAfterViewInit() {
        this.state = 'show';
        this.cdr.detectChanges();
    }

    ngOnChanges() {
 
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
        if(this.attribute.code && this.attribute.localizedLabel) {

            // if code has a space
            if(this.attribute.code.indexOf(" ") !== -1){
                return false;
            }

            // If label is only spaces
            if(this.attribute.localizedLabel.replace(/\s/g, '').length === 0) {
                return false
            }

            return true;
        }
        
        return false;
    }

    error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.localizedMessage || err.message );
            
            console.log(this.message);
        }
    }

}
