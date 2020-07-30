import { Component, Input } from '@angular/core';
import { LocalizationService } from '@shared/service/localization.service';

import { Step, StepConfig } from '@registry/model/registry';


@Component( { 
    selector: 'step-indicator',
    templateUrl: './step-indicator.component.html',
    styleUrls: ['./step-indicator.css']
} )
export class StepIndicatorComponent {

    @Input('steps')
    set steps(value: StepConfig) {
        this._stepConfig = value;
    }

    _stepConfig: StepConfig;
    step: Step;


    constructor( private localizeService: LocalizationService ) {

        this._stepConfig = {"steps": []};
    }


    ngOnInit(): void {
    }

    ngOnDestroy() {
       
    }
}
