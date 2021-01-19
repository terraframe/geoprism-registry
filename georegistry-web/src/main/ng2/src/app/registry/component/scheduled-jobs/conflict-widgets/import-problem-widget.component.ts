import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { BsModalService } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { GeoObjectType, MasterList, ScheduledJob, ImportError } from '@registry/model/registry';

import { GeoObjectEditorComponent } from '../../geoobject-editor/geoobject-editor.component';
import Utils from '../../../utility/Utils'

import { RegistryService, IOService } from '@registry/service';
import { ErrorHandler } from '@shared/component';
import { LocalizationService } from '@shared/service';

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

    getFriendlyProblemType(probType: string): string {

        if(probType === "net.geoprism.registry.io.ParentCodeException"){
            return this.lService.decode( "scheduledjobs.job.problem.type.parent.lookup" );
        }

        if(probType === "net.geoprism.registry.io.PostalCodeLocationException"){
            return this.lService.decode( "scheduledjobs.job.problem.type.postal.code.lookup" );
        }

        if(probType === "net.geoprism.registry.io.AmbiguousParentException"){
          return this.lService.decode( "scheduledjobs.job.problem.type.multi.parent.lookup" );
        }

        if(probType === "net.geoprism.registry.io.InvalidGeometryException"){
          return this.lService.decode( "scheduledjobs.job.problem.type.invalid.geom.lookup" );
        }

        if(probType === "net.geoprism.registry.DataNotFoundException"){
          return this.lService.decode( "scheduledjobs.job.problem.type.datanotfound" );
        }
        
        if(
            probType === "net.geoprism.registry.roles.CreateGeoObjectPermissionException"
            || probType === "net.geoprism.registry.roles.WriteGeoObjectPermissionException"
            || probType === "net.geoprism.registry.roles.DeleteGeoObjectPermissionException"
            || probType === "net.geoprism.registry.roles.ReadGeoObjectPermissionException"
          ){
          return this.lService.decode( "scheduledjobs.job.problem.type.permission" );
        }

        // if(probType === "net.geoprism.registry.io.TermValueException"){
        //   return this.localizeService.decode( "scheduledjobs.job.problem.type.postal.code.lookup" );
        // }
        if(
          probType === "com.runwaysdk.dataaccess.DuplicateDataException"
          || probType === "net.geoprism.registry.DuplicateGeoObjectException"
          || probType === "net.geoprism.registry.DuplicateGeoObjectCodeException"
          ){
          return this.lService.decode( "scheduledjobs.job.problem.type.duplicate.data.lookup" );
        }

        return probType;
    }

    onSubmit(): void {

    }

    onCancel(): void {
      this.bsModalRef.hide();
    }

	formatDate(date: string): string {
		return this.lService.formatDateForDisplay(date);
	}

    error( err: HttpErrorResponse ): void {
            this.message = ErrorHandler.getMessageFromError(err);
    }

}
