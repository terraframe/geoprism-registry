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

import { Component, OnInit, Input, ViewChild, ElementRef } from "@angular/core";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";
import { FileUploader, FileUploaderOptions } from "ng2-file-upload";
import { HttpErrorResponse } from "@angular/common/http";

import { ErrorHandler } from "@shared/component";
import { LocalizationService, EventService } from "@shared/service";
import { HierarchyService } from "@registry/service";

import { SpreadsheetModalComponent } from "@registry/component/importer/modals/spreadsheet-modal.component";
import { ImportStrategy } from "@registry/model/constants";
import { HierarchyGroupedTypeView, TypeGroupedHierachyView } from "@registry/model/hierarchy";
import { GraphTypeService } from "@registry/service/graph-type.service";

import { environment } from 'src/environments/environment';
import { GraphType } from "@registry/model/registry";

@Component({

    selector: "edge-importer",
    templateUrl: "./edge-importer.component.html",
    styleUrls: ["./edge-importer.component.css"]
})
export class EdgeImporterComponent implements OnInit {

    currentDate: Date = new Date();

    showImportConfig: boolean = true;

    isValid: boolean = false;

    /*
    * All available system GraphTypes
    */
    graphTypes: GraphType[] = [];

    /*
     * Code of the currently selected GraphType
     */
    graphTypeCode: string = null;

    allTypes: any[];

    importStrategy: ImportStrategy;
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
     * Date
     */
    date: string = null;

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

    /*
     * currently selected external system.
     */
    externalSystemId: string;

    copyBlank: boolean = true;

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private eventService: EventService,
        private modalService: BsModalService,
        private localizationService: LocalizationService,
        private hierarchyService: HierarchyService,
        private edgeService: GraphTypeService
    ) { }

    ngOnInit(): void {
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
            form.append("type", this.graphTypeCode);
            form.append("copyBlank", this.copyBlank);

            if (this.date != null) {
                form.append("date", this.date);
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

            // TODO : Set other things here?
            configuration.geoObjectType = { code: this.typeCode };

            this.bsModalRef = this.modalService.show(SpreadsheetModalComponent, { backdrop: true, ignoreBackdropClick: true });
            this.bsModalRef.content.init(configuration, "geoObjectType", true);
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

    public error(err: any): void {
        this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

}
