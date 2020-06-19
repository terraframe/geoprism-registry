import { Component, OnInit, Input, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { FileSelectDirective, FileDropDirective, FileUploader, FileUploaderOptions } from 'ng2-file-upload';
import { HttpErrorResponse } from "@angular/common/http";

import { SuccessModalComponent } from '../../../shared/component/modals/success-modal.component';
import { ErrorModalComponent } from '../../../shared/component/modals/error-modal.component';
import { SpreadsheetModalComponent } from './modals/spreadsheet-modal.component';
import { ShapefileModalComponent } from './modals/shapefile-modal.component';

import { IOService } from '../../service/io.service';
import { EventService } from '../../../shared/service/event.service';
import { AuthService } from '../../../shared/service/auth.service';
import { ExternalSystemService } from '../../../shared/service/external-system.service';
import { LocalizationService } from '../../../shared/service/localization.service';
import { ImportStrategy } from '../../model/registry';
import { ExternalSystem } from '../../../shared/model/core';

declare var acp: string;

@Component({

	selector: 'dataimporter',
	templateUrl: './dataimporter.component.html',
	styleUrls: []
})
export class DataImporterComponent implements OnInit {

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
     * Currently start date
     */
	startDate: string = null;

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
  
  isExternal: boolean = null;
  
  /*
   * List of available external systems (filtered based on user's org)
   */
  externalSystems: ExternalSystem[];
  
  /*
   * currently selected external system.
   */ 
  externalSystemId: string;
  
  isLoading: boolean = true;

	constructor(private service: IOService,
    	private eventService: EventService,
    	private modalService: BsModalService,
    	private localizationService: LocalizationService,
    	private router: Router,
    	private authService: AuthService,
    	private sysService: ExternalSystemService
   ) { }

	ngOnInit(): void {
	  this.sysService.getExternalSystems(1, 100).then(paginatedSystems => {
      
      this.externalSystems = paginatedSystems.resultSet;
      
      if (this.externalSystems.length == 0)
      {
        this.isExternal = false;
      }
      
      this.isLoading = false;

    }).catch((err: HttpErrorResponse) => {
      this.error(err);
    });
	
		this.service.listGeoObjectTypes(true).then(types => {
			
			var myOrgTypes = [];
            for (var i = 0; i < types.length; ++i)
            {
              if (this.authService.isOrganizationRA(types[i].orgCode))
              {
                myOrgTypes.push(types[i]);
              }
            }
            this.types = myOrgTypes;

		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
		
		var getUrl = acp + '/excel/get-configuration';
		if (this.format === "SHAPEFILE")
		{
		  getUrl = acp + '/shapefile/get-shapefile-configuration';
		}

		let options: FileUploaderOptions = {
			queueLimit: 1,
			removeAfterUpload: true,
			url: getUrl
		};

		this.uploader = new FileUploader(options);

		this.uploader.onBuildItemForm = (fileItem: any, form: any) => {
			form.append('type', this.code);

			if (this.startDate != null) {
				form.append('startDate', this.startDate);
			}
			if (this.importStrategy) {
				form.append('strategy', this.importStrategy)
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
			for (let i = 0; i < this.externalSystems.length; ++i)
			{
			  let sys: ExternalSystem = this.externalSystems[i];
			  
			  if (sys.oid === this.externalSystemId)
			  {
			    externalSystem = sys;
			  }
			}
			
			configuration.externalSystemId = this.externalSystemId;
			configuration.externalSystem = externalSystem;
			
      if (this.format === "SHAPEFILE")
	    {
	      this.bsModalRef = this.modalService.show(ShapefileModalComponent, { backdrop: true });
	    }
	    else
	    {
	      this.bsModalRef = this.modalService.show(SpreadsheetModalComponent, { backdrop: true, ignoreBackdropClick: true });
	    }

			this.bsModalRef.content.configuration = configuration;
		};
		this.uploader.onErrorItem = (item: any, response: string, status: number, headers: any) => {
			const error = JSON.parse(response)

			this.error({ error: error });
		}
	}
	
	onClickImportFile(): void {
	  this.isExternal = false;
	}
	
	onClickImportExternalSystem(): void {
	  this.isExternal = true;
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


	public error(err: any): void {
		// Handle error
		if (err !== null) {
			this.bsModalRef = this.modalService.show(ErrorModalComponent, { backdrop: true });
			this.bsModalRef.content.message = (err.error.localizedMessage || err.error.message || err.message);
		}
	}

}
