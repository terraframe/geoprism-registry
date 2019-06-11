import { Component } from '@angular/core';

import { PageEvent, ChangeRequest } from './crtable';

@Component( {

    selector: 'registry-viewer',
    templateUrl: './registry-viewer.component.html',
    styleUrls: []
} )
export class RegistryViewerComponent {

    state: string = 'REQUESTS';
    request: ChangeRequest;

    constructor() { }

    onPageChange( event: PageEvent ): void {
        if ( event.type === 'BACK' ) {
            this.handleBack( event );
        }
        else if ( event.type === 'NEXT' ) {
            this.handleNext( event );
        }
    }

    handleBack( event: PageEvent ): void {
        if ( this.state === 'ACTIONS' ) {
            this.state = 'REQUESTS';
        }
    }

    handleNext( event: PageEvent ): void {
        if ( this.state === 'REQUESTS' ) {
            this.state = 'ACTIONS';
            this.request = event.data;            
        }
    }

}
