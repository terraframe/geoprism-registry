import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { HttpErrorResponse } from "@angular/common/http";
import { BsModalRef } from 'ngx-bootstrap/modal';
import { BsModalService } from 'ngx-bootstrap/modal';
import { Router } from '@angular/router';

import { LocalizationService } from '@shared/service';
import { ErrorHandler, SuccessModalComponent, ConfirmModalComponent } from '@shared/component';

import { ImportConfiguration } from '@registry/model/io';
import { IOService } from '@registry/service';

@Component({
    selector: 'spreadsheet-modal',
    templateUrl: './spreadsheet-modal.component.html',
    styleUrls: []
})
export class SpreadsheetModalComponent {

    configuration: ImportConfiguration;
    message: string = null;
    state: string = 'MAP';
    property: string;
    includeChild: boolean;

    constructor(private service: IOService, public bsModalRef: BsModalRef, private modalService: BsModalService,
        private localizeService: LocalizationService, private router: Router) {
    }

    init(configuration: ImportConfiguration, property: string = 'type', includeChild: boolean = false): void {
        this.configuration = configuration;
        this.property = property;
        this.includeChild = includeChild;
    }

    onStateChange(event: string): void {
        if (event === 'BACK') {
            this.handleBack();
        }
        else if (event === 'NEXT') {
            this.handleNext();
        }
        else if (event === 'CANCEL') {
            this.handleCancel();
        }
    }

    handleBack(): void {
        if (this.state === 'LOCATION') {
            this.state = 'MAP';
        }
    }

    handleNext(): void {
        if (this.state === 'MAP') {
            if (!this.configuration.postalCode) {
                this.state = 'LOCATION';
            }
            else {
                this.handleSubmit();
            }
        }
        else if (this.state === 'LOCATION') {
            this.handleSubmit();
        }
        else if (this.state === 'LOCATION-PROBLEM') {

            if (this.configuration.termProblems != null) {
                this.state = 'TERM-PROBLEM';
            }
            else {
                this.handleSubmit();
            }
        }
        else if (this.state === 'TERM-PROBLEM') {
            this.handleSubmit();
        }
    }

    handleSubmit(): void {
        this.service.importSpreadsheet(this.configuration).then(config => {

            if (config.locationProblems != null) {
                this.state = 'LOCATION-PROBLEM';
                this.configuration = config;
            }
            else if (config.termProblems != null) {
                this.state = 'TERM-PROBLEM';
                this.configuration = config;
            }
            else {
                this.bsModalRef.hide()

                this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
                    animated: true,
                    backdrop: true,
                    ignoreBackdropClick: true,
                });
                this.bsModalRef.content.message = this.localizeService.decode("data.import.go.to.scheduled.jobs.confirm.message");
                this.bsModalRef.content.submitText = this.localizeService.decode("data.import.go.to.scheduled.jobs.button");

                (<ConfirmModalComponent>this.bsModalRef.content).onConfirm.subscribe(data => {
                    this.router.navigate(['/registry/scheduled-jobs']);
                });

            }
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });

    }

    handleCancel(): void {
        this.service.cancelImport(this.configuration).then(response => {
            this.bsModalRef.hide()
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
