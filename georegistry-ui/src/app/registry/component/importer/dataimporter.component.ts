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

import { Component, OnInit, Input, ViewChild, ViewChildren, ElementRef, QueryList, ChangeDetectorRef } from "@angular/core";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";
import { FileUploader, FileUploaderOptions, FileUploadModule } from "ng2-file-upload";
import { HttpErrorResponse } from "@angular/common/http";

import { DateFieldComponent, ErrorHandler } from "@shared/component";
import { LocalizationService, EventService, ExternalSystemService } from "@shared/service";
import { HierarchyService } from "@registry/service";

import { ImportModalComponent } from "./modals/import-modal.component";
import { ImportStrategy } from "@registry/model/constants";
import { GeoObjectTypeImportView, HierarchyGroupedTypeView } from "@registry/model/hierarchy";
import { environment } from "src/environments/environment";
import { DataSource } from "@registry/model/source";
import { DataSourceService } from "@registry/service/data-source.service";
import { BooleanFieldComponent } from "../../../shared/component/form-fields/boolean-field/boolean-field.component";
import { DateFieldComponent as DateFieldComponent_1 } from "../../../shared/component/form-fields/date-field/date-field.component";
import { FormsModule } from "@angular/forms";
import { LocalizeComponent } from "../../../shared/component/localize/localize.component";
import { NgIf, NgClass, NgFor } from "@angular/common";


@Component({
    selector: "dataimporter",
    templateUrl: "./dataimporter.component.html",
    styleUrls: ["./dataimporter.css"],
    standalone: true,
    imports: [NgIf, LocalizeComponent, FormsModule, NgFor, DateFieldComponent_1, BooleanFieldComponent, FileUploadModule]
})
export class DataImporterComponent implements OnInit {

    @ViewChildren("dateFieldComponents") dateFieldComponentsArray: QueryList<DateFieldComponent>;

    currentDate: Date = new Date();

    isValid: boolean = false;

    /*
    * GeoObjectTypes grouped by hierarchy
    */
    hierarchies: HierarchyGroupedTypeView[] = [];

    /*
     * Hierarchies grouped by GeoObjectType
     */
    allTypes: GeoObjectTypeImportView[] = [];

    filteredTypes: GeoObjectTypeImportView[] = [];

    importStrategy: ImportStrategy = ImportStrategy.NEW_ONLY;
    importStrategies: any[] = [
        { strategy: ImportStrategy.NEW_AND_UPDATE, label: this.localizationService.decode("etl.import.ImportStrategy.NEW_AND_UPDATE") },
        { strategy: ImportStrategy.NEW_ONLY, label: this.localizationService.decode("etl.import.ImportStrategy.NEW_ONLY") },
        { strategy: ImportStrategy.UPDATE_ONLY, label: this.localizationService.decode("etl.import.ImportStrategy.UPDATE_ONLY") }
    ]

    /*
     * Code of the currently selected GeoObjectType
     */
    typeCode: string = null;

    /*
     * Code of the currently selected Hierarchy
     */
    hierarchyCode: string = null;

    /*
     * Start date
     */
    startDate: string = null;

    /*
     * End date
     */
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
    format: string; // Can be SHAPEFILE or EXCEL

    isExternal: boolean = false;

    copyBlank: boolean = true;

    /*
     * Hierarchies grouped by GeoObjectType
     */
    sources: DataSource[];

    source: DataSource;

    dataSource: string;

    description: string;



    // eslint-disable-next-line no-useless-constructor
    constructor(
        private eventService: EventService,
        private modalService: BsModalService,
        private localizationService: LocalizationService,
        private sourceService: DataSourceService,
        private hierarchyService: HierarchyService,
        private changeDetectorRef: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        this.sourceService.getAll().then(sources =>
            this.sources = sources
        ).catch((err: HttpErrorResponse) => {
            this.error(err);
        });

        this.hierarchyService.getHierarchyGroupedTypes().then(view => {

            this.hierarchies = view.hierarchies.sort((a, b) => a.label.toLowerCase().localeCompare(b.label.toLowerCase()));
            this.allTypes = view.types.sort((a, b) => a.label.toLowerCase().localeCompare(b.label.toLowerCase()));

            this.filteredTypes = this.allTypes.filter(t => !t.isAbstract);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });

        let getUrl = environment.apiUrl + "/api/excel/get-configuration";
        if (this.format === "SHAPEFILE") {
            getUrl = environment.apiUrl + "/api/shapefile/get-shapefile-configuration";

            // this.showImportConfig = true; // show the upload widget if shapefile because external system from shapefile isn't supported
        }

        let options: FileUploaderOptions = {
            queueLimit: 1,
            removeAfterUpload: true,
            url: getUrl
        };

        this.uploader = new FileUploader(options);

        this.uploader.onBuildItemForm = (fileItem: any, form: any) => {
            form.append("type", this.typeCode);
            form.append("copyBlank", this.copyBlank);

            if (this.dataSource != null) {
                form.append("dataSource", this.dataSource);
            }

            if (this.description != null) {
                form.append("description", this.description);
            }

            if (this.startDate != null) {
                form.append("startDate", this.startDate);
            }
            if (this.endDate != null) {
                form.append("endDate", this.endDate);
            }
            if (this.importStrategy) {
                form.append("strategy", this.importStrategy);
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
            configuration.hierarchy = this.hierarchyCode;


            this.bsModalRef = this.modalService.show(ImportModalComponent, {
                animated: false,
                backdrop: true,
                ignoreBackdropClick: true
            });
            this.bsModalRef.content.init(configuration);
            this.bsModalRef.content.configuration = configuration;

        };
        this.uploader.onErrorItem = (item: any, response: string, status: number, headers: any) => {
            const error = JSON.parse(response);

            this.error({ error: error });
        };
    }

    onSelectHierarchy(): void {
        const view = this.hierarchies.find(h => h.code === this.hierarchyCode);

        if (view != null) {
            this.filteredTypes = this.allTypes.filter(t => !t.isAbstract && view.types.findIndex(tt => tt === t.code) !== -1);
        } else {
            this.filteredTypes = this.allTypes.filter(t => !t.isAbstract);
        }
    }

    onSelectType(): void {
        // let view: TypeGroupedHierachyView = null;

        // let len = this.allTypeViews.length;
        // for (let i = 0; i < len; ++i) {
        //     if (this.allTypeViews[i].code === this.typeCode) {
        //         view = this.allTypeViews[i];
        //         break;
        //     }
        // }

        // if (view != null) {
        //     this.filteredHierarchyViews = view.hierarchies;
        // } else {
        //     this.filteredHierarchyViews = this.allHierarchyViews;
        // }

        this.checkDates();
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

    setImportSource(event, type): void {
        if (type === "EXTERNAL") {
            this.isExternal = true;
        } else {
            this.isExternal = false;
        }
    }

    //    setInfinity(endDate: any): void {
    //
    //        if(endDate === PRESENT){
    //            this.endDate = null;
    //        }
    //        else{
    //            this.endDate = PRESENT;
    //        }
    //    }

    checkDates(): any {
        setTimeout(() => {
            this.isValid = this.checkDateFieldValidity();
        }, 0);
    }

    checkDateFieldValidity(): boolean {
        let dateFields = this.dateFieldComponentsArray.toArray();

        let startDateField: DateFieldComponent = null;
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

        if (startDateField != null && this.startDate > this.endDate) {
            startDateField.setInvalid(this.localizationService.decode("date.input.startdate.after.enddate.error.message"));

            this.changeDetectorRef.detectChanges();
        }

        return true;
    }

    public error(err: any): void {
        this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

}
