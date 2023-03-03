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

import { Component, Input, ViewChild, ElementRef } from "@angular/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Subject } from "rxjs";
import { FileUploader, FileUploaderOptions } from "ng2-file-upload";

import { ModalTypes } from "@shared/model/modal";
import { LocalizationService, EventService } from "@shared/service";
import { ErrorHandler } from "@shared/component";

import { ScheduledJob } from "@registry/model/registry";

import { GeoRegistryConfiguration } from "@core/model/core"; import { environment } from 'src/environments/environment';

@Component({
    selector: "reupload-modal",
    templateUrl: "./reupload-modal.component.html",
    styleUrls: []
})
export class ReuploadModalComponent {

  @Input() title: string = this.localizeService.decode("reuploadmodal.title");

  @Input() message: string = this.localizeService.decode("reuploadmodal.message");

  @Input() data: any;

  @Input() submitText: string = this.localizeService.decode("reuploadmodal.import");

  @Input() cancelText: string = this.localizeService.decode("modal.button.cancel");

  @Input() type: ModalTypes = ModalTypes.warning;

  @Input() job: ScheduledJob;

  @ViewChild("myFile")
  fileRef: ElementRef;

  /*
   * File uploader
   */
  uploader: FileUploader;

  errorMessage: string;

  /*
   * Called on confirm
   */
  public onConfirm: Subject<any>;

  constructor(public bsModalRef: BsModalRef, private localizeService: LocalizationService, private eventService: EventService) { }

  ngOnInit(): void {
      this.onConfirm = new Subject();

      let options: FileUploaderOptions = {
          queueLimit: 1,
          removeAfterUpload: true,
          url: environment.apiUrl + "/api/etl/reimport"
      };

      this.uploader = new FileUploader(options);
      this.uploader.onBuildItemForm = (fileItem: any, form: any) => {
          form.append("config", JSON.stringify(this.job.configuration));
      };
      this.uploader.onBeforeUploadItem = (fileItem: any) => {
          this.eventService.start();
      };
      this.uploader.onCompleteItem = (item: any, response: any, status: any, headers: any) => {
          this.fileRef.nativeElement.value = "";
          this.eventService.complete();
      };
      this.uploader.onSuccessItem = (item: any, response: string, status: number, headers: any) => {
          this.onConfirm.next(this.data);
          this.bsModalRef.hide();
      };
      this.uploader.onErrorItem = (item: any, response: string, status: number, headers: any) => {
          this.error(JSON.parse(response));
      };
  }

  toString(data: any): string {
      return JSON.stringify(data);
  }

  confirm(): void {
      if (this.uploader.queue != null && this.uploader.queue.length > 0) {
          this.uploader.uploadAll();
      } else {
          this.error({ message: this.localizeService.decode("io.missing.file"), error: {} });
      }
  }

  public error(err: any): void {
      this.errorMessage = ErrorHandler.getMessageFromError(err);
  }

}
