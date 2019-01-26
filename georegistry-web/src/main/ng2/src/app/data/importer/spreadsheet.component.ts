import { Component, OnInit, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { FileSelectDirective, FileDropDirective, FileUploader, FileUploaderOptions } from 'ng2-file-upload/ng2-file-upload';

import { ErrorModalComponent } from '../../core/modals/error-modal.component';
import { SpreadsheetModalComponent } from './modals/spreadsheet-modal.component';

import { ShapefileService } from '../../service/shapefile.service';
import { ExcelService } from '../../service/excel.service';
import { EventService } from '../../event/event.service';

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
    private types: { label: string, code: string }[]

    /*
     * Currently selected code
     */
    private code: string = null;

    /*
     * Reference to the modal current showing
     */
    private bsModalRef: BsModalRef;

    /*
     * File uploader
     */
    private uploader: FileUploader;

    constructor( private service: ExcelService, private sService: ShapefileService, private eventService: EventService, private modalService: BsModalService ) { }

    ngOnInit(): void {
        this.sService.listGeoObjectTypes().then( types => {
            this.types = types;

        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );

        let options: FileUploaderOptions = {
            queueLimit: 1,
            removeAfterUpload: true,
            url: acp + '/excel/get-configuration'
        };

        this.uploader = new FileUploader( options );

        this.uploader.onBuildItemForm = ( fileItem: any, form: any ) => {
            form.append( 'type', this.code );
        };
        this.uploader.onBeforeUploadItem = ( fileItem: any ) => {
            this.eventService.start();
        };
        this.uploader.onCompleteItem = ( item: any, response: any, status: any, headers: any ) => {
            this.eventService.complete();
        };
        this.uploader.onSuccessItem = ( item: any, response: string, status: number, headers: any ) => {
            const configuration = JSON.parse( response );

            this.bsModalRef = this.modalService.show( SpreadsheetModalComponent, { backdrop: true } );
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
            this.error( { message: 'File is required' } );
        }
    }

    public error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            this.bsModalRef.content.message = ( err.localizedMessage || err.message );
        }
    }

}
