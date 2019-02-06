import { Component, Input, OnInit, EventEmitter, Output } from '@angular/core';
import { ImportConfiguration, LocationProblem } from '../io';

@Component( {

    selector: 'location-problem-page',
    templateUrl: './location-problem-page.component.html',
    styleUrls: []
} )
export class LocationProblemPageComponent implements OnInit {

    @Input() configuration: ImportConfiguration;
    @Output() stateChange = new EventEmitter<string>();
    problems: LocationProblem[] = [];
    message: string = null;

    constructor() { }

    ngOnInit(): void {
        if ( this.configuration.locationProblems != null ) {
            this.problems = this.configuration.locationProblems;
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

        for ( let i = 0; i < this.problems.length; i++ ) {
            const problem = this.problems[i];

            if ( problem.resolved && problem.action.name == 'IGNOREATLOCATION' ) {
                const value = ( problem.parent != null ? problem.parent + "-" + problem.label : problem.label );
                const exclusion = { code: '##PARENT##', value: value };

                this.configuration.exclusions.push( exclusion );
            }
        }


        this.stateChange.emit( 'NEXT' );
    }

    onCancel(): void {
        this.stateChange.emit( 'CANCEL' );
    }
}
