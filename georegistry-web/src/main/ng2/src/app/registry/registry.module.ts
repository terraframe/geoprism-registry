import { NgModule } from "@angular/core";
import { CommonModule, DatePipe } from "@angular/common";
import { RouterModule } from "@angular/router";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { TreeModule } from "@circlon/angular-tree-component";
import { ContextMenuModule } from "ngx-contextmenu";
import { BsDropdownModule } from "ngx-bootstrap/dropdown";
import { ButtonsModule } from "ngx-bootstrap/buttons";
import { TypeaheadModule } from "ngx-bootstrap/typeahead";
import { FileUploadModule } from "ng2-file-upload";
import { NgxPaginationModule } from "ngx-pagination";
import { ProgressbarModule } from "ngx-bootstrap/progressbar";
import { CollapseModule } from "ngx-bootstrap/collapse";
import { TabsModule } from "ngx-bootstrap/tabs";
import { ImportTypesModalComponent } from "./component/hierarchy/modals/import-types-modal.component";
import { ExportSystemModalComponent } from "./component/master-list/export-system-modal.component";
import { LocalizedInputComponent } from "./component/form-fields/localized-input/localized-input.component";
import { LocalizedTextComponent } from "./component/form-fields/localized-text/localized-text.component";
import { HierarchyComponent } from "./component/hierarchy/hierarchy.component";
import { RelationshipVisualizerComponent } from "./component/relationship-visualizer/relationship-visualizer.component";
import { CreateHierarchyTypeModalComponent } from "./component/hierarchy/modals/create-hierarchy-type-modal.component";
import { AddChildToHierarchyModalComponent } from "./component/hierarchy/modals/add-child-to-hierarchy-modal.component";
import { CreateGeoObjTypeModalComponent } from "./component/hierarchy/modals/create-geoobjtype-modal.component";
import { ManageAttributesModalComponent } from "./component/hierarchy/geoobjecttype-management/manage-attributes-modal.component";
import { DefineAttributeModalContentComponent } from "./component/hierarchy/geoobjecttype-management/define-attribute-modal-content.component";
import { EditAttributeModalContentComponent } from "./component/hierarchy/geoobjecttype-management/edit-attribute-modal-content.component";
import { ShapefileModalComponent } from "./component/importer/modals/shapefile-modal.component";
import { AttributesPageComponent } from "./component/importer/modals/attributes-page.component";
import { LocationPageComponent } from "./component/importer/modals/location-page.component";
import { LocationProblemPageComponent } from "./component/importer/modals/location-problem-page.component";
import { LocationProblemComponent } from "./component/importer/modals/location-problem.component";
import { TermProblemPageComponent } from "./component/importer/modals/term-problem-page.component";
import { TermProblemComponent } from "./component/importer/modals/term-problem.component";
import { SpreadsheetModalComponent } from "./component/importer/modals/spreadsheet-modal.component";
import { DataPageComponent } from "./component/data-page/data-page.component";
import { TermOptionWidgetComponent } from "./component/hierarchy/geoobjecttype-management/term-option-widget.component";
import { AttributeInputComponent } from "./component/hierarchy/geoobjecttype-management/attribute-input.component";
import { EditTermOptionInputComponent } from "./component/hierarchy/geoobjecttype-management/edit-term-option-input.component";
import { ManageTermOptionsComponent } from "./component/hierarchy/geoobjecttype-management/manage-term-options.component";
import { GeoObjectTypeInputComponent } from "./component/hierarchy/geoobjecttype-management/geoobjecttype-input.component";
import { ManageGeoObjectTypeModalComponent } from "./component/hierarchy/modals/manage-geoobjecttype-modal.component";
import { MasterListManagerComponent } from "./component/master-list/master-list-manager.component";
import { PublishModalComponent } from "./component/master-list/publish-modal.component";
import { ExportFormatModalComponent } from "./component/master-list/export-format-modal.component";
import { RequestTableComponent } from "./component/crtable/request-table.component";
import { CreateUpdateGeoObjectDetailComponent } from "./component/crtable/action-detail/create-update-geo-object/detail.component";
import { GeoObjectSharedAttributeEditorComponent } from "./component/geoobject-shared-attribute-editor/geoobject-shared-attribute-editor.component";
import { StabilityPeriodComponent } from "./component/geoobject-shared-attribute-editor/stability-period.component";
import { ManageVersionsComponent } from "./component/geoobject-shared-attribute-editor/manage-versions.component";
import { StandardAttributeEditorComponent } from "./component/geoobject-shared-attribute-editor/standard-attribute-editor.component";
import { SubmitChangeRequestComponent } from "./component/submit-change-request/submit-change-request.component";
import { ChangeRequestPageComponent } from "./component/change-request-page/change-request-page.component";
import { GeoObjectEditorComponent } from "./component/geoobject-editor/geoobject-editor.component";
import { GeoObjectEditorMapComponent } from "./component/geoobject-editor-map/geoobject-editor-map.component";
import { SimpleEditControl } from "./component/geoobject-editor-map/simple-edit-control/simple-edit-control.component";
import { CascadingGeoSelector } from "./component/cascading-geo-selector/cascading-geo-selector";
import { TreeGeoSelector } from "./component/tree-geo-selector/tree-geo-selector";
import { ActionDetailModalComponent } from "./component/crtable/action-detail/action-detail-modal.component";
import { DataImporterComponent } from "./component/importer/dataimporter.component";
import { DataExportComponent } from "./component/data-export/data-export.component";
import { MasterListComponent } from "./component/master-list/master-list.component";
import { PublishedMasterListHistoryComponent } from "./component/master-list/published-master-list-history.component";
import { MasterListHistoryComponent } from "./component/master-list/master-list-history.component";
import { MasterListViewComponent } from "./component/master-list/master-list-view.component";
import { ScheduledJobsComponent } from "./component/scheduled-jobs/scheduled-jobs.component";
import { JobComponent } from "./component/scheduled-jobs/job.component";
import { JobConflictModalComponent } from "./component/scheduled-jobs/conflict-widgets/job-conflict-modal.component";
import { ReuploadModalComponent } from "./component/scheduled-jobs/conflict-widgets/reupload-modal.component";
import { ParentReferenceProblemWidgetComponent } from "./component/scheduled-jobs/conflict-widgets/parent-reference-problem-widget.component";
import { TermReferenceProblemWidgetComponent } from "./component/scheduled-jobs/conflict-widgets/term-reference-problem-widget.component";
import { RowValidationProblemWidgetComponent } from "./component/scheduled-jobs/conflict-widgets/row-validation-problem-widget.component";
import { StepIndicatorComponent } from "./component/scheduled-jobs/step-indicator.component";
import { ImportProblemWidgetComponent } from "./component/scheduled-jobs/conflict-widgets/import-problem-widget.component";
import { TaskViewerComponent } from "./component/task-viewer/task-viewer.component";
import { FhirExportSynchronizationConfigComponent } from "./component/synchronization-config/fhir-export-synchronization-config.component";
import { Dhis2SynchronizationConfigComponent } from "./component/synchronization-config/dhis2-synchronization-config.component";
import { SynchronizationConfigManagerComponent } from "./component/synchronization-config/synchronization-config-manager.component";
import { SynchronizationConfigModalComponent } from "./component/synchronization-config/synchronization-config-modal.component";
import { SynchronizationConfigComponent } from "./component/synchronization-config/synchronization-config.component";
import { SyncDetailsComponent } from "./component/synchronization-config/details.component";

