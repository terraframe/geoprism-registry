import { Component, OnInit, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef } from '@angular/core';
import { HttpErrorResponse } from "@angular/common/http";
import { Router } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { FileSelectDirective, FileDropDirective, FileUploader, FileUploaderOptions } from 'ng2-file-upload';

import { ErrorHandler, ErrorModalComponent, SuccessModalComponent } from '@shared/component';
import { LocalizationService, AuthService } from '@shared/service';

declare var acp: string;

@Component( {

    selector: 'data-page',
    templateUrl: './data-page.component.html',
    styleUrls: ['./data-page.css']
} )
export class DataPageComponent implements OnInit {

	content: string = "SPREADSHEET";
	pageTitle: string;
	bsModalRef: BsModalRef;
	isAdmin: boolean;
    isMaintainer: boolean;
    isContributor: boolean;
    
    constructor( private localizationService: LocalizationService, private modalService: BsModalService, private service: AuthService ) {
        this.isAdmin = service.isAdmin();
        this.isMaintainer = this.isAdmin || service.isMaintainer();
		this.isContributor = this.isAdmin || this.isMaintainer || service.isContributer();

		this.isMaintainer ? this.renderContent("SPREADSHEET") : this.renderContent("EXPORT");
	}

    ngOnInit(): void {
	}
	
	renderContent(content: string): void {
		this.content = content;

		if(content === "SPREADSHEET"){
			this.pageTitle = this.localizationService.decode("spreadsheet.title");
		}
		else if(content === "SHAPEFILE"){
			this.pageTitle = this.localizationService.decode("shapefile.title");
		}
		else if(content === "EXPORT"){
			this.pageTitle = this.localizationService.decode("io.export.title");
		}
	}


    public error( err: HttpErrorResponse ): void {
            this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            this.bsModalRef.content.message = ErrorHandler.getMessageFromError(err);
    }

}
