import { Component, OnInit, Input, EventEmitter, Output } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { BsModalService } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs/Subject';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';

import { GeoObjectType, MasterList, Conflict, ScheduledJob } from '../../../model/registry';

import { GeoObjectEditorComponent } from '../../geoobject-editor/geoobject-editor.component';

import Utils from '../../../utility/Utils'

import { RegistryService } from '../../../service/registry.service';
import { IOService } from '../../../service/io.service';
import { LocalizationService } from '../../../../shared/service/localization.service';

@Component( {
    selector: 'row-validation-problem-widget',
    templateUrl: './row-validation-problem-widget.component.html',
    styleUrls: []
} )
export class RowValidationProblemWidgetComponent implements OnInit {
    message: string = null;
    @Input() conflict: any;
    @Input() job: ScheduledJob;
    @Output() public onProblemResolved = new EventEmitter<any>();
    
    searchLabel: string;

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
        
        // this.searchLabel = this.conflict.label;
        
        this.conflict.parent = null;
        this.searchLabel = "";

    }
    
    getString(conflict: any): string {
      return JSON.stringify(conflict);
    }

    getValidationProblemDisplayLabel(conflict: any): string {
      return conflict.type;
    }
    
    getTypeAheadObservable( typeCode: string, conflict: any ): Observable<any> {

        let parentCode = null;
        let hierarchyCode = this.job.configuration.hierarchy;

        return Observable.create(( observer: any ) => {
            this.service.getGeoObjectSuggestions( this.searchLabel, typeCode, parentCode, hierarchyCode, this.job.startDate ).then( results => {
                observer.next( results );
            } );
        } );
    }

    typeaheadOnSelect( e: TypeaheadMatch, conflict: any ): void {

        this.service.getParentGeoObjects( e.item.uid, conflict.typeCode, [], false, this.job.startDate ).then( ancestors => {

            conflict.parent = ancestors.geoObject;
            this.searchLabel = ancestors.geoObject.properties.displayLabel.localizedValue;

        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );
    }
    
    onIgnore(): void {
      let cfg = {
        resolution: "IGNORE",
        validationProblemId: this.conflict.id
      };
    
      this.service.submitValidationResolve( cfg ).then( response => {
        
        this.onProblemResolved.emit(this.conflict);
        
        this.bsModalRef.hide()
        
      } ).catch(( err: HttpErrorResponse ) => {
        this.error(err);
      } );
    }
    
    onCreateSynonym(): void {
      let cfg = {
        validationProblemId: this.conflict.id,
        resolution: "SYNONYM",
        code: this.conflict.parent.properties.code,
        typeCode: this.conflict.parent.properties.type,
        label: this.conflict.label
      };
    
      this.service.submitValidationResolve( cfg ).then( response => {
        
        this.onProblemResolved.emit(this.conflict);
        
        this.bsModalRef.hide()
        
      } ).catch(( err: HttpErrorResponse ) => {
        this.error(err);
      } );
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