import { LocationManagerComponent } from "./component/location-manager/location-manager.component";
import { DatasetLocationManagerComponent } from "./component/location-manager/dataset-location-manager.component";
import { ContextLayerModalComponent } from "./component/location-manager/context-layer-modal.component";
import { LayerPanelComponent } from "./component/location-manager/layer-panel.component";
import { FeaturePanelComponent } from "./component/location-manager/feature-panel.component";
import { GeometryPanelComponent } from "./component/location-manager/geometry-panel.component";

import { GeoObjectAttributeCodeValidator } from "./factory/form-validation.factory";

import { GeoObjectTypePipe } from "./pipe/geoobjecttype.pipe";
import { GeoObjectAttributeExcludesPipe } from "./pipe/geoobject-attribute-excludes.pipe";
import { ToEpochDateTimePipe } from "./pipe/to-epoch-date-time.pipe";
import { RegistryService } from "./service/registry.service";
import { TaskService } from "./service/task.service";
import { HierarchyService } from "./service/hierarchy.service";
import { SynchronizationConfigService } from "./service/synchronization-config.service";
import { LocalizationManagerService } from "./service/localization-manager.service";
import { ChangeRequestService } from "./service/change-request.service";
import { IOService } from "./service/io.service";
import { MapService } from "./service/map.service";
import { GeoObjectTypeManagementService } from "./service/geoobjecttype-management.service";

import { RegistryRoutingModule } from "./registry-routing.module";
import { SharedModule } from "../shared/shared.module";

import { AccordionModule } from "ngx-bootstrap/accordion";

import "../rxjs-extensions";
import { FhirImportSynchronizationConfigComponent } from "./component/synchronization-config/fhir-import-synchronization-config.component";
import { TransitionEventService } from "./service/transition-event.service";
import { TransitionEventTableComponent } from "./component/transition-event/transition-event-table.component";
import { TransitionEventModalComponent } from "./component/transition-event/transition-event-modal.component";
import { HistoricalReportComponent } from "./component/historical-report/historical-report.component";
import { HistoricalEventModuleComponent } from "./component/historical-event-module/historical-event-module.component";

import { DndModule } from "ngx-drag-drop";

