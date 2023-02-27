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
import { Progress } from "@shared/model/progress";

export interface IProgressListener {
    start(): void;
    progress(progress: Progress): void;
    complete(): void;
}

@Injectable()
export class ProgressService {

    private listeners: IProgressListener[] = [];

    // eslint-disable-next-line no-useless-constructor
    public constructor() { }

    public registerListener(listener: IProgressListener): void {
        this.listeners.push(listener);
    }

    public deregisterListener(listener: IProgressListener): boolean {
        let indexOfItem = this.listeners.indexOf(listener);

        if (indexOfItem === -1) {
            return false;
        }

        this.listeners.splice(indexOfItem, 1);

        return true;
    }

    public start(): void {
        for (const listener of this.listeners) {
            listener.start();
        }
    }

    public progress(progress: Progress): void {
        for (const listener of this.listeners) {
            listener.progress(progress);
        }
    }

    public complete(): void {
        for (const listener of this.listeners) {
            listener.complete();
        }
    }
}