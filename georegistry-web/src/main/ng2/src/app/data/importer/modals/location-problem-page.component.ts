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

    constructor() { }

    ngOnInit(): void {
        if ( this.configuration.locationProblems != null ) {
            this.problems = this.configuration.locationProblems;
        }
    }

    hasProblems(): boolean {
        if ( this.problems != null ) {
            for ( let i = 0; i < this.problems.length; i++ ) {

                if ( !this.problems[i].resolved ) {
                    return true;
                }
            }
        }

        return false;
    }

    onNext(): void {
        this.stateChange.emit( 'NEXT' );
    }

    onCancel(): void {
        this.stateChange.emit( 'CANCEL' );
    }
}
