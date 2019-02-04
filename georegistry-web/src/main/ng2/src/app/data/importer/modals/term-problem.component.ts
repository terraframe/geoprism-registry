import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';

import { TermProblem } from '../io';

import { TermService } from '../../../service/term.service';

@Component( {

    selector: 'term-problem',
    templateUrl: './term-problem.component.html',
    styleUrls: []
} )
export class TermProblemComponent implements OnInit {

    @Input() problem: TermProblem;

    @Output() onProblemChange = new EventEmitter();

    show: boolean;
    hasSynonym: boolean;
    synonym: string;

    constructor( private service: TermService ) { }

    ngOnInit(): void {
        this.problem.action = null;
        this.show = false;
        this.hasSynonym = false;
    }
//
//    source = ( text: string ) => {
//        let limit = '20';
//
//        return this.service.getClassifierSuggestions( this.problem.mdAttributeId, text, limit );
//    }
//
//    setSynonym() {
//        this.hasSynonym = ( this.synonym != null && this.synonym.length > 0 );
//    }
//
//    createSynonym(): void {
//        if ( this.hasSynonym ) {
//            this.service.createClassifierSynonym( this.synonym, this.problem.label )
//                .then( response => {
//                    this.problem.resolved = true;
//                    this.problem.action = {
//                        name: 'SYNONYM',
//                        synonymId: response.synonymId,
//                        label: response.label
//                    };
//
//                    this.onProblemChange.emit( this.problem );
//                } );
//        }
//    }
//
//    createOption(): void {
//        this.service.create( this.problem.label, this.problem.categoryId, false )
//            .then( response => {
//                this.problem.resolved = true;
//                this.problem.action = {
//                    name: 'OPTION',
//                    optionId: response.oid
//                };
//
//                this.onProblemChange.emit( this.problem );
//            } );
//    }
//
//    ignoreValue(): void {
//        this.problem.resolved = true;
//
//        this.problem.action = {
//            name: 'IGNORE'
//        };
//    }
//
//    undoAction(): void {
//
//        if ( this.problem.resolved ) {
//
//            let action = this.problem.action;
//
//            if ( action.name == 'IGNORE' ) {
//                this.problem.resolved = false;
//                this.problem.action = null;
//
//                this.onProblemChange.emit( this.problem );
//            }
//            else if ( action.name == 'SYNONYM' ) {
//                this.uploadService.deleteClassifierSynonym( action.synonymId )
//                    .then( response => {
//                        this.problem.resolved = false;
//                        this.problem.action = null;
//
//                        this.onProblemChange.emit( this.problem );
//                    } );
//            }
//            else if ( action.name == 'OPTION' ) {
//                this.service.remove( action.optionId )
//                    .then( response => {
//                        this.problem.resolved = false;
//                        this.problem.action = null;
//
//                        this.onProblemChange.emit( this.problem );
//                    } );
//            }
//        }
//    }
}
