import { Component, OnInit, Input } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { BsModalService } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs/Subject';
import { HttpErrorResponse } from '@angular/common/http';

import { GeoObjectType, MasterList, ScheduledJob, ImportError } from '../../../model/registry';

import { GeoObjectEditorComponent } from '../../geoobject-editor/geoobject-editor.component';

import Utils from '../../../utility/Utils'

import { RegistryService } from '../../../service/registry.service';
import { IOService } from '../../../service/io.service';
import { LocalizationService } from '../../../../shared/service/localization.service';

@Component( {
    selector: 'import-problem-widget',
    templateUrl: './import-problem-widget.component.html',
    styleUrls: []
} )
export class ImportProblemWidgetComponent implements OnInit {
    message: string = null;
    @Input() problem: ImportError;
    @Input() job: ScheduledJob;

    /*
     * Observable subject for submission.  Called when an update is successful 
     */
    // onConflictAction: Subject<any>;

    readonly: boolean = false;
    edit: boolean = false;


    constructor( private service: RegistryService, private iService: IOService, 
        private lService: LocalizationService, public bsModalRef: BsModalRef, private modalService: BsModalService
        ) { }

    ngOnInit(): void {

        // this.onConflictAction = new Subject();

    }

    onConflictResolution(conflict: any, job: ScheduledJob): void {
        let editModal = this.modalService.show( GeoObjectEditorComponent, {
            backdrop: true,
            ignoreBackdropClick: true
        } );

        // TODO: change last param from fixed true to equivilent of this.list.isGeometryEditable
        editModal.content.configureFromImportError(conflict.object, conflict.object.geoObject.attributes.type, job.createDate, true );
        editModal.content.setMasterListId( null );
        editModal.content.setOnSuccessCallback(() => {

            console.log("success")
            
        } );
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
