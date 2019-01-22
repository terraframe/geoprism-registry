import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { GeoObjectType } from '../../hierarchy/hierarchy';
import { ShapefileConfiguration } from '../shapefile';

import { ExcelService } from '../../../service/excel.service';

@Component( {
    selector: 'spreadsheet-modal',
    templateUrl: './spreadsheet-modal.component.html',
    styleUrls: []
} )
export class SpreadsheetModalComponent implements OnInit {

    configuration: ShapefileConfiguration;

    message: string = null;

    constructor( private service: ExcelService, public bsModalRef: BsModalRef ) {
    }

    ngOnInit(): void {
    }

    onSubmit(): void {
        this.service.importSpreadsheet( this.configuration ).then( response => {
            this.bsModalRef.hide()
        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );

    }

    onCancel(): void {
        this.service.cancelImport( this.configuration ).then( response => {
            this.bsModalRef.hide()
        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
    }

    error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.localizedMessage || err.message );
        }
    }
}
