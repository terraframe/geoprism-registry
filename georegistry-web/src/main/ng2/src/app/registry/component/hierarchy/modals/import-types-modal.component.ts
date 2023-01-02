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
