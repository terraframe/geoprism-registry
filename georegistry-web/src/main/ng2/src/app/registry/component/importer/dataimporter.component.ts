import { Component, OnInit, Input, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { FileSelectDirective, FileDropDirective, FileUploader, FileUploaderOptions } from 'ng2-file-upload';
import { HttpErrorResponse } from "@angular/common/http";

import { ErrorHandler, ErrorModalComponent, SuccessModalComponent } from '@shared/component';
import { LocalizationService, AuthService, EventService, ExternalSystemService } from '@shared/service';
import { ExternalSystem } from '@shared/model/core';

import { SpreadsheetModalComponent } from './modals/spreadsheet-modal.component';
import { ShapefileModalComponent } from './modals/shapefile-modal.component';
import { IOService } from '@registry/service';
import { ImportStrategy, PRESENT } from '@registry/model/registry';

declare var acp: string;

@Component({

	selector: 'dataimporter',
	templateUrl: './dataimporter.component.html',
	styleUrls: ['./dataimporter.css']
})
export class DataImporterComponent implements OnInit {
	
	currentDate : Date = new Date();

	showImportConfig: boolean = false;

    /*
     * List of geo object types from the system
     */
	types: { label: string, code: string }[]

	importStrategy: ImportStrategy;
	importStrategies: any[] = [
		{ "strategy": ImportStrategy.NEW_AND_UPDATE, "label": this.localizationService.decode("etl.import.ImportStrategy.NEW_AND_UPDATE") },
		{ "strategy": ImportStrategy.NEW_ONLY, "label": this.localizationService.decode("etl.import.ImportStrategy.NEW_ONLY") },
		{ "strategy": ImportStrategy.UPDATE_ONLY, "label": this.localizationService.decode("etl.import.ImportStrategy.UPDATE_ONLY") }
	]

    /*
     * Currently selected code
     */
	code: string = null;

    /*
     * Start date
     */
	startDate: Date = null;

    /*
     * End date
     */
	endDate: Date | string = null;

    /*
     * Reference to the modal current showing
     */
	bsModalRef: BsModalRef;

    /*
     * File uploader
     */
	uploader: FileUploader;

	@ViewChild('myFile')
	fileRef: ElementRef;

	@Input()
	format: string; // Can be SHAPEFILE or EXCEL

	isExternal: boolean = false;

	/*
	 * List of available external systems (filtered based on user's org)
	 */
	externalSystems: ExternalSystem[];

	/*
	 * currently selected external system.
	 */
	externalSystemId: string;

	isLoading: boolean = true;

	copyBlank: boolean = true;

	constructor(private service: IOService,
		private eventService: EventService,
		private modalService: BsModalService,
		private localizationService: LocalizationService,
		private authService: AuthService,
		private sysService: ExternalSystemService
	) { }

	ngOnInit(): void {
		this.sysService.getExternalSystems(1, 100).then(paginatedSystems => {

			this.externalSystems = paginatedSystems.resultSet;

			if (this.externalSystems.length === 0) {
				this.isExternal = false;
				this.showImportConfig = true; // Show the upload widget if there are no external systems registered
			}

			this.isLoading = false;

		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});

		this.service.listGeoObjectTypes(false).then(types => {

			const myOrgTypes = [];

			for (var i = 0; i < types.length; ++i) {
				const type = types[i];
				const orgCode = type.orgCode;
				const typeCode = type.superTypeCode != null ? type.superTypeCode : type.code;

				if (this.authService.isOrganizationRA(orgCode) || this.authService.isGeoObjectTypeRM(orgCode, typeCode)) {
					myOrgTypes.push(types[i]);
				}
			}

			this.types = myOrgTypes;
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});

		var getUrl = acp + '/excel/get-configuration';
		if (this.format === "SHAPEFILE") {
			getUrl = acp + '/shapefile/get-shapefile-configuration';

			//this.showImportConfig = true; // show the upload widget if shapefile because external system from shapefile isn't supported
		}

		let options: FileUploaderOptions = {
			queueLimit: 1,
			removeAfterUpload: true,
			url: getUrl
		};

		this.uploader = new FileUploader(options);

		this.uploader.onBuildItemForm = (fileItem: any, form: any) => {
			form.append('type', this.code);
			form.append('copyBlank', this.copyBlank);

			if (this.startDate != null) {
				form.append('startDate', this.startDate);
			}
			if (this.endDate != null) {
				form.append('endDate', this.endDate);
			}
			if (this.importStrategy) {
				form.append('strategy', this.importStrategy);
			}
		};
		this.uploader.onBeforeUploadItem = (fileItem: any) => {
			this.eventService.start();
		};
		this.uploader.onCompleteItem = (item: any, response: any, status: any, headers: any) => {
			this.fileRef.nativeElement.value = "";
			this.eventService.complete();
		};
		this.uploader.onSuccessItem = (item: any, response: string, status: number, headers: any) => {
			const configuration = JSON.parse(response);

			configuration.isExternal = this.isExternal;

			let externalSystem: ExternalSystem = null;
			for (let i = 0; i < this.externalSystems.length; ++i) {
				let sys: ExternalSystem = this.externalSystems[i];

				if (sys.oid === this.externalSystemId) {
					externalSystem = sys;
				}
			}

			configuration.externalSystemId = this.externalSystemId;
			configuration.externalSystem = externalSystem;

			if (this.format === "SHAPEFILE") {
				this.bsModalRef = this.modalService.show(ShapefileModalComponent, { backdrop: true });
			}
			else {
				this.bsModalRef = this.modalService.show(SpreadsheetModalComponent, { backdrop: true, ignoreBackdropClick: true });
			}

			this.bsModalRef.content.configuration = configuration;
		};
		this.uploader.onErrorItem = (item: any, response: string, status: number, headers: any) => {
			const error = JSON.parse(response)

			this.error({ error: error });
		}
	}

	onClick(): void {

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

	setImportSource(event, type): void {
		if (type === "EXTERNAL") {
			this.isExternal = true;
		}
		else {
			this.isExternal = false;
		}
	}

	onNext(): void {
		this.showImportConfig = true;
	}

	onBack(): void {
		this.showImportConfig = false;
	}
	
	setInfinity(endDate: any): void {

		if(endDate === PRESENT){
			this.endDate = null;
		}
		else{
			this.endDate = PRESENT;
		}
	}


	public error(err: any): void {
		this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
	}

}
