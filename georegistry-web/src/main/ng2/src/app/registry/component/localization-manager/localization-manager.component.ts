///
/// Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Runway SDK(tm).
///
/// Runway SDK(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Runway SDK(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { Component, OnInit } from '@angular/core';
import { HttpHeaders, HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

import { EventService } from '../../../shared/service/event.service';

import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { LocalizationManagerService } from '../../service/localization-manager.service';

import { ErrorModalComponent } from '../../../shared/component/modals/error-modal.component';
import { NewLocaleModalComponent } from './new-locale-modal.component';

declare var acp: any;

@Component( {

    selector: 'localization-manager',
    templateUrl: './localization-manager.component.html',
    styleUrls: []
} )
export class LocalizationManagerComponent implements OnInit {



    constructor( private router: Router, private eventService: EventService, private http: HttpClient, private localizationManagerService: LocalizationManagerService, private modalService: BsModalService ) {

    }

    ngOnInit(): void {

    }

    ngAfterViewInit() {

    }

    showNewLocaleModal() {
        let bsModalRef = this.modalService.show( NewLocaleModalComponent, { backdrop: true } );
    }

    importLocalization( event: any ) {
        let fileList: FileList = event.target.files;
        if ( fileList.length > 0 ) {
            let file: File = fileList[0];
            let formData: FormData = new FormData();
            formData.append( 'file', file, file.name );

            let headers = new HttpHeaders();

            this.eventService.start();

            this.http.post( acp + "/localization/importSpreadsheet", formData, { headers: headers } )
                .toPromise()
                .then( response => {
                    this.eventService.complete();
                    this.error( { message: "Import success", error: {} } );
                } ).catch(( err: HttpErrorResponse ) => {
                    console.log( err )
                    this.eventService.complete();
                    this.error( err );
                } );
        }
    }

    exportLocalization() {
        console.log( "exporting localization" );

        //this.localizationManagerService.exportLocalization();
        window.location.href = acp + "/localization/exportSpreadsheet";
    }

    public error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            let bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            bsModalRef.content.message = ( err.error.localizedMessage || err.error.message || err.message );
        }
    }

}
