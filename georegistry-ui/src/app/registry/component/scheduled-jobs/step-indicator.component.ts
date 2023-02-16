import { Component, Input } from "@angular/core";

import { Step, StepConfig } from "@registry/model/registry";

@Component({
    selector: "step-indicator",
    templateUrl: "./step-indicator.component.html",
    styleUrls: ["./step-indicator.css"]
})
export class StepIndicatorComponent {

    // eslint-disable-next-line accessor-pairs
    @Input("steps")
    set steps(value: StepConfig) {
        this._stepConfig = value;
    }

    _stepConfig: StepConfig;
    step: Step;

    constructor() {
        this._stepConfig = { steps: [] };
    }

    ngOnInit(): void {
    }

    ngOnDestroy() {

    }

}