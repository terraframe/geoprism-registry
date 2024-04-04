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

export interface IEventListener {
    start(): void;
    complete(): void;
}

export interface ISessionListener {
    onLogin(): void;
    onLogout(): void;
}

@Injectable()
export class EventService {

    private eventListeners: IEventListener[] = [];

    private sessionListeners: ISessionListener[] = [];

    public registerListener(listener: IEventListener): void {
        this.eventListeners.push(listener);
    }

    public deregisterListener(listener: IEventListener): boolean {
        let indexOfItem = this.eventListeners.indexOf(listener);

        if (indexOfItem === -1) {
            return false;
        }

        this.eventListeners.splice(indexOfItem, 1);

        return true;
    }

    public start(): void {

        setTimeout(() => {
            for (const listener of this.eventListeners) {
                listener.start();
            }
        }, 1);
    }

    public complete(): void {
        setTimeout(() => {
            for (const listener of this.eventListeners) {
                listener.complete();
            }
        }, 1);
    }

    public registerSessionListener(listener: ISessionListener): void {
        this.sessionListeners.push(listener);
    }

    public deregisterSessionListener(listener: ISessionListener): boolean {
        let indexOfItem = this.sessionListeners.indexOf(listener);

        if (indexOfItem === -1) {
            return false;
        }

        this.sessionListeners.splice(indexOfItem, 1);

        return true;
    }

    public onLogin(): void {

        setTimeout(() => {
            for (const listener of this.sessionListeners) {
                listener.onLogin();
            }
        }, 1);
    }

    public onLogout(): void {
        setTimeout(() => {
            for (const listener of this.sessionListeners) {
                listener.onLogout();
            }
        }, 1);
    }

}
