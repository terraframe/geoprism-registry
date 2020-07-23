import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';
import { Observable } from 'rxjs';
import { v4 as uuid } from 'uuid';

import { ImportConfiguration, TermProblem } from '../../../model/io';
import { IOService } from '../../../service/io.service';

@Component( {

    selector: 'term-problem',
    templateUrl: './term-problem.component.html',
    styleUrls: []
} )
export class TermProblemComponent implements OnInit {

    @Input() configuration: ImportConfiguration;
    @Input() problem: TermProblem;
    @Input() index: number;
    @Output() onError: EventEmitter<any> = new EventEmitter<any>();

    //    show: boolean;
    dataSource: Observable<any>;
    hasSynonym: boolean;

    termLabel: string;
    termId: string;

    constructor( private service: IOService ) {
        this.dataSource = Observable.create(( observer: any ) => {
            this.service.getTermSuggestions( this.problem.mdAttributeId, this.termLabel, '20' ).then( results => {
                observer.next( results );
            } );
        } );
    }

    ngOnInit(): void {
        this.termLabel = null;
        this.termId = null;
        this.hasSynonym = false;
    }

    typeaheadOnSelect( e: TypeaheadMatch ): void {
        this.termId = e.item.value;
        this.hasSynonym = ( this.termId != null );
    }

    createSynonym(): void {
        if ( this.hasSynonym ) {
            this.onError.emit( null );

            this.service.createTermSynonym( this.termId, this.problem.label ).then( response => {
                this.problem.resolved = true;
                this.problem.action = {
                    name: 'SYNONYM',
                    synonymId: response.synonymId,
                    label: response.label
                };
            } ).catch( e => {
                this.onError.emit( e.error );
            } );
        }
    }

    createOption(): void {
        this.onError.emit( null );
        
        this.service.createTerm( this.problem.label, uuid(), this.problem.parentCode).then( term => {
            this.problem.resolved = true;
            this.problem.action = {
                name: 'OPTION',
                term: term
            };
        } ).catch( e => {
            this.onError.emit( e.error );
        } );
    }

    ignoreValue(): void {
        this.problem.resolved = true;

        this.problem.action = {
            name: 'IGNORE'
        };
    }

    undoAction(): void {

        if ( this.problem.resolved ) {

            let action = this.problem.action;

            if ( action.name == 'IGNORE' ) {
                this.problem.resolved = false;
                this.problem.action = null;
            }
            else if ( action.name == 'SYNONYM' ) {
                this.onError.emit( null );

                this.service.deleteTermSynonym( action.synonymId ).then( response => {
                    this.problem.resolved = false;
                    this.problem.action = null;
                } ).catch( e => {
                    this.onError.emit( e.error );
                } );
            }
            else if ( action.name == 'OPTION' ) {
                this.onError.emit( null );

                this.service.removeTerm(this.problem.parentCode, action.term.code ).then( response => {
                    this.problem.resolved = false;
                    this.problem.action = null;
                } ).catch( e => {
                    this.onError.emit( e.error );
                } );
            }
        }
    }
}
