import { Component, Input } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { TreeNode } from 'angular-tree-component';

import { BsModalService } from 'ngx-bootstrap/modal';

import { LocalizationManagerService } from '../../service/localization-manager.service';

import { EventService } from '../../event/event.service';

import { AllLocaleInfo } from './localization-manager';

import { ErrorModalComponent } from '../../core/modals/error-modal.component';

@Component( {
    selector: 'new-locale-modal',
    templateUrl: './new-locale-modal.component.html',
    styleUrls: []
} )
export class NewLocaleModalComponent {

    private allLocaleInfo: AllLocaleInfo;
    
    private language: string;
    
    private country: string;
    
    private variant: string;

    constructor( public bsModalRef: BsModalRef, private localizationManagerService: LocalizationManagerService, private eventService: EventService, private modalService: BsModalService ) { }

    ngOnInit(): void {
      this.allLocaleInfo = new AllLocaleInfo();
    
      this.localizationManagerService.getNewLocaleInfo()
      .then( allLocaleInfoIN => {
        this.allLocaleInfo = allLocaleInfoIN;
        this.eventService.complete();
      }).catch(( err: any ) => {
        console.log(err);
        
        this.bsModalRef.hide();
        this.eventService.complete();
        this.error( err.json() );
      });
    }

    submit(): void {
      this.eventService.start();
      
      this.localizationManagerService.installLocale(this.language, this.country, this.variant)
      .then( () => {
        this.eventService.complete();
        this.bsModalRef.hide();
      }).catch(( err: any ) => {
        console.log(err);
        
        this.bsModalRef.hide();
        this.eventService.complete();
        this.error( err.json() );
      });
    }
    
    cancel(): void {
      this.bsModalRef.hide();
      
      
    }
    
    public error( err: any ): void {
      // Handle error
      if ( err !== null ) {
          let bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
          bsModalRef.content.message = ( err.localizedMessage || err.message );
      }
    }
}
