import { Component, OnInit, Input } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';
import { HttpErrorResponse } from '@angular/common/http';

import { GeoObjectType, MasterList, Conflict, ScheduledJob } from '../../../model/registry';

import Utils from '../../../utility/Utils'

import { RegistryService } from '../../../service/registry.service';

import { IOService } from '../../../service/io.service';
import { LocalizationService } from '../../../../shared/service/localization.service';

@Component( {
    selector: 'relationship-problem-widget',
    templateUrl: './relationship-problem-widget.component.html',
    styleUrls: []
} )
export class RelationshipProblemWidgetComponent implements OnInit {
    message: string = null;
    @Input() conflict: Conflict;
    @Input() job: ScheduledJob;

    /*
     * Observable subject for submission.  Called when an update is successful 
     */
    // onConflictAction: Subject<any>;

    readonly: boolean = false;
    edit: boolean = false;


    constructor( private service: RegistryService, private iService: IOService, private lService: LocalizationService, public bsModalRef: BsModalRef ) { }

    ngOnInit(): void {

        // this.onConflictAction = new Subject();

    }

    getFriendlyProblemType(type: string): string {
        return Utils.getFriendlyProblemType(type);
    }

    onSubmit(): void {

    }

    onCancel(): void {
        this.bsModalRef.hide()
    }

    error( err: HttpErrorResponse ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.error.localizedMessage || err.error.message || err.message );
        }
    }

}
