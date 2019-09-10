import { Component, Input, OnInit, EventEmitter, Output } from '@angular/core';
import { ImportConfiguration, TermProblem } from '../../../model/io';

@Component( {

    selector: 'term-problem-page',
    templateUrl: './term-problem-page.component.html',
    styleUrls: []
} )
export class TermProblemPageComponent implements OnInit {

    @Input() configuration: ImportConfiguration;
    @Output() stateChange = new EventEmitter<string>();
    message: string = null;

    constructor() { }

    ngOnInit(): void {
    }

    hasProblems(): boolean {
        for ( let i = 0; i < this.configuration.termProblems.length; i++ ) {

            if ( !this.configuration.termProblems[i].resolved ) {
                return true;
            }
        }

        return false;
    }

    handleError( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.localizedMessage || err.message );
        }
    }

    onNext(): void {
        if ( this.configuration.exclusions == null ) {
            this.configuration.exclusions = [];
        }

        for ( let i = 0; i < this.configuration.termProblems.length; i++ ) {
            const problem = this.configuration.termProblems[i];

            if ( problem.resolved && problem.action.name == 'IGNORE' ) {
                const exclusion = { code: problem.attributeCode, value: problem.label };

                this.configuration.exclusions.push( exclusion );
            }
        }

        this.stateChange.emit( 'NEXT' );
    }

    onCancel(): void {
        this.stateChange.emit( 'CANCEL' );
    }
}
