import { Component, ViewChild, HostListener } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { PageEvent, ChangeRequest } from './crtable';

import { ComponentCanDeactivate, PendingChangesGuard } from '../../core/pending-changes-guard';

@Component( {

    selector: 'registry-viewer',
    templateUrl: './registry-viewer.component.html',
    styleUrls: []
} )
export class RegistryViewerComponent implements ComponentCanDeactivate {

    state: string = 'REQUESTS';
    request: ChangeRequest;
    
    @ViewChild('requestTable') private requestTable;
    @ViewChild('actionTable') private actionTable;

    constructor(private pendingChangesGuard: PendingChangesGuard) { }

    onPageChange( event: PageEvent ): void {
      if ( event.type === 'BACK' ) {
        this.handleBack( event );
      }
      else if ( event.type === 'NEXT' ) {
        this.handleNext( event );
      }
    }

    handleBack( event: PageEvent ): void {
      if (this.pendingChangesGuard.canDeactivate(this))
      {
        if ( this.state === 'ACTIONS' ) {
          this.state = 'REQUESTS';
        }
      }
    }

    handleNext( event: PageEvent ): void {
      if ( this.state === 'REQUESTS' ) {
        this.state = 'ACTIONS';
        this.request = event.data;            
      }
    }
    
    // Big thanks to https://stackoverflow.com/questions/35922071/warn-user-of-unsaved-changes-before-leaving-page
    @HostListener('window:beforeunload')
    canDeactivate(): Observable<boolean> | boolean {
      if ( this.state === "ACTIONS" && this.actionTable != null )
      {
        return this.actionTable.canDeactivate();
      }
      
      return true;
    }
    
    @HostListener('window:unload')
    afterDeactivate(isDeactivating: boolean)
    {
      if (isDeactivating === undefined) { isDeactivating = true; }
    
      if ( this.state === "ACTIONS" && this.actionTable != null )
      {
        return this.actionTable.afterDeactivate(isDeactivating);
      }
    }

}
