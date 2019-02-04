import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';
import { Observable } from 'rxjs';

import { ImportConfiguration, TermProblem } from '../io';
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
            this.service.createTermSynonym( this.termId, this.problem.label ).then( response => {
                this.problem.resolved = true;
                this.problem.action = {
                    name: 'SYNONYM',
                    synonymId: response.synonymId,
                    label: response.label
                };
            } ).catch( e => {
                this.onError.emit( e );
            } );
        }
    }

    createOption(): void {
        this.service.createTerm( this.problem.label, this.problem.categoryId, false ).then( term => {
            this.problem.resolved = true;
            this.problem.action = {
                name: 'OPTION',
                optionId: term.oid
            };
        } ).catch( e => {
            this.onError.emit( e );
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
                this.service.deleteTermSynonym( action.synonymId ).then( response => {
                    this.problem.resolved = false;
                    this.problem.action = null;
                } ).catch( e => {
                    this.onError.emit( e );
                } );
            }
            else if ( action.name == 'OPTION' ) {
                this.service.removeTerm( action.optionId ).then( response => {
                    this.problem.resolved = false;
                    this.problem.action = null;
                } ).catch( e => {
                    this.onError.emit( e );
                } );
            }
        }
    }
}
