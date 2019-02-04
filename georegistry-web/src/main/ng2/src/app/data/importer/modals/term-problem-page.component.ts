import { Component, Input, OnInit, EventEmitter, Output } from '@angular/core';
import { ImportConfiguration, TermProblem } from '../io';

@Component( {

    selector: 'term-problem-page',
    templateUrl: './term-problem-page.component.html',
    styleUrls: []
} )
export class TermProblemPageComponent implements OnInit {

    @Input() configuration: ImportConfiguration;
    @Output() stateChange = new EventEmitter<string>();
    problems: TermProblem[] = [];

    constructor() { }

    ngOnInit(): void {
        if ( this.configuration.termProblems != null ) {
            this.problems = this.configuration.termProblems;
        }
    }

    hasProblems(): boolean {
        for ( let i = 0; i < this.problems.length; i++ ) {

            if ( !this.problems[i].resolved ) {
                return true;
            }
        }

        return false;
    }

    onNext(): void {
        if ( this.configuration.exclusions == null ) {
            this.configuration.exclusions = [];
        }

        for ( let i = 0; i < this.problems.length; i++ ) {
            const problem = this.problems[i];

            if ( problem.resolved && problem.action.name == 'IGNORE' ) {
                const exclusion = { code: problem.code, value: problem.label };

                this.configuration.exclusions.push(exclusion);
            }
        }

        this.stateChange.emit( 'NEXT' );
    }

    onCancel(): void {
        this.stateChange.emit( 'CANCEL' );
    }
}
