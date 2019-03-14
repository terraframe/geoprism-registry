import { Component, OnInit, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef, Input, Output, EventEmitter } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { DatePipe } from '@angular/common';

import { ErrorModalComponent } from '../../core/modals/error-modal.component';
import { AttributeInputComponent } from '../hierarchy/geoobjecttype-management/attribute-input.component';

import { HierarchyService } from '../../service/hierarchy.service';
import { RegistryService } from '../../service/registry.service';
import { ChangeRequestService } from '../../service/change-request.service';


import { IOService } from '../../service/io.service';
import { GeoObjectType, GeoObject, Attribute, AttributeTerm, AttributeDecimal, Term } from '../../model/registry';

import { GeoObjectAttributeExcludesPipe } from '../../data/change-request/geoobject-attribute-excludes.pipe';
import { ToEpochDateTimePipe } from '../../data/change-request/to-epoch-date-time.pipe';

import { Observable } from 'rxjs';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';
import { mergeMap } from 'rxjs/operators';

declare var acp: string;


@Component({
    selector: 'geoobject-shared-attribute-editor',
    templateUrl: './geoobject-shared-attribute-editor.component.html',
    styleUrls: ['./geoobject-shared-attribute-editor.css'],
    providers: [DatePipe]
})

/**
 * This component is shared between:
 * - crtable (create-update-geo-object action detail)
 * - change-request (for submitting change requests)
 * - master list geoobject editing widget
 * 
 * Be wary of changing this component for one usecase and breaking other usecases!
 */
export class GeoObjectSharedAttributeEditorComponent implements OnInit {

    objectKeys = Object.keys;

    /*
     * Reference to the modal current showing
     */
    private bsModalRef: BsModalRef;
    
    /*
	 * The current state of the GeoObject in the GeoRegistry
	 */
    @Input() preGeoObject: GeoObject = null;
	
	/*
	 * The state of the GeoObject after our Change Request has been approved 
	 */
    @Input() postGeoObject: any = {};

    @Input() geoObjectType: GeoObjectType;
    
    @Input() isValid: boolean = true;
    
    @Output() valid = new EventEmitter<boolean>();
    
    modifiedTermOption: Term = null;
    currentTermOption: Term = null;
    geoObjectAttributeExcludes: string[] = ["uid", "sequence", "type", "lastUpdateDate", "createDate", "status"];
    
    @ViewChild("changeRequestForm") changeRequestForm;

    constructor(private service: IOService, private modalService: BsModalService, private changeDetectorRef: ChangeDetectorRef,
            private registryService: RegistryService, private elRef: ElementRef, private changeRequestService: ChangeRequestService,
            private date: DatePipe, private toEpochDateTimePipe: ToEpochDateTimePipe) {
    	
    }
    
    ngOnInit(): void {
		if (this.postGeoObject == null)
	    {
	      this.postGeoObject = JSON.parse(JSON.stringify(this.preGeoObject)); // Object.assign is a shallow copy. We want a deep copy.
	    }
		
		this.changeRequestForm.statusChanges.subscribe(result => {
			this.isValid = (result === "VALID");
			this.valid.emit(this.isValid);
			
			console.log("Pre and Post GO", this.preGeoObject, this.postGeoObject);
          }
		);
    }

    onSelectPropertyOption(event: any, option:any): void {
        this.currentTermOption = JSON.parse(JSON.stringify(this.modifiedTermOption));
    }

    getGeoObjectTypeTermAttributeOptions(termAttributeCode: string) {
        for (let i=0; i<this.geoObjectType.attributes.length; i++) {
            let attr: any = this.geoObjectType.attributes[i];

            if(attr.type === "term" && attr.code === termAttributeCode){

                attr = <AttributeTerm> attr;
                let attrOpts = attr.rootTerm.children;

                if(attrOpts.length > 0){
                    return attrOpts;
                }
            }
        }

        return null;
    }

    getTypeDefinition(key: string): string {
        // let attrs = this.geoObjectType.attributes;


        // attrs.attributes.forEach(attr => {
        for(let i=0; i<this.geoObjectType.attributes.length; i++){
            let attr = this.geoObjectType.attributes[i];

         if (attr.code === key) {
                return attr.type;
            }
        }

        return null;
    }

    isFormValid(): boolean {
        return this.isValid;
    }

    public getGeoObject(): any {
    	return this.postGeoObject;
    }
    
    public error(err: any): void {
        // Handle error
        if (err !== null) {
            this.bsModalRef = this.modalService.show(ErrorModalComponent, { backdrop: true });
            this.bsModalRef.content.message = (err.localizedMessage || err.message);
        }
    }
}
