import { Component } from '@angular/core';
import { ModalStepIndicatorService } from '@shared/service';

import { Step, StepConfig } from '@shared/model/modal';
import { Subscription } from 'rxjs';


@Component( { 
    selector: 'modal-step-indicator',
    templateUrl: './modal-step-indicator.component.html',
    styleUrls: ['./modal-step-indicator.css']
} )
export class ModalStepIndicatorComponent {

    stepConfig: StepConfig;
    step: Step;
    stepSubscription: Subscription;

    constructor( private modalStepIndicatorService: ModalStepIndicatorService ) { 
        this.stepSubscription = modalStepIndicatorService.modalStepChange.subscribe( stepConfig => {
            this.stepConfig = stepConfig;
        })

    }

    ngOnInit(): void {
    }

    ngOnDestroy() {
        this.stepSubscription.unsubscribe();
    }
}
