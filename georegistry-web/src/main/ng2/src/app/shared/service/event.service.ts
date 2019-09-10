import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Response } from '@angular/http';

import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { ErrorModalComponent } from '../component/modals/error-modal.component';
import { MessageContainer } from '../model/core';

declare var acp;

export interface IEventListener {
    start(): void;
    complete(): void;
}

@Injectable()
export class EventService {
    private listeners: IEventListener[] = [];

    bsModalRef: BsModalRef;
    modalService: BsModalService;

    constructor( modalService: BsModalService ) {
        this.modalService = modalService;
    }


    public registerListener( listener: IEventListener ): void {
        this.listeners.push( listener );
    }

    public deregisterListener( listener: IEventListener ): boolean {
        let indexOfItem = this.listeners.indexOf( listener );

        if ( indexOfItem === -1 ) {
            return false;
        }

        this.listeners.splice( indexOfItem, 1 );

        return true;
    }

    public start(): void {
        for ( const listener of this.listeners ) {
            listener.start();
        }
    }

    public complete(): void {
        for ( const listener of this.listeners ) {
            listener.complete();
        }
    }

    public error( resp: any, container: MessageContainer ): void {
        // Handle error
        if ( resp !== null ) {
            if ( resp instanceof Response ) {

                if ( resp.status == 401 ) {
                    window.location.href = acp + "/cgr/manage#/login";
                }
                else {
                    const err: any = resp.json();

                    if ( container != null ) {
                        container.setMessage( err.localizedMessage || err.message );
                    }
                    else {
                        this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
                        this.bsModalRef.content.message = ( err.localizedMessage || err.message );
                    }
                }
            }
            else if ( typeof resp === 'string' ) {

                if ( container != null ) {
                    container.setMessage( resp );
                }
                else {
                    this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
                    this.bsModalRef.content.message = resp;
                }

            }
        }
    }


}
