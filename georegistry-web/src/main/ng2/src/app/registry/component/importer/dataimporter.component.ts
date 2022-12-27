import { Component, OnInit, Input, ViewChild, ViewChildren, ElementRef, QueryList, ChangeDetectorRef } from "@angular/core";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";
import { FileUploader, FileUploaderOptions } from "ng2-file-upload";
import { HttpErrorResponse } from "@angular/common/http";

import { DateFieldComponent, ErrorHandler } from "@shared/component";
import { LocalizationService, AuthService, EventService, ExternalSystemService } from "@shared/service";
import { HierarchyService, IOService } from "@registry/service";
import { ExternalSystem } from "@shared/model/core";

import { SpreadsheetModalComponent } from "./modals/spreadsheet-modal.component";
import { ShapefileModalComponent } from "./modals/shapefile-modal.component";
import { ImportStrategy } from "@registry/model/constants";
import { HierarchyGroupedTypeView, TypeGroupedHierachyView } from "@registry/model/hierarchy";

import { GeoRegistryConfiguration } from "@core/model/core"; import { environment } from 'src/environments/environment';

@Component({

    selector: "dataimporter",
    templateUrl: "./dataimporter.component.html",
    styleUrls: ["./dataimporter.css"]
})
export class DataImporterComponent implements OnInit {

    @ViewChildren("dateFieldComponents") dateFieldComponentsArray:QueryList<DateFieldComponent>;

    currentDate : Date = new Date();

    showImportConfig: boolean = false;

    isValid: boolean = false;

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
    startDate: Date = null;

    /*
     * End date
     */
    endDate: Date | string = null;

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

    /*
     * List of available external systems (filtered based on user's org)
     */
    externalSystems: ExternalSystem[];

    /*
     * currently selected external system.
     */
    externalSystemId: string;

    isLoading: boolean = true;

    copyBlank: boolean = true;

    // eslint-disable-next-line no-useless-constructor
    constructor(private service: IOService,
        private eventService: EventService,
        private modalService: BsModalService,
        private localizationService: LocalizationService,
        private authService: AuthService,
        private sysService: ExternalSystemService,
        private hierarchyService: HierarchyService,
        private changeDetectorRef: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        this.sysService.getExternalSystems(1, 100).then(paginatedSystems => {
            this.externalSystems = paginatedSystems.resultSet;

            if (this.externalSystems.length === 0) {
                this.isExternal = false;
                this.showImportConfig = true; // Show the upload widget if there are no external systems registered
            }

            this.isLoading = false;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
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

            let externalSystem: ExternalSystem = null;
            for (let i = 0; i < this.externalSystems.length; ++i) {
                let sys: ExternalSystem = this.externalSystems[i];

                if (sys.oid === this.externalSystemId) {
                    externalSystem = sys;
                }
            }

            configuration.externalSystemId = this.externalSystemId;
            configuration.externalSystem = externalSystem;

            if (this.format === "SHAPEFILE") {
                this.bsModalRef = this.modalService.show(ShapefileModalComponent, { backdrop: true, ignoreBackdropClick: true });
                this.bsModalRef.content.configuration = configuration;
            } else {
                this.bsModalRef = this.modalService.show(SpreadsheetModalComponent, { backdrop: true, ignoreBackdropClick: true });
                this.bsModalRef.content.init(configuration);
            }

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

    onNext(): void {
        this.showImportConfig = true;
    }

    onBack(): void {
        this.showImportConfig = false;
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
