import { Component, Input, ViewChild, ElementRef } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';
import { LocalizationService } from '../../../../shared/service/localization.service';
import { ModalTypes } from '../../../../shared/model/modal';
import { FileSelectDirective, FileDropDirective, FileUploader, FileUploaderOptions } from 'ng2-file-upload/ng2-file-upload';
import { EventService } from '../../../../shared/service/event.service'
import { ScheduledJob } from '../../../model/registry';

declare var acp: any;

@Component( {
    selector: 'reupload-modal',
    templateUrl: './reupload-modal.component.html',
    styleUrls: ['../../../../shared/component/modals/modal.css']
} )
export class ReuploadModalComponent {
  @Input() title: string = this.localizeService.decode("reuploadmodal.title");

  @Input() message: string = this.localizeService.decode("reuploadmodal.message");
  
  @Input() data: any;
  
  @Input() submitText: string = this.localizeService.decode("reuploadmodal.import");
  
  @Input() cancelText: string = this.localizeService.decode("modal.button.cancel");
  
  @Input() type: ModalTypes = ModalTypes.warning;
  
  @Input() job: ScheduledJob;
  
  @ViewChild( 'myFile' )
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
  
  constructor( public bsModalRef: BsModalRef, private localizeService: LocalizationService, private eventService: EventService ) { }
  
  ngOnInit(): void {
    this.onConfirm = new Subject();
    
    let options: FileUploaderOptions = {
      queueLimit: 1,
      removeAfterUpload: true,
      url: acp + '/etl/reimport'
    };
    
    this.uploader = new FileUploader( options );
    this.uploader.onBuildItemForm = ( fileItem: any, form: any ) => {
        form.append( 'json', JSON.stringify(this.job.configuration) );
    };
    this.uploader.onBeforeUploadItem = ( fileItem: any ) => {
        this.eventService.start();
    };
    this.uploader.onCompleteItem = ( item: any, response: any, status: any, headers: any ) => {
        this.fileRef.nativeElement.value = "";
        this.eventService.complete();
    };
    this.uploader.onSuccessItem = ( item: any, response: string, status: number, headers: any ) => {
      this.onConfirm.next( this.data );
      this.bsModalRef.hide();
    };
    this.uploader.onErrorItem = ( item: any, response: string, status: number, headers: any ) => {
      this.error( JSON.parse( response ) );
    }
  }
  
  toString(data: any): string
  {
    return JSON.stringify(data);
  }
  
  confirm(): void {
    if ( this.uploader.queue != null && this.uploader.queue.length > 0 ) {
      this.uploader.uploadAll();
    }
    else {
      this.error( { message: this.localizeService.decode( 'io.missing.file' ), error: {} } );
    }
  }
  
  public error( err: any ): void {
    console.log("ERROR", err);
  
    // Handle error
    if ( err !== null ) {
      this.error = ( err.error.localizedMessage || err.error.message || err.message );
    }
  }
}
