///
/// Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Geoprism Registry(tm).
///
/// Geoprism Registry(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Geoprism Registry(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
///

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
