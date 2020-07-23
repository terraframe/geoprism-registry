import { Component, ViewChild, ElementRef } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { BsModalService } from 'ngx-bootstrap/modal';
import { FileUploader, FileUploaderOptions } from 'ng2-file-upload';

import { ErrorModalComponent } from '../../../shared/component/modals/error-modal.component';
import { EventService } from '../../../shared/service/event.service';
import { LocalizationService } from '../../../shared/service/localization.service';
import { ErrorHandler } from '../../../shared/component/error-handler/error-handler';

declare var acp: any;


@Component({
	selector: 'import-localization-modal',
	templateUrl: './import-localization-modal.component.html',
	styleUrls: []
})
export class ImportLocalizationModalComponent {

	@ViewChild('myFile')
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
			url: acp + "/localization/importSpreadsheet"
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
			const error = JSON.parse(response)

			this.error({ error: error });
		}
	}

	submit(): void {

		if (this.uploader.queue != null && this.uploader.queue.length > 0) {
			this.uploader.uploadAll();
		}
		else {
			this.error({
				message: this.localizationService.decode('io.missing.file'),
				error: {},
			});
		}
	}

	cancel(): void {
		this.bsModalRef.hide();
	}

	public error(err: any): void {
			this.bsModalRef = this.modalService.show(ErrorModalComponent, { backdrop: true });
			this.bsModalRef.content.message = ErrorHandler.getMessageFromError(err);
	}

}
