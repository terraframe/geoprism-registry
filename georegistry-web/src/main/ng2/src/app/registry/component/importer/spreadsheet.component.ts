import { Component, OnInit, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { FileSelectDirective, FileDropDirective, FileUploader, FileUploaderOptions } from 'ng2-file-upload/ng2-file-upload';
import { HttpErrorResponse } from "@angular/common/http";

import { SuccessModalComponent } from '../../../shared/component/modals/success-modal.component';
import { ErrorModalComponent } from '../../../shared/component/modals/error-modal.component';
import { SpreadsheetModalComponent } from './modals/spreadsheet-modal.component';

import { IOService } from '../../service/io.service';
import { EventService } from '../../../shared/service/event.service';
import { LocalizationService } from '../../../shared/service/localization.service';
import { ImportStrategy } from '../../model/registry';

declare var acp: string;

@Component( {

    selector: 'spreadsheet',
    templateUrl: './spreadsheet.component.html',
    styleUrls: []
} )
export class SpreadsheetComponent implements OnInit {

    /*
     * List of geo object types from the system
     */
    types: { label: string, code: string }[]

    importStrategy: ImportStrategy;
    importStrategies: any[] = [
        {"strategy": ImportStrategy.NEW_AND_UPDATE, "label": "new and update"},
        {"strategy": ImportStrategy.NEW_ONLY, "label": "new only"},
        {"strategy": ImportStrategy.UPDATE_ONLY, "label": "update only"}
    ]

    /*
     * Currently selected code
     */
    code: string = null;
    
    /*
     * Currently start date
     */
    startDate: string = null;

    /*
     * Reference to the modal current showing
     */
    bsModalRef: BsModalRef;

    /*
     * File uploader
     */
    uploader: FileUploader;

    @ViewChild( 'myFile' )
    fileRef: ElementRef;


    constructor( private service: IOService, private eventService: EventService, private modalService: BsModalService, private localizationService: LocalizationService ) { }

    ngOnInit(): void {
        this.service.listGeoObjectTypes( true ).then( types => {
            this.types = types;

        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );

        let options: FileUploaderOptions = {
            queueLimit: 1,
            removeAfterUpload: true,
            url: acp + '/excel/get-configuration'
        };

        this.uploader = new FileUploader( options );

        this.uploader.onBuildItemForm = ( fileItem: any, form: any ) => {
            form.append( 'type', this.code );
            
            if ( this.startDate != null ) {
                form.append( 'startDate', this.startDate );
            }
            if (this.importStrategy) {
                form.append( 'strategy', this.importStrategy)
            }
        };
        this.uploader.onBeforeUploadItem = ( fileItem: any ) => {
            this.eventService.start();
        };
        this.uploader.onCompleteItem = ( item: any, response: any, status: any, headers: any ) => {
            this.fileRef.nativeElement.value = "";
            this.eventService.complete();
        };
        this.uploader.onSuccessItem = ( item: any, response: string, status: number, headers: any ) => {
            const configuration = JSON.parse( response );

            this.bsModalRef = this.modalService.show( SpreadsheetModalComponent, { backdrop: true, ignoreBackdropClick: true } );
            this.bsModalRef.content.configuration = configuration;
        };
        this.uploader.onErrorItem = ( item: any, response: string, status: number, headers: any ) => {
            this.error( JSON.parse( response ) );
        }
    }

    onClick(): void {

        if ( this.uploader.queue != null && this.uploader.queue.length > 0 ) {
            this.uploader.uploadAll();
        }
        else {
            this.error( {
                message: this.localizationService.decode( 'io.missing.file' ),
                error:{},
            } );
        }
    }


    public error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            this.bsModalRef.content.message = ( err.error.localizedMessage || err.error.message || err.message );
        }
    }

}
