import { Component, OnInit, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef, Input } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { HttpErrorResponse } from '@angular/common/http';

import { SuccessModalComponent } from '../../../shared/component/modals/success-modal.component';

import { DatePipe } from '@angular/common';

import { ErrorModalComponent } from '../../../shared/component/modals/error-modal.component';
import { AttributeInputComponent } from '../hierarchy/geoobjecttype-management/attribute-input.component';
import { ChangeRequestService } from '../../service/change-request.service';


import { HierarchyService } from '../../service/hierarchy.service';
import { RegistryService } from '../../service/registry.service';
import { LocalizationService } from '../../../shared/service/localization.service';

import { IOService } from '../../service/io.service';
import { GeoObjectType, GeoObject, Attribute, AttributeTerm, AttributeDecimal, Term } from '../../model/registry';

import { ToEpochDateTimePipe } from '../../pipe/to-epoch-date-time.pipe';

import { Observable} from 'rxjs';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';
import { mergeMap } from 'rxjs/operators';

declare var acp: string;

@Component( { 
    selector: 'submit-change-request',
    templateUrl: './submit-change-request.component.html',
    styleUrls: []
} )
export class SubmitChangeRequestComponent implements OnInit {

    /*
     * Reference to the modal current showing
     */
    bsModalRef: BsModalRef;

    geoObjectType: GeoObjectType;

    geoObjectTypes: GeoObjectType[] = [];

    typeaheadLoading: boolean;

    typeaheadNoResults: boolean;

	geoObjectId: string = "";

    reason: string = "";

	dataSource: Observable<any>;
	
	@ViewChild("attributeEditor") attributeEditor;
	
	@ViewChild("geometryEditor") geometryEditor;
	
	/*
	 * The current state of the GeoObject in the GeoRegistry
	 */
	preGeoObject: GeoObject = null;
	
	/*
	 * The state of the GeoObject after our Change Request has been approved 
	 */
	postGeoObject: GeoObject = null;
	
	isValid: boolean = false;
	
	geoObjectAttributeExcludes: string[] = ["uid", "sequence", "type", "lastUpdateDate", "createDate", "status"];

    constructor(private service: IOService, private modalService: BsModalService, private changeDetectorRef: ChangeDetectorRef,
        private registryService: RegistryService, private elRef: ElementRef, private changeRequestService: ChangeRequestService,
        private date: DatePipe, private toEpochDateTimePipe: ToEpochDateTimePipe, private localizeService: LocalizationService) {

        this.dataSource = Observable.create((observer: any) => {
            this.registryService.getGeoObjectSuggestionsTypeAhead(this.geoObjectId, this.geoObjectType.code).then(results => {
                observer.next(results);
            });
        });
    }

    ngOnInit(): void {
		this.registryService.getGeoObjectTypes([])
	    .then(types => {
	        this.geoObjectTypes = types;
	
	        this.geoObjectTypes.sort((a, b) => {
	            if (a.label.localizedValue.toLowerCase() < b.label.localizedValue.toLowerCase()) return -1;
	            else if (a.label.localizedValue.toLowerCase() > b.label.localizedValue.toLowerCase()) return 1;
	            else return 0;
	        });
	
	        let pos = this.getGeoObjectTypePosition("ROOT");
	        if (pos) {
	            this.geoObjectTypes.splice(pos, 1);
	        }
	
	        // this.currentGeoObjectType = this.geoObjectTypes[1];
	
	    }).catch((err: HttpErrorResponse) => {
	        this.error(err);
        });
        
    }
    
    
    
    private onValidChange(newValid: boolean)
    {
      if (this.preGeoObject == null) {
    	  this.isValid = false;
    	  return;
      }
      
      if (this.geometryEditor != null && !this.geometryEditor.getIsValid())
      {
        this.isValid = false;
        return;
      }
      
      if (this.attributeEditor != null && !this.attributeEditor.getIsValid())
      {
        this.isValid = false;
        return;
      }
      
      this.isValid = true;
    }
    
    private getGeoObjectTypePosition(code: string): number {
        for (let i = 0; i < this.geoObjectTypes.length; i++) {
            let obj = this.geoObjectTypes[i];
            if (obj.code === code) {
                return i;
            }
        }

        return null;
    }
    
    changeTypeaheadLoading(e: boolean): void {
        this.typeaheadLoading = e;
    }

    typeaheadOnSelect(e: TypeaheadMatch): void {
        this.registryService.getGeoObjectByCode(e.item.code, this.geoObjectType.code)
            .then(geoObject => {
                this.preGeoObject = geoObject;
                this.postGeoObject = JSON.parse(JSON.stringify(this.preGeoObject)); // Object.assign is a shallow copy. We want a deep copy.

            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
    }
    
    submit(): void {
    
        let goSubmit: GeoObject = this.attributeEditor.getGeoObject();
        
        if (this.geometryEditor != null)
        {
          let goGeometries: GeoObject = this.geometryEditor.saveDraw();
          goSubmit.geometry = goGeometries.geometry;
        }

        let actions = [{  
            "actionType":"geoobject/update", // TODO: account for create
            "apiVersion":"1.0-SNAPSHOT", // TODO: make dynamic
            "createActionDate":new Date().getTime(), 
            "geoObject": goSubmit,
            "contributorNotes":this.reason
        }]

        this.changeRequestService.submitChangeRequest(JSON.stringify(actions))
	    .then( geoObject => {
			this.cancel();
			
			this.bsModalRef = this.modalService.show( SuccessModalComponent, { backdrop: true } );
			this.bsModalRef.content.message = this.localizeService.decode("change.request.success.message");

	    }).catch(( err: HttpErrorResponse ) => {
	      this.error( err );
	    });
        
        this.isValid = false;
    }

    cancel(): void {
    	this.isValid = false;
      this.preGeoObject = null;
      this.postGeoObject = null;
      this.geoObjectId = null;
      this.geoObjectType = null;
      this.reason = null;
    }

    public error(err: any): void {
        // Handle error
        if (err !== null) {
            this.bsModalRef = this.modalService.show(ErrorModalComponent, { backdrop: true });
            this.bsModalRef.content.message = (err.localizedMessage || err.message);
        }
    }
}