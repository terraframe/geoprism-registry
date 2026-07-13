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

import { Component, OnInit, Input, ViewChild, ElementRef, ChangeDetectorRef, ViewChildren, QueryList } from "@angular/core";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";
import { FileUploader, FileUploaderOptions, FileUploadModule } from "ng2-file-upload";
import { HttpErrorResponse } from "@angular/common/http";

import { ErrorHandler } from "@shared/component";
import { LocalizationService, EventService } from "@shared/service";

import { ImportModalComponent } from "@registry/component/importer/modals/import-modal.component";
import { ImportStrategy } from "@registry/model/constants";
import { ObjectClass } from "@registry/model/object-class";
import { BusinessTypeService } from "@registry/service/business-type.service";
import { ConceptClassService } from "@registry/service/concept-class.service";

import { environment } from 'src/environments/environment';
import { Source } from "@registry/model/source";
import { SourceService } from "@registry/service/source.service";
import { BooleanFieldComponent } from "@shared/component/form-fields/boolean-field/boolean-field.component";
import { DateFieldComponent } from "@shared/component/form-fields/date-field/date-field.component";
import { FormsModule } from "@angular/forms";
import { NgIf, NgFor } from "@angular/common";
import { LocalizeComponent } from "@shared/component/localize/localize.component";
import { PageContainerComponent } from "@shared/component/page-container/page-container.component";
import { ImportConfiguration, ImportConfigurationView } from "@registry/model/io";

@Component({
    selector: "object-importer",
    templateUrl: "./object-importer.component.html",
    styleUrls: ["./object-importer.css"],
    standalone: true,
    imports: [PageContainerComponent, LocalizeComponent, NgIf, FormsModule, NgFor, DateFieldComponent, BooleanFieldComponent, FileUploadModule]
})
export class ObjectImporterComponent implements OnInit {

    @ViewChildren("dateFieldComponents") dateFieldComponentsArray: QueryList<DateFieldComponent>;

    currentDate: Date = new Date();

    showImportConfig: boolean = true;

    isValid: boolean = false;

    /*
    * Types
    */
    types: ObjectClass[] = [];

    importStrategies: any[] = [
        { strategy: ImportStrategy.NEW_AND_UPDATE, label: this.localizationService.decode("etl.import.ImportStrategy.NEW_AND_UPDATE") },
        { strategy: ImportStrategy.NEW_ONLY, label: this.localizationService.decode("etl.import.ImportStrategy.NEW_ONLY") },
        { strategy: ImportStrategy.UPDATE_ONLY, label: this.localizationService.decode("etl.import.ImportStrategy.UPDATE_ONLY") }
    ]

    config: ImportConfigurationView = {
        objectType: "BUSINESS_OBJECT",
        type: null,
        startDate: null,
        endDate: null,
        copyBlank: true,
        dataSource: null,
        description: null,
        strategy: null
    }

    /*
     * File uploader
     */
    uploader: FileUploader;

    @ViewChild("myFile")
    fileRef: ElementRef;

    @Input()
    format: string = "EXCEL";

    /*
     * Hierarchies grouped by GeoObjectType
     */
    sources: Source[];

    dataSource: string;

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private eventService: EventService,
        private modalService: BsModalService,
        private sourceService: SourceService,
        private localizationService: LocalizationService,
        private businessService: BusinessTypeService,
        private conceptService: ConceptClassService,
        private changeDetectorRef: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        this.sourceService.getAll().then(sources =>
            this.sources = sources
        ).catch((err: HttpErrorResponse) => {
            this.error(err);
        });

        let getUrl = environment.apiUrl + "/api/excel/get-import-config";

        let options: FileUploaderOptions = {
            queueLimit: 1,
            removeAfterUpload: true,
            url: getUrl
        };

        this.uploader = new FileUploader(options);

        this.uploader.onBuildItemForm = (fileItem: any, form: any) => {

            form.append("type", this.config.type);
            form.append("copyBlank", this.config.copyBlank);
            form.append("dataSource", this.config.dataSource);
            form.append("objectType", this.config.objectType);
            form.append("description", this.config.description);

            if (this.config.startDate != null) {
                form.append("startDate", this.config.startDate);
            }
            if (this.config.endDate != null) {
                form.append("endDate", this.config.endDate);
            }

            if (this.config.strategy) {
                form.append("strategy", this.config.strategy);
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
            const configuration: ImportConfiguration = JSON.parse(response);


            const bsModalRef = this.modalService.show(ImportModalComponent, {
                animated: false, backdrop: true,
                ignoreBackdropClick: true
            });
            bsModalRef.content.init(configuration, "geoObjectType", true);
        };
        this.uploader.onErrorItem = (item: any, response: string, status: number, headers: any) => {
            const error = JSON.parse(response);

            this.error({ error: error });
        };

        this.handObjectTypeChange();
    }

    onClick(): void {
        if (this.uploader.queue != null && this.uploader.queue.length > 0) {
            this.uploader.uploadAll();
        } else {
            this.error({
                message: this.localizationService.decode("io.missing.file"),
                error: {}
            });
        }
    }

    checkDates(): any {
        setTimeout(() => {
            this.isValid = this.checkDateFieldValidity();
        }, 0);
    }

    checkDateFieldValidity(): boolean {
        let dateFields = this.dateFieldComponentsArray.toArray();

        let startDateField: DateFieldComponent;
        for (let i = 0; i < dateFields.length; i++) {
            let field = dateFields[i];

            if (field.inputName === "startDate") {
                // set startDateField so we can use it in the next check
                startDateField = field;
            }

            if (!field.valid) {
                return false;
            }
        }

        if (this.config.startDate > this.config.endDate) {
            startDateField.setInvalid(this.localizationService.decode("date.input.startdate.after.enddate.error.message"));

            this.changeDetectorRef.detectChanges();
        }

        return true;
    }

    handObjectTypeChange(): void {

        this.types = [];
        this.config.type = null;

        if (this.config.objectType != null && this.config.objectType === "BUSINESS_OBJECT") {
            this.businessService.getAll().then(types => {
                this.types = types;
            });
        }
        else if (this.config.objectType != null && this.config.objectType === "CONCEPT_OBJECT") {
            this.conceptService.getAll().then(types => {
                this.types = types;
            });
        }
    }


    public error(err: any): void {
        ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

}
