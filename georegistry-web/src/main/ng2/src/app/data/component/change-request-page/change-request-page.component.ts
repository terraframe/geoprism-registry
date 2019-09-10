import { Component, OnInit, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { FileSelectDirective, FileDropDirective, FileUploader, FileUploaderOptions } from 'ng2-file-upload/ng2-file-upload';

import { SuccessModalComponent } from '../../../shared/component/modals/success-modal.component';
import { ErrorModalComponent } from '../../../shared/component/modals/error-modal.component';
import { AuthService } from '../../../shared/service/auth.service';
import { LocalizationService } from '../../../shared/service/localization.service';

declare var acp: string;

@Component( {

    selector: 'change-request-page',
    templateUrl: './change-request-page.component.html',
    styleUrls: ['./change-request-page.css']
} )
export class ChangeRequestPageComponent implements OnInit {

	content: string = "SUBMIT";
	pageTitle: string;
	bsModalRef: BsModalRef;
	isAdmin: boolean;
    isMaintainer: boolean;
    isContributor: boolean;
    
    constructor( private localizationService: LocalizationService, private modalService: BsModalService, private service: AuthService ) {
        this.isAdmin = service.isAdmin();
        this.isMaintainer = this.isAdmin || service.isMaintainer();
		this.isContributor = this.isAdmin || this.isMaintainer || service.isContributer();

		this.isMaintainer ? this.renderContent("MANAGE") : this.renderContent("SUBMIT");
	}

    ngOnInit(): void {
	}
	
	renderContent(content: string): void {
		this.content = content;

		if(content === "SUBMIT"){
			this.pageTitle = this.localizationService.decode("change.request.page.title");
		}
		else if(content === "MANAGE"){
			this.pageTitle = this.localizationService.decode("change.request.table.title");
		}
	}


    public error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            this.bsModalRef.content.message = ( err.localizedMessage || err.message );
        }
    }

}
