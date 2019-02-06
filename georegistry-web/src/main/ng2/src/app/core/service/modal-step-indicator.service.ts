import { Injectable } from '@angular/core';
// import { Subject } from 'rxjs'
import { Subject } from 'rxjs/Subject';


import { Step, StepConfig } from '../modals/modal';


declare var acp: string;

@Injectable()
export class ModalStepIndicatorService {

    stepConfig: StepConfig;
    step: Step;
    private modalStepChangedSource = new Subject<StepConfig>();
    modalStepChange = this.modalStepChangedSource.asObservable();


    constructor(  ) { }

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
