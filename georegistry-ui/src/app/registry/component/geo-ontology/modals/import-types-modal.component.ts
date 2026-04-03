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

import { Component, OnInit, ElementRef, ViewChild } from "@angular/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Subject } from "rxjs";
import { ErrorHandler } from "@shared/component";
import { Organization } from "@shared/model/core";
import { FileUploader, FileUploaderOptions } from "ng2-file-upload";
import { EventService, LocalizationService } from "@shared/service";

import { GeoRegistryConfiguration } from "@core/model/core"; import { environment } from 'src/environments/environment';

@Component({
    selector: "import-types-modal",
    templateUrl: "./import-types-modal.component.html",
    styleUrls: []
})
export class ImportTypesModalComponent implements OnInit {

  public organizations: Organization[] = [];
  public orgCode: string;

  /*
   * File uploader
   */
  uploader: FileUploader;

  @ViewChild("myFile")
  fileRef: ElementRef;

  message: string = null;

  public onNodeChange: Subject<boolean>;

  constructor(private eventService: EventService, private localizationService: LocalizationService, public bsModalRef: BsModalRef) {
  }

  ngOnInit(): void {
      this.onNodeChange = new Subject();
  }

  init(organizations: Organization[]): void {
      this.organizations = organizations;

      let options: FileUploaderOptions = {
          queueLimit: 1,
          removeAfterUpload: true,
          url: environment.apiUrl + "/api/cgr/import-types"
      };

      this.uploader = new FileUploader(options);

      this.uploader.onBuildItemForm = (fileItem: any, form: any) => {
          form.append("orgCode", this.orgCode);
      };
      this.uploader.onBeforeUploadItem = (fileItem: any) => {
          this.eventService.start();
      };
      this.uploader.onCompleteItem = (item: any, response: any, status: any, headers: any) => {
          this.fileRef.nativeElement.value = "";
          this.eventService.complete();
      };
      this.uploader.onSuccessItem = (item: any, response: string, status: number, headers: any) => {
          this.onNodeChange.next(true);
          this.bsModalRef.hide();
      };
      this.uploader.onErrorItem = (item: any, response: string, status: number, headers: any) => {
          const error = JSON.parse(response);

          this.error({ error: error });
      };
  }

  onSelect(event: Event): void {
    this.orgCode = (event.target as HTMLInputElement).value;    
  }

  onClick(): void {
      if (this.uploader.queue != null && this.uploader.queue.length > 0) {
          this.uploader.uploadAll();
      } else {
          this.error({
              message: this.localizationService.decode("io.missing.file"),
              error: {}
          });
      }
  }

  public error(err: any): void {
      this.message = ErrorHandler.getMessageFromError(err);
  }

}
