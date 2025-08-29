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

import { Component, OnInit, Input, ViewChild, ElementRef, QueryList, ViewChildren, ChangeDetectorRef } from "@angular/core";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";
import { FileUploader, FileUploaderOptions } from "ng2-file-upload";
import { HttpErrorResponse } from "@angular/common/http";

import { DateFieldComponent, ErrorHandler } from "@shared/component";
import { LocalizationService, EventService } from "@shared/service";
import { HierarchyService } from "@registry/service";

import { ImportModalComponent } from "@registry/component/importer/modals/import-modal.component";
import { ImportStrategy } from "@registry/model/constants";
import { HierarchyGroupedTypeView, TypeGroupedHierachyView } from "@registry/model/hierarchy";
import { GraphTypeService } from "@registry/service/graph-type.service";

import { environment } from 'src/environments/environment';
import { GraphType } from "@registry/model/registry";
import { Source } from "@registry/model/source";
import { SourceService } from "@registry/service/source.service";

@Component({

    selector: "edge-importer",
    templateUrl: "./edge-importer.component.html",
    styleUrls: ["./edge-importer.component.css"]
})
export class EdgeImporterComponent implements OnInit {

    @ViewChildren("dateFieldComponents") dateFieldComponentsArray: QueryList<DateFieldComponent>;

    currentDate: Date = new Date();

    showImportConfig: boolean = true;

    isValid: boolean = false;

    /*
    * All available system GraphTypes
    */
    graphTypes: GraphType[] = [];

    /*
     * The currently selected GraphType
     */
    selectedGraphType: GraphType = null;

    allTypes: any[];

    sources: Source[];
    
    source: Source;

    dataSource: string;

    importStrategy: ImportStrategy;
    importStrategies: any[] = [
        { strategy: ImportStrategy.NEW_AND_UPDATE, label: this.localizationService.decode("etl.import.ImportStrategy.NEW_AND_UPDATE") },
        { strategy: ImportStrategy.NEW_ONLY, label: this.localizationService.decode("etl.import.ImportStrategy.NEW_ONLY") },
        { strategy: ImportStrategy.UPDATE_ONLY, label: this.localizationService.decode("etl.import.ImportStrategy.UPDATE_ONLY") }
    ]

    /*
     * Date
     */
    startDate: string = null;
    endDate: string = null;

    /*
     * Reference to the modal current showing
     */
    bsModalRef: BsModalRef;

    /*
     * File uploader
     */
    uploader: FileUploader;

    @ViewChild("myFile")
    fileRef: ElementRef;

    @Input()
    format: string = "JSON";

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private eventService: EventService,
        private modalService: BsModalService,
        private localizationService: LocalizationService,
        private sourceService: SourceService,
        private hierarchyService: HierarchyService,
        private edgeService: GraphTypeService,
        private changeDetectorRef: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        this.sourceService.getAll().then(sources =>
            this.sources = sources
        ).catch((err: HttpErrorResponse) => {
            this.error(err);
        });

        this.edgeService.get().then(edgeTypes => {
            this.graphTypes = edgeTypes;
        });

        this.hierarchyService.getHierarchyGroupedTypes().then(views => {
            this.allTypes = [];

            let len = views.length;
            for (let i = 0; i < len; ++i) {
                let view = views[i];

                let len2 = view.types.length;
                for (let j = 0; j < len2; ++j) {
                    let type = view.types[j];

                    if (this.allTypes.findIndex(t => t.code == type.code) == -1)
                        this.allTypes.push(type);
                }
            }
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });

        let getUrl = environment.apiUrl + "/api/graph/get-json-import-config";

        let options: FileUploaderOptions = {
            queueLimit: 1,
            removeAfterUpload: true,
            url: getUrl
        };

        this.uploader = new FileUploader(options);

        this.uploader.onBuildItemForm = (fileItem: any, form: any) => {
            form.append("graphTypeCode", this.selectedGraphType.code);
            form.append("graphTypeClass", this.selectedGraphType.typeCode);

            if (this.startDate != null) {
                form.append("startDate", this.startDate);
            }
            if (this.endDate != null) {
                form.append("endDate", this.endDate);
            }
            if (this.importStrategy) {
                form.append("strategy", this.importStrategy);
            }

            if (this.dataSource != null) {
                form.append("dataSource", this.dataSource);
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
            configuration.allTypes = this.allTypes;

            this.bsModalRef = this.modalService.show(ImportModalComponent, { backdrop: true, ignoreBackdropClick: true });
            this.bsModalRef.content.init(configuration, "EDGE");
        };
        this.uploader.onErrorItem = (item: any, response: string, status: number, headers: any) => {
            const error = JSON.parse(response);

            this.error({ error: error });
        };
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

        if (this.startDate > this.endDate) {
            startDateField.setInvalid(this.localizationService.decode("date.input.startdate.after.enddate.error.message"));

            this.changeDetectorRef.detectChanges();
        }

        return true;
    }

    public error(err: any): void {
        this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

}
