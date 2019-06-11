import { Component, OnInit, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { FileSelectDirective, FileDropDirective, FileUploader, FileUploaderOptions } from 'ng2-file-upload/ng2-file-upload';

import { ErrorModalComponent } from '../../core/modals/error-modal.component';
import { ShapefileModalComponent } from './modals/shapefile-modal.component';

import { IOService } from '../../service/io.service';
import { EventService } from '../../event/event.service';
import { LocalizationService } from '../../core/service/localization.service';

declare var acp: string;

@Component( {

    selector: 'shapefile',
    templateUrl: './shapefile.component.html',
    styleUrls: []
} )
export class ShapefileComponent implements OnInit {

    /*
     * List of geo object types from the system
     */
    types: { label: string, code: string }[]

    /*
     * Currently selected code
     */
    code: string = null;

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

        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );

        let options: FileUploaderOptions = {
            queueLimit: 1,
            removeAfterUpload: true,
            url: acp + '/shapefile/get-shapefile-configuration'
        };

        this.uploader = new FileUploader( options );
        this.uploader.onBuildItemForm = ( fileItem: any, form: any ) => {
            form.append( 'type', this.code );
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

            this.bsModalRef = this.modalService.show( ShapefileModalComponent, { backdrop: true } );
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
            this.error( { message: this.localizationService.decode( 'io.missing.file' ) } );
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
