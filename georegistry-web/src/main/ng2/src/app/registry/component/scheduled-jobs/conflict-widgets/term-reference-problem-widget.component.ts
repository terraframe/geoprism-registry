import { Component, OnInit, Input, EventEmitter, Output } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { BsModalService } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';

import { GeoObjectType, MasterList, ScheduledJob } from '../../../model/registry';

import { GeoObjectEditorComponent } from '../../geoobject-editor/geoobject-editor.component';

import Utils from '../../../utility/Utils'

import { RegistryService } from '../../../service/registry.service';
import { IOService } from '../../../service/io.service';
import { LocalizationService } from '../../../../shared/service/localization.service';

@Component( {
    selector: 'term-reference-problem-widget',
    templateUrl: './term-reference-problem-widget.component.html',
    styleUrls: []
} )
export class TermReferenceProblemWidgetComponent implements OnInit {
    message: string = null;
    @Input() problem: any;
    @Input() job: ScheduledJob;
    @Output() public onProblemResolved = new EventEmitter<any>();
    
    termId: string = null;
    searchLabel: string;

    readonly: boolean = false;
    edit: boolean = false;


    constructor( private service: RegistryService, private iService: IOService, 
        private lService: LocalizationService, public bsModalRef: BsModalRef, private modalService: BsModalService
        ) { }

    ngOnInit(): void {

        this.problem.parent = null;
        this.searchLabel = "";

    }

    getValidationProblemDisplayLabel(conflict: any): string {
      return conflict.type;
    }
    
    getTypeAheadObservable( conflict: any ): Observable<any> {
        return Observable.create(( observer: any ) => {
            this.iService.getTermSuggestions( conflict.mdAttributeId, this.searchLabel, '20' ).then( results => {
                observer.next( results );
            } );
        } );
    }
    
    typeaheadOnSelect( e: TypeaheadMatch ): void {
        this.termId = e.item.value;
    }
    
    onIgnore(): void {
      let cfg = {
        resolution: "IGNORE",
        validationProblemId: this.problem.id
      };
    
      this.service.submitValidationResolve( cfg ).then( response => {
        
        this.onProblemResolved.emit(this.problem);
        
        this.bsModalRef.hide()
        
      } ).catch(( err: HttpErrorResponse ) => {
        this.error(err);
      } );
    }
    
    onCreateSynonym(): void {
      let cfg = {
        validationProblemId: this.problem.id,
        resolution: "SYNONYM",
        classifierId: this.termId,
        label: this.problem.label
      };
    
      this.service.submitValidationResolve( cfg ).then( response => {
        
        this.onProblemResolved.emit(this.problem);
        
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
