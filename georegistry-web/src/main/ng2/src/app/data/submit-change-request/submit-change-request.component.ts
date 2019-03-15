import { Component, OnInit, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef, Input } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { DatePipe } from '@angular/common';

import { ErrorModalComponent } from '../../core/modals/error-modal.component';
import { AttributeInputComponent } from '../hierarchy/geoobjecttype-management/attribute-input.component';
import { ChangeRequestService } from '../../service/change-request.service';


import { HierarchyService } from '../../service/hierarchy.service';
import { RegistryService } from '../../service/registry.service';

import { IOService } from '../../service/io.service';
import { GeoObjectType, GeoObject, Attribute, AttributeTerm, AttributeDecimal, Term } from '../../model/registry';

import { ToEpochDateTimePipe } from '../../data/submit-change-request/to-epoch-date-time.pipe';

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
    private bsModalRef: BsModalRef;

    private geoObjectType: GeoObjectType;

    private geoObjectTypes: GeoObjectType[] = [];

    private typeaheadLoading: boolean;

    private typeaheadNoResults: boolean;

	private geoObjectId: string = "";

    private reason: string = "";

	private dataSource: Observable<any>;
	
	/*
	 * The current state of the GeoObject in the GeoRegistry
	 */
	private preGeoObject: GeoObject = null;
	
	/*
	 * The state of the GeoObject after our Change Request has been approved 
	 */
	private postGeoObject: GeoObject = null;
	
	isValid: boolean = false;
	
	private geoObjectAttributeExcludes: string[] = ["uid", "sequence", "type", "lastUpdateDate", "createDate", "status"];

    constructor(private service: IOService, private modalService: BsModalService, private changeDetectorRef: ChangeDetectorRef,
        private registryService: RegistryService, private elRef: ElementRef, private changeRequestService: ChangeRequestService,
        private date: DatePipe, private toEpochDateTimePipe: ToEpochDateTimePipe) {

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
	
	    }).catch((err: Response) => {
	        this.error(err.json());
	    });
    }
    
    
    
    private onValidChange(newValid: boolean)
    {
      if (this.preGeoObject == null) {
    	  this.isValid = false;
    	  return;
      }
      this.isValid = newValid;
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

            }).catch((err: Response) => {
                this.error(err.json());
            });
    }
    
    submit(): void {
    	
        // Convert all dates from input element (date type) format to epoch time
        for(let i=0; i<this.geoObjectType.attributes.length; i++){
            let attr = this.geoObjectType.attributes[i];

            if(attr.type === "date" && this.geoObjectAttributeExcludes.indexOf(attr.code) === -1){
                let propInGeoObj = this.postGeoObject.properties[attr.code];

                if(propInGeoObj && propInGeoObj.length > 0){
                    let formattedStr = new Date(propInGeoObj).getTime();

                    // let formattedStr = formatted.getFullYear() + "-" + formatted.getMonth() + "-" + formatted.getDay() + " AD " + formatted.getHours() + "-" + formatted.getMinutes() + "-" + formatted.getSeconds() + "-" + formatted.getMilliseconds() + " -" + formatted.getTimezoneOffset()

                    // Required format: yyyy-MM-dd G HH-mm-ss-SS Z 
                    // Example = "2019-02-17 AD 15-16-29-00 -0700"
                    this.postGeoObject.properties[attr.code] = formattedStr;
                }
            }
        }

        let toDelete = [];
        for (var key in this.postGeoObject.properties) {
            if (this.postGeoObject.properties.hasOwnProperty(key)) {
                if(!this.postGeoObject.properties[key] || this.postGeoObject.properties[key].length < 1){
                    toDelete.push(key);
                }
            }
        }

        toDelete.forEach(key => {
            delete this.postGeoObject.properties[key];
        })


        let submitObj = [{  
            "actionType":"geoobject/update", // TODO: account for create
            "apiVersion":"1.0-SNAPSHOT", // TODO: make dynamic
            "createActionDate":new Date().getTime(), 
            "geoObject":this.postGeoObject,
            "contributorNotes":this.reason
        }]

        this.changeRequestService.submitChangeRequest(JSON.stringify(submitObj))
	    .then( geoObject => {
            this.cancel();
	    }).catch(( err: Response ) => {
	      this.error( err.json() );
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