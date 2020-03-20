import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
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
    @Output() public onProblemResolved = new EventEmitter<any>();

    readonly: boolean = false;
    edit: boolean = false;


    constructor( private service: RegistryService, private iService: IOService, 
        private lService: LocalizationService, public bsModalRef: BsModalRef, private modalService: BsModalService
        ) { }

    ngOnInit(): void {

    }

    onEditGeoObject(): void {
        let editModal = this.modalService.show( GeoObjectEditorComponent, {
            backdrop: true,
            ignoreBackdropClick: true
        } );

        editModal.content.configureFromImportError(this.problem, this.job.historyId, this.job.configuration.startDate, true);
        editModal.content.setMasterListId( null );
        editModal.content.setOnSuccessCallback(() => {

          this.onProblemResolved.emit(this.problem);
          this.bsModalRef.hide()
            
        } );
    }

    getFriendlyProblemType(type: string): string {
        return Utils.getFriendlyProblemType(type);
    }

    onSubmit(): void {

    }

    onCancel(): void {
      this.bsModalRef.hide();
    }

    error( err: HttpErrorResponse ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.error.localizedMessage || err.error.message || err.message );
        }
    }

}
