import { Injectable } from "@angular/core";
import { Subject } from "rxjs";
import { Step, StepConfig } from "@shared/model/modal";

import { GeoRegistryConfiguration } from "@core/model/core"; import { environment } from 'src/environments/environment';

@Injectable()
export class ModalStepIndicatorService {

    stepConfig: StepConfig;
    step: Step;
    private modalStepChangedSource = new Subject<StepConfig>();
    modalStepChange = this.modalStepChangedSource.asObservable();


    // eslint-disable-next-line no-useless-constructor
    constructor() { }

    public getStepConfig(): StepConfig {
        return this.stepConfig;
    }

    public setStepConfig(config: StepConfig): void {
        this.stepConfig = config;

        this.modalStepChangedSource.next(this.stepConfig);
    }

    getStep(): Step {
        return this.step;
    }

    setStep(step: Step): void {
        this.step = step;
    }
}
