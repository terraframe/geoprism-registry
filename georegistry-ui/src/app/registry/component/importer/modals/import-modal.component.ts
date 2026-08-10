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

import { Component } from '@angular/core';
import { HttpErrorResponse } from "@angular/common/http";
import { BsModalRef } from 'ngx-bootstrap/modal';
import { BsModalService } from 'ngx-bootstrap/modal';
import { Router } from '@angular/router';

import { LocalizationService } from '@shared/service';
import { ErrorHandler, ConfirmModalComponent } from '@shared/component';

import { EdgeImportConfiguration, ImportConfiguration } from '@registry/model/io';
import { IOService } from '@registry/service';
import { TermProblemPageComponent } from './term-problem-page.component';
import { LocationProblemPageComponent } from './location-problem-page.component';
import { LocationPageComponent } from './location-page.component';
import { AttributesPageComponent } from './attributes-page.component';
import { EdgePageComponent } from './edge-page.component';
import { NgIf } from '@angular/common';
import { SourceAuthorityService } from '@registry/service/source-authority.service';
import { SourceAuthority } from '@registry/model/source';
import { IdMappingPageComponent } from './id-mapping-page.component';

enum Pages {
    EDGE = "EDGE",
    MAP = "MAP",
    IDS = "IDS",
    LOCATION = "LOCATION",
    LOCATION_PROBLEM = "LOCATION-PROBLEM",
    TERM_PROBLEM = "TERM-PROBLEM"
}

@Component({
    selector: 'import-modal',
    templateUrl: './import-modal.component.html',
    styleUrls: [],
    standalone: true,
    imports: [NgIf, EdgePageComponent, AttributesPageComponent, LocationPageComponent, LocationProblemPageComponent, TermProblemPageComponent, IdMappingPageComponent]
})
export class ImportModalComponent {

    configuration: ImportConfiguration;
    message: string = null;
    state: string = Pages.MAP;
    property: string;
    includeChild: boolean;
    authorities: SourceAuthority[] = [];
    pages: string[] = [Pages.MAP];

    hasNext: boolean = true;
    hasBack: boolean = false;

    constructor(
        public bsModalRef: BsModalRef,
        private service: IOService,
        private modalService: BsModalService,
        private localizeService: LocalizationService,
        private authorityService: SourceAuthorityService,
        private router: Router) {
    }

    init(configuration: ImportConfiguration, property: string = 'type', includeChild: boolean = false): void {
        this.configuration = configuration;
        this.property = property;
        this.includeChild = includeChild;

        this.authorityService.getAll().then(authorities => {
            this.authorities = authorities;

            if (this.property === 'EDGE') {
                this.pages = [Pages.EDGE];
            }
            else {
                this.pages = [Pages.MAP];

                if (authorities.length > 0 && this.property === 'type') {
                    this.pages.push(Pages.IDS);
                }

                if (this.configuration.postalCode || this.configuration.hierarchy != null) {
                    this.pages.push(Pages.LOCATION);
                }
            }

            this.state = this.pages[0];
        });
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

    updateButtonFlags(): void {

        const index = this.pages.findIndex(p => p === this.state);

        this.hasBack = (index > 0);
        this.hasNext = (index < (this.pages.length - 1))
    }

    handleBack(): void {

        const index = this.pages.findIndex(p => p === this.state);

        if (index > 0) {
            this.state = this.pages[(index - 1)];
        }

        this.updateButtonFlags();
    }

    handleNext(): void {
        const index = this.pages.findIndex(p => p === this.state);

        if ((index + 1) < this.pages.length) {
            this.state = this.pages[(index + 1)];
        }
        else {
            this.handleSubmit()
        }

        this.updateButtonFlags();
    }

    handleSubmit(): void {
        this.message = null;

        delete (this.configuration as EdgeImportConfiguration).sourceTypes;
        delete (this.configuration as EdgeImportConfiguration).targetTypes;

        this.service.beginImport(this.configuration).then(config => {
            this.pages = [];

            if (config.locationProblems != null) {
                this.pages.push(Pages.LOCATION_PROBLEM);
                this.state = Pages.LOCATION_PROBLEM;
                this.configuration = config;
            }
            else if (config.termProblems != null) {
                this.pages.push(Pages.TERM_PROBLEM);

                this.state = Pages.TERM_PROBLEM;
                this.configuration = config;
            }
            else {
                this.bsModalRef.hide()

                this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
                    animated: false,
                    backdrop: true,
                    ignoreBackdropClick: true,
                });
                this.bsModalRef.content.message = this.localizeService.decode("data.import.go.to.scheduled.jobs.confirm.message");
                this.bsModalRef.content.submitText = this.localizeService.decode("data.import.go.to.scheduled.jobs.button");
                this.bsModalRef.content.cancelText = this.localizeService.decode("modal.button.close");

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
