import { Injectable } from "@angular/core";

export interface IEventListener {
    start(): void;
    complete(): void;
}

@Injectable()
export class EventService {

    private listeners: IEventListener[] = [];

    public registerListener(listener: IEventListener): void {
        this.listeners.push(listener);
    }

    public deregisterListener(listener: IEventListener): boolean {
        let indexOfItem = this.listeners.indexOf(listener);

        if (indexOfItem === -1) {
            return false;
        }

        this.listeners.splice(indexOfItem, 1);

        return true;
    }

    public start(): void {

        setTimeout(() => {
            for (const listener of this.listeners) {
                listener.start();
            }
        }, 1);
    }

    public complete(): void {
        setTimeout(() => {
            for (const listener of this.listeners) {
                listener.complete();
            }
        }, 1);
    }
}