@NgModule({
    imports: [
        CommonModule,
        RouterModule,
        FormsModule,
        ReactiveFormsModule,
        FileUploadModule,
        TreeModule,
        ContextMenuModule,
        BsDropdownModule,
        ButtonsModule,
        TypeaheadModule,
        ProgressbarModule,
        TabsModule,
        CollapseModule,
        NgxPaginationModule,
        SharedModule,
        RegistryRoutingModule,
        DndModule,
        AccordionModule.forRoot()
    ],
    declarations: [
        HierarchyComponent,
        RelationshipVisualizerComponent,
        RequestTableComponent,
        CreateUpdateGeoObjectDetailComponent,
        ImportTypesModalComponent,
        ExportSystemModalComponent,
        FhirExportSynchronizationConfigComponent,
        FhirImportSynchronizationConfigComponent,
        Dhis2SynchronizationConfigComponent,
        CreateHierarchyTypeModalComponent,
        AddChildToHierarchyModalComponent,
        CreateGeoObjTypeModalComponent,
        ManageAttributesModalComponent,
        DefineAttributeModalContentComponent,
        ShapefileModalComponent,
        AttributesPageComponent,
        LocationPageComponent,
        LocationProblemPageComponent,
        LocationProblemComponent,
        TermProblemPageComponent,
        TermProblemComponent,
        SpreadsheetModalComponent,
        GeoObjectTypePipe,
        GeoObjectAttributeCodeValidator,
        EditAttributeModalContentComponent,
        TermOptionWidgetComponent,
        AttributeInputComponent,
        EditTermOptionInputComponent,
        ManageGeoObjectTypeModalComponent,
        GeoObjectTypeInputComponent,
        ManageTermOptionsComponent,
        LocalizedInputComponent,
        LocalizedTextComponent,
        GeoObjectSharedAttributeEditorComponent,
        StabilityPeriodComponent,
        ManageVersionsComponent,
        StandardAttributeEditorComponent,
        SubmitChangeRequestComponent,
        GeoObjectEditorComponent,
        GeoObjectAttributeExcludesPipe,
        ToEpochDateTimePipe,
        GeoObjectEditorMapComponent,
        SimpleEditControl,
        DataPageComponent,
        ChangeRequestPageComponent,
        CascadingGeoSelector,
        TreeGeoSelector,
        ActionDetailModalComponent,
        DataImporterComponent,
        DataExportComponent,
        // Master List screens
        MasterListManagerComponent,
        MasterListComponent,
        MasterListHistoryComponent,
        PublishedMasterListHistoryComponent,
        PublishModalComponent,
        MasterListViewComponent,
        // Scheduled jobs
        ExportFormatModalComponent,
        ScheduledJobsComponent,
        JobComponent,
        JobConflictModalComponent,
        ReuploadModalComponent,
        ParentReferenceProblemWidgetComponent,
        TermReferenceProblemWidgetComponent,
        RowValidationProblemWidgetComponent,
        StepIndicatorComponent,
        ImportProblemWidgetComponent,
        TaskViewerComponent,
        // Synchronization Config
        SynchronizationConfigManagerComponent,
        SynchronizationConfigModalComponent,
        SynchronizationConfigComponent,
        SyncDetailsComponent,
        // Location manager
        LocationManagerComponent,
        DatasetLocationManagerComponent,
        ContextLayerModalComponent,
        LayerPanelComponent,
        FeaturePanelComponent,
        GeometryPanelComponent,
        TransitionEventTableComponent,
        TransitionEventModalComponent,
        HistoricalReportComponent,
        HistoricalEventModuleComponent
    ],
    providers: [
        MapService,
        HierarchyService,
        LocalizationManagerService,
        ChangeRequestService,
        IOService,
        GeoObjectTypeManagementService,
        RegistryService,
        TaskService,
        DatePipe,
        ToEpochDateTimePipe,
        StepIndicatorComponent,
        SynchronizationConfigService,
        TransitionEventService
    ],
    entryComponents: [
        AddChildToHierarchyModalComponent,
        CreateGeoObjTypeModalComponent,
        ManageAttributesModalComponent,
        DefineAttributeModalContentComponent,
        EditAttributeModalContentComponent,
        CreateHierarchyTypeModalComponent,
        ShapefileModalComponent,
        SpreadsheetModalComponent,
        TermOptionWidgetComponent,
        AttributeInputComponent,
        EditTermOptionInputComponent,
        ManageGeoObjectTypeModalComponent,
        GeoObjectTypeInputComponent,
        ManageTermOptionsComponent,
        GeoObjectSharedAttributeEditorComponent,
        SubmitChangeRequestComponent,
        GeoObjectEditorComponent,
        PublishModalComponent,
        ExportFormatModalComponent,
        DataPageComponent,
        ChangeRequestPageComponent,
        ActionDetailModalComponent,
        JobConflictModalComponent,
        ReuploadModalComponent,
        StepIndicatorComponent,
        SynchronizationConfigModalComponent,
        ContextLayerModalComponent,
        ExportSystemModalComponent,
        TransitionEventModalComponent
    ]
})
export class RegistryModule { }
