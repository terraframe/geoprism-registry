import { Component, OnInit, Input, ViewChild, ViewChildren, ElementRef, QueryList, ChangeDetectorRef } from "@angular/core";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";
import { FileUploader, FileUploaderOptions } from "ng2-file-upload";
import { HttpErrorResponse } from "@angular/common/http";
import { DateFieldComponent } from "../../../shared/component/form-fields/date-field/date-field.component";

import { ErrorHandler, ErrorModalComponent, SuccessModalComponent } from "@shared/component";
import { LocalizationService, AuthService, EventService, ExternalSystemService } from "@shared/service";
import { HierarchyService, IOService } from "@registry/service";
import { ExternalSystem } from "@shared/model/core";

import { SpreadsheetModalComponent } from "./modals/spreadsheet-modal.component";
import { ShapefileModalComponent } from "./modals/shapefile-modal.component";
import { PRESENT } from "@registry/model/registry";
import { ImportStrategy } from "@registry/model/constants";
import { HierarchyGroupedTypeView } from "@registry/model/hierarchy";

declare var acp: string;

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
     * List of geo object types from the system
     */
    types: {code: string, label: string, orgCode: string, permissions: [string]}[]

    /*
    * GeoObjectTypes grouped by hierarchy
    */
    hierarchyViews: HierarchyGroupedTypeView[];

    importStrategy: ImportStrategy;
    importStrategies: any[] = [
        { strategy: ImportStrategy.NEW_AND_UPDATE, label: this.localizationService.decode("etl.import.ImportStrategy.NEW_AND_UPDATE") },
        { strategy: ImportStrategy.NEW_ONLY, label: this.localizationService.decode("etl.import.ImportStrategy.NEW_ONLY") },
        { strategy: ImportStrategy.UPDATE_ONLY, label: this.localizationService.decode("etl.import.ImportStrategy.UPDATE_ONLY") }
    ]

    /*
     * Code of the currently selected GeoObjectType
     */
    code: string = null;

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

            this.hierarchyViews = views;

        }).catch((err: HttpErrorResponse) => {

            this.error(err);

        });

        let getUrl = acp + "/excel/get-configuration";
        if (this.format === "SHAPEFILE") {

            getUrl = acp + "/shapefile/get-shapefile-configuration";

            // this.showImportConfig = true; // show the upload widget if shapefile because external system from shapefile isn't supported

        }

        let options: FileUploaderOptions = {
            queueLimit: 1,
            removeAfterUpload: true,
            url: getUrl
        };

        this.uploader = new FileUploader(options);

        this.uploader.onBuildItemForm = (fileItem: any, form: any) => {

            form.append("type", this.code);
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

            } else {

                this.bsModalRef = this.modalService.show(SpreadsheetModalComponent, { backdrop: true, ignoreBackdropClick: true });

            }

            this.bsModalRef.content.configuration = configuration;

        };
        this.uploader.onErrorItem = (item: any, response: string, status: number, headers: any) => {

            const error = JSON.parse(response);

            this.error({ error: error });

        };

    }

    onSelectHierarchy(): void {

        let view = null;

        let len = this.hierarchyViews.length;
        for (let i = 0; i < len; ++i) {

            if (this.hierarchyViews[i].code === this.hierarchyCode) {

                view = this.hierarchyViews[i];
                break;

            }

        }

        this.code = null;

        if (view != null) {

            this.types = view.types;

        } else {

            this.types = null;

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
