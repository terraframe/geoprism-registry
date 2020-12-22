import { Component, OnInit } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { HttpErrorResponse } from "@angular/common/http";

import { LocalizationService, AuthService } from '@shared/service';

import { ErrorHandler, ErrorModalComponent } from '@shared/component';

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


    public error( err: HttpErrorResponse ): void {
            this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

}
