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

import { GeoObjectAttributeExcludesPipe } from '../../data/geoobject-shared-attribute-editor/geoobject-attribute-excludes.pipe';
import { ToEpochDateTimePipe } from '../../data/submit-change-request/to-epoch-date-time.pipe';

import { Observable } from 'rxjs';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';
import { mergeMap } from 'rxjs/operators';

declare var acp: string;


@Component({
    selector: 'geoobject-editor',
    templateUrl: './geoobject-editor.component.html',
    styleUrls: ['./geoobject-editor.css'],
    providers: [DatePipe]
})

/**
 * This component is used in the master list when editing a row.
 */
export class GeoObjectEditorComponent implements OnInit {

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
    
    @ViewChild("attributeForm") attributeForm;

    constructor(private service: IOService, private modalService: BsModalService, private changeDetectorRef: ChangeDetectorRef,
            private registryService: RegistryService, private elRef: ElementRef, private changeRequestService: ChangeRequestService,
            private date: DatePipe, private toEpochDateTimePipe: ToEpochDateTimePipe) {
    	
    }
    
    ngOnInit(): void {
    }
    
    public error(err: any): void {
        // Handle error
        if (err !== null) {
            this.bsModalRef = this.modalService.show(ErrorModalComponent, { backdrop: true });
            this.bsModalRef.content.message = (err.localizedMessage || err.message);
        }
    }
}
