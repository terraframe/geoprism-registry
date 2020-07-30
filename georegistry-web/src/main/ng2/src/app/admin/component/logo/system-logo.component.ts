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

import { Component, OnInit, ViewChild, ElementRef, Inject, Input } from '@angular/core';
import { ActivatedRoute, Params, Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Location } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';
import { ErrorHandler } from '@shared/component/error-handler/error-handler';
import { FileSelectDirective, FileDropDirective, FileUploader, FileUploaderOptions } from 'ng2-file-upload';

import { Observable } from 'rxjs';


import { EventService } from '@shared/service/event.service';
import { SystemLogoService } from '@admin/service/system-logo.service';
import { SystemLogo } from '@admin/model/system-logo';

declare var acp: any;

@Component( {

    selector: 'system-logo',
    templateUrl: './system-logo.component.html',
    styles: []
} )
export class SystemLogoComponent implements OnInit {
    message: string = null;
    icon: SystemLogo;

    public uploader: FileUploader;
    public dropActive: boolean = false;

    file: any;
    context: string;

    @ViewChild( 'uploadEl' )
    private uploadElRef: ElementRef;

    public onSuccess: Subject<any>;


    constructor( private route: ActivatedRoute,
        private eventService: EventService,
        public bsModalRef: BsModalRef )
        {
            this.context = acp as string;
        }

    ngOnInit(): void {

        this.onSuccess = new Subject();

        let options: FileUploaderOptions = {
            autoUpload: false,
            queueLimit: 1,
            removeAfterUpload: true,
            url: acp + '/logo/apply'
        };

        this.uploader = new FileUploader( options );

        this.uploader.onBeforeUploadItem = ( fileItem: any ) => {
            this.eventService.start();
        };
        this.uploader.onCompleteItem = ( item: any, response: any, status: any, headers: any ) => {
            this.eventService.complete();
        };
        this.uploader.onSuccessItem = ( item: any, response: string, status: number, headers: any ) => {
            this.onSuccess.next( item );
            this.bsModalRef.hide();
        };
        this.uploader.onErrorItem = ( item: any, response: string, status: number, headers: any ) => {
            this.error( response );
        };
        this.uploader.onBuildItemForm = ( fileItem: any, form: any ) => {
            form.append( 'oid', this.icon.oid );
        };
    }

    ngAfterViewInit() {
        let that = this;

        this.uploader.onAfterAddingFile = ( item => {
            this.uploadElRef.nativeElement.value = '';

            let reader = new FileReader();
            reader.onload = function( e: any ) {
                that.file = reader.result;
            };
            reader.readAsDataURL( item._file );
        } );
    }

    fileOver( e: any ): void {
        this.dropActive = e;
    }

    cancel(): void {
        this.bsModalRef.hide();
    }

    onSubmit(): void {
        if( this.file == null ) {
            this.bsModalRef.hide();
        }
        else {
            this.uploader.uploadAll();
        }
    }

    clear(): void {
        this.file = null;

        this.uploader.clearQueue()
    }

    error( err: any ): void {
            this.message = ErrorHandler.getMessageFromError(err);
    }
}
