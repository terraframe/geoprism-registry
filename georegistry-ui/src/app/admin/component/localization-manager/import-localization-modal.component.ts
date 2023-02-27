///
/// Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Geoprism Registry(tm).
///
/// Geoprism Registry(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Geoprism Registry(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { Component, ViewChild, ElementRef } from "@angular/core";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { FileUploader, FileUploaderOptions } from "ng2-file-upload";

import { ErrorHandler } from "@shared/component";
import { LocalizationService, EventService } from "@shared/service";
import { HttpErrorResponse } from "@angular/common/http";
import { LocalizationFeedbackModalComponent } from "./localization-feedback-modal.component";

import { GeoRegistryConfiguration } from "@core/model/core"; import { environment } from 'src/environments/environment';

@Component({
    selector: "import-localization-modal",
    templateUrl: "./import-localization-modal.component.html",
    styleUrls: []
})
export class ImportLocalizationModalComponent {

    @ViewChild("myFile")
    fileRef: ElementRef;

    /*
     * File uploader
     */
    uploader: FileUploader;

    constructor(public bsModalRef: BsModalRef, private localizationService: LocalizationService, private eventService: EventService, private modalService: BsModalService) { }

    ngOnInit(): void {
        let options: FileUploaderOptions = {
            queueLimit: 1,
            removeAfterUpload: true,
            url: environment.apiUrl + "/api/localization/importSpreadsheet"
        };

        this.uploader = new FileUploader(options);

        this.uploader.onBuildItemForm = (fileItem: any, form: any) => {
        };
        this.uploader.onBeforeUploadItem = (fileItem: any) => {
            this.eventService.start();
        };
        this.uploader.onCompleteItem = (item: any, response: any, status: any, headers: any) => {
            this.fileRef.nativeElement.value = "";
            this.eventService.complete();
        };
        this.uploader.onSuccessItem = (item: any, response: string, status: number, headers: any) => {
            this.bsModalRef.hide();
        };
        this.uploader.onErrorItem = (item: any, response: string, status: number, headers: any) => {
            let error = JSON.parse(response);

            if (error instanceof HttpErrorResponse && error.status === 401) {
                return null;
            }

            let newline = "\n";

            let errMsg = error.localizedMessage.replaceAll("\\n", newline);

            let bsModalRef = this.modalService.show(LocalizationFeedbackModalComponent, { backdrop: true, class: "error-white-space-pre" });

            bsModalRef.content.message = errMsg;

            return bsModalRef;
        };
    }

    submit(): void {
        if (this.uploader.queue != null && this.uploader.queue.length > 0) {
            this.uploader.uploadAll();
        } else {
            this.error({
                message: this.localizationService.decode("io.missing.file"),
                error: {}
            });
        }
    }

    cancel(): void {
        this.bsModalRef.hide();
    }

    public error(err: any): void {
        ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

}
