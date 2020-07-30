import { Component, Input, OnInit, EventEmitter, Output } from '@angular/core';
import { ImportConfiguration, LocationProblem } from '@registry/model/io';
import { ErrorHandler } from '@shared/component/error-handler/error-handler';

@Component( {

    selector: 'location-problem-page',
    templateUrl: './location-problem-page.component.html',
    styleUrls: []
} )
export class LocationProblemPageComponent implements OnInit {

    @Input() configuration: ImportConfiguration;
    @Output() stateChange = new EventEmitter<string>();
    message: string = null;

    constructor() { }

    ngOnInit(): void {
    }

    hasProblems(): boolean {
        for ( let i = 0; i < this.configuration.locationProblems.length; i++ ) {

            if ( !this.configuration.locationProblems[i].resolved ) {
                return true;
            }
        }

        return false;
    }

    handleError( err: any ): void {
            this.message = ErrorHandler.getMessageFromError(err);
    }

    onNext(): void {
        if ( this.configuration.exclusions == null ) {
            this.configuration.exclusions = [];
        }

        for ( let i = 0; i < this.configuration.locationProblems.length; i++ ) {
            const problem = this.configuration.locationProblems[i];

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
