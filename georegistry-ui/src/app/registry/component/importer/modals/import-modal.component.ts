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
    selector: 'import-modal',
    templateUrl: './import-modal.component.html',
    styleUrls: []
})
export class ImportModalComponent {

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

        if (this.property === 'EDGE') {
            this.state = 'EDGE';
        }
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
        } else if (this.state === 'EDGE') {
            this.handleSubmit();
        }
    }

    handleSubmit(): void {
        this.message = null;
        delete (this.configuration as any).allTypes;

        this.service.beginImport(this.configuration).then(config => {

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
                this.bsModalRef.content.cancelText = this.localizeService.decode( "modal.button.close" );

                (<ConfirmModalComponent>this.bsModalRef.content).onConfirm.subscribe(data => {
                    this.router.navigate(['/registry/scheduled-jobs']);
                });

            }
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });

    }

    handleCancel(): void {
        this.message = null;

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
