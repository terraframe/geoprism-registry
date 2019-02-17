import { Component, OnInit, Input, ElementRef } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { ErrorModalComponent } from '../../core/modals/error-modal.component';
import { AttributeInputComponent } from '../hierarchy/geoobjecttype-management/attribute-input.component';
import { ChangeRequestService } from '../../service/change-request.service';


import { HierarchyService } from '../../service/hierarchy.service';
import { RegistryService } from '../../service/registry.service';

import { IOService } from '../../service/io.service';
import { GeoObjectType, GeoObject, Attribute, AttributeTerm, AttributeDecimal, Term } from '../../model/registry';

import { Observable} from 'rxjs';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';
import { mergeMap } from 'rxjs/operators';

declare var acp: string;

@Component( { 
    selector: 'stand-alone-change-request',
    templateUrl: './stand-alone-change-request.component.html',
    styleUrls: []
} )
export class StandAloneChangeRequestComponent implements OnInit {

    /*
     * Reference to the modal current showing
     */
    private bsModalRef: BsModalRef;


    constructor(private service: IOService, private modalService: BsModalService,
        private registryService: RegistryService, private elRef: ElementRef, private changeRequestService: ChangeRequestService) {

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