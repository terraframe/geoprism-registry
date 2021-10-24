import { Component, OnInit, Input, ViewChild, ViewChildren, ElementRef, QueryList, ChangeDetectorRef } from "@angular/core";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";
import { FileUploader, FileUploaderOptions } from "ng2-file-upload";
import { HttpErrorResponse } from "@angular/common/http";

import { ErrorHandler } from "@shared/component";
import { LocalizationService, EventService } from "@shared/service";
import { HierarchyService } from "@registry/service";

import { SpreadsheetModalComponent } from "@registry/component/importer/modals/spreadsheet-modal.component";
import { ImportStrategy } from "@registry/model/constants";
import { HierarchyGroupedTypeView, TypeGroupedHierachyView } from "@registry/model/hierarchy";
import { BusinessType } from "@registry/model/business-type";
import { BusinessTypeService } from "@registry/service/business-type.service";

declare let acp: string;

@Component({

    selector: "business-importer",
    templateUrl: "./business-importer.component.html",
    styleUrls: ["./business-importer.css"]
})
export class BusinessImporterComponent implements OnInit {

    currentDate: Date = new Date();

    showImportConfig: boolean = false;

    isValid: boolean = false;

    /*
    * GeoObjectTypes grouped by hierarchy
    */
    businessTypes: BusinessType[] = [];

    /*
     * Code of the currently selected GeoObjectType
     */
    businessTypeCode: string = null;

    /*
    * GeoObjectTypes grouped by hierarchy
    */
    allHierarchyViews: HierarchyGroupedTypeView[];

    filteredHierarchyViews: any[];

    /*
     * Hierarchies grouped by GeoObjectType
     */
    allTypeViews: TypeGroupedHierachyView[];

    filteredTypeViews: any[];

    importStrategy: ImportStrategy;
    importStrategies: any[] = [
        { strategy: ImportStrategy.NEW_ONLY, label: this.localizationService.decode("etl.import.ImportStrategy.NEW_ONLY") },
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
     * Date
     */
    date: Date = null;

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
    format: string = "EXCEL";

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
        private businessService: BusinessTypeService
    ) { }

    ngOnInit(): void {

        this.businessService.getAll().then(businessTypes => {
            this.businessTypes = businessTypes;
        });

        this.hierarchyService.getHierarchyGroupedTypes().then(views => {
            this.allHierarchyViews = views;
            this.allTypeViews = [];

            // Make sure we are using the same object references for all types
            let len0 = this.allHierarchyViews.length;
            for (let i = 0; i < len0; ++i) {
                let view = this.allHierarchyViews[i];

                let len2 = view.types.length;
                for (let j = 0; j < len2; ++j) {
                    let type = view.types[j];

                    let len9 = this.allHierarchyViews.length;
                    for (let j = 0; j < len9; ++j) {
                        let view2 = this.allHierarchyViews[j];

                        let indexOf = view2.types.findIndex(findType => type.code === findType.code);

                        if (indexOf !== -1) {
                            view2.types[indexOf] = type;
                        }
                    }
                }
            }

            // Generate a TypeGroupedHierarchy lookup structure from the HierarchyGroupedType structure
            let len = this.allHierarchyViews.length;
            for (let i = 0; i < len; ++i) {
                let view = this.allHierarchyViews[i];

                let len2 = view.types.length;
                for (let j = 0; j < len2; ++j) {
                    let type = view.types[j];

                    let indexOf = this.allTypeViews.findIndex(findType => findType.code === type.code);

                    if (indexOf !== -1) {
                        let findType = this.allTypeViews[indexOf];

                        let existingHierarchyIndex = findType.hierarchies.findIndex(findHier => findHier.code === view.code);

                        if (existingHierarchyIndex === -1) {
                            findType.hierarchies.push(view);
                        }
                    } else {
                        if (type.hierarchies == null) {
                            type.hierarchies = [];
                        }
                        type.hierarchies.push(view);
                        this.allTypeViews.push(type);
                    }
                }
            }

            this.filteredHierarchyViews = this.allHierarchyViews;
            this.filteredTypeViews = this.allTypeViews;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });

        let getUrl = acp + "/excel/get-business-config";

        let options: FileUploaderOptions = {
            queueLimit: 1,
            removeAfterUpload: true,
            url: getUrl
        };

        this.uploader = new FileUploader(options);

        this.uploader.onBuildItemForm = (fileItem: any, form: any) => {
            form.append("type", this.businessTypeCode);
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

            configuration.hierarchy = this.hierarchyCode;
            configuration.geoObjectType = {code: this.typeCode};

            this.bsModalRef = this.modalService.show(SpreadsheetModalComponent, { backdrop: true, ignoreBackdropClick: true });
            this.bsModalRef.content.init(configuration, 'geoObjectType', true);
        };
        this.uploader.onErrorItem = (item: any, response: string, status: number, headers: any) => {
            const error = JSON.parse(response);

            this.error({ error: error });
        };
    }

    onSelectHierarchy(): void {
        let view: HierarchyGroupedTypeView = null;

        let len = this.allHierarchyViews.length;
        for (let i = 0; i < len; ++i) {
            if (this.allHierarchyViews[i].code === this.hierarchyCode) {
                view = this.allHierarchyViews[i];
                break;
            }
        }

        if (view != null) {
            this.filteredTypeViews = view.types;
        } else {
            this.filteredTypeViews = this.allTypeViews;
        }
    }

    onSelectType(): void {
        let view: TypeGroupedHierachyView = null;

        let len = this.allTypeViews.length;
        for (let i = 0; i < len; ++i) {
            if (this.allTypeViews[i].code === this.typeCode) {
                view = this.allTypeViews[i];
                break;
            }
        }

        if (view != null) {
            this.filteredHierarchyViews = view.hierarchies;
        } else {
            this.filteredHierarchyViews = this.allHierarchyViews;
        }
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

    onNext(): void {
        this.showImportConfig = true;
    }

    onBack(): void {
        this.showImportConfig = false;
    }


    public error(err: any): void {
        this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

}
