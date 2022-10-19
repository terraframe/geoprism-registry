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
import { DndModule } from "ngx-drag-drop";
import { DragDropModule } from "@angular/cdk/drag-drop";
import { NgxSpinnerModule } from "ngx-spinner";

import { ImportTypesModalComponent } from "./component/hierarchy/modals/import-types-modal.component";
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
import { RelationshipVisualizationService } from "./service/relationship-visualization.service";
import { SynchronizationConfigService } from "./service/synchronization-config.service";
import { LocalizationManagerService } from "./service/localization-manager.service";
import { LocationManagerStateService } from "./service/location-manager.service";
import { ChangeRequestService } from "./service/change-request.service";
import { IOService } from "./service/io.service";
import { MapService } from "./service/map.service";
import { FhirImportSynchronizationConfigComponent } from "./component/synchronization-config/fhir-import-synchronization-config.component";
import { TransitionEventService } from "./service/transition-event.service";
import { TransitionEventTableComponent } from "./component/transition-event/transition-event-table.component";
import { TransitionEventModalComponent } from "./component/transition-event/transition-event-modal.component";
import { BusinessTypeManagerComponent } from "./component/business-type/business-type-manager.component";
import { BusinessTableComponent } from "./component/business-table/business-table.component";
import { BusinessTypeService } from "./service/business-type.service";
import { CreateBusinessTypeModalComponent } from "./component/business-type/modals/create-business-type-modal.component";
import { ManageBusinessTypeModalComponent } from "./component/business-type/modals/manage-business-type-modal.component";
import { BusinessImporterComponent } from "./component/business-importer/business-importer.component";
import { HistoricalReportComponent } from "./component/historical-report/historical-report.component";
import { HistoricalEventModuleComponent } from "./component/historical-event-module/historical-event-module.component";

import { RegistryRoutingModule } from "./registry-routing.module";
import { SharedModule } from "../shared/shared.module";

import { AccordionModule } from "ngx-bootstrap/accordion";

import "../rxjs-extensions";

import { NgxGraphModule } from "@swimlane/ngx-graph";
import { ListTypeManagerComponent } from "./component/list-type/list-type-manager.component";
import { ListTypePublishModalComponent } from "./component/list-type/publish-modal.component";
import { ListTypeService } from "./service/list-type.service";
import { ListsForTypeComponent } from "./component/list-type/lists-for-type.component";
import { ListTypeComponent } from "./component/list-type/list-type.component";
import { ListComponent } from "./component/list-type/list.component";
import { PublishVersionComponent } from "./component/list-type/publish-version.component";
import { ExportFormatModalComponent } from "./component/list-type/export-format-modal.component";
import { RecordPanelComponent } from "./component/location-manager/record-panel.component";
import { RecordPopupComponent } from "./component/location-manager/record-popup.component";
import { SelectTypeModalComponent } from "./component/location-manager/select-type-modal.component";
import { CurationJobComponent } from "./component/curation/curation-job.component";
import { CurationProblemModalComponent } from "./component/curation/curation-problem-modal.component";
import { ClassificationTypeManagerComponent } from "./component/classification-type/classification-type-manager.component";
import { ClassificationTypeService } from "./service/classification-type.service";
import { ClassificationTypePublishModalComponent } from "./component/classification-type/classification-type-publish-modal.component";
import { ClassificationTypeComponent } from "./component/classification-type/classification-type.component";
import { ClassificationPublishModalComponent } from "./component/classification-type/classification-publish-modal.component";
import { ClassificationService } from "./service/classification.service";
import { ClassificationFieldComponent } from "./component/form-fields/classification-field/classification-field.component";
import { ClassificationFieldModalComponent } from "./component/form-fields/classification-field/classification-field-modal.component";
import { GeometryService } from "@registry/service/geometry.service";

import { RegistryCacheService } from "./service/registry-cache.service";
import { BusinessObjectPanelComponent } from "./component/location-manager/business-object-panel.component";
import { BusinessObjectService } from "./service/business-object.service";

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
        NgxGraphModule,
        DragDropModule,
        AccordionModule.forRoot(),
        NgxSpinnerModule
    ],
    declarations: [
        HierarchyComponent,
        RelationshipVisualizerComponent,
        RequestTableComponent,
        CreateUpdateGeoObjectDetailComponent,
        ImportTypesModalComponent,
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
        LayerPanelComponent,
        FeaturePanelComponent,
        GeometryPanelComponent,
        TransitionEventTableComponent,
        TransitionEventModalComponent,
        RecordPanelComponent,
        RecordPopupComponent,
        BusinessObjectPanelComponent,
        // Business Type components
        BusinessTypeManagerComponent,
        CreateBusinessTypeModalComponent,
        ManageBusinessTypeModalComponent,
        BusinessImporterComponent,
        BusinessTableComponent,
        // Historical report components
        HistoricalReportComponent,
        HistoricalEventModuleComponent,
        SelectTypeModalComponent,
        // List type
        ListTypeManagerComponent,
        ListTypePublishModalComponent,
        ListsForTypeComponent,
        ListTypeComponent,
        ListComponent,
        PublishVersionComponent,
        // Curation
        CurationJobComponent,
        CurationProblemModalComponent,
        // Classification
        ClassificationTypeManagerComponent,
        ClassificationTypePublishModalComponent,
        ClassificationTypeComponent,
        ClassificationPublishModalComponent,
        ClassificationFieldComponent,
        ClassificationFieldModalComponent
    ],
    providers: [
        MapService,
        HierarchyService,
        RelationshipVisualizationService,
        LocalizationManagerService,
        LocationManagerStateService,
        ChangeRequestService,
        IOService,
        RegistryService,
        RegistryCacheService,
        TaskService,
        DatePipe,
        ToEpochDateTimePipe,
        StepIndicatorComponent,
        SynchronizationConfigService,
        TransitionEventService,
        BusinessTypeService,
        BusinessObjectService,
        ListTypeService,
        ClassificationTypeService,
        ClassificationService,
        GeometryService
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
        ExportFormatModalComponent,
        DataPageComponent,
        ChangeRequestPageComponent,
        ActionDetailModalComponent,
        JobConflictModalComponent,
        ReuploadModalComponent,
        StepIndicatorComponent,
        SynchronizationConfigModalComponent,
        CreateBusinessTypeModalComponent,
        ManageBusinessTypeModalComponent,
        TransitionEventModalComponent,
        ListTypePublishModalComponent,
        PublishVersionComponent,
        SelectTypeModalComponent,
        CurationProblemModalComponent,
        ClassificationTypePublishModalComponent,
        ClassificationPublishModalComponent,
        ClassificationFieldModalComponent,
        RecordPopupComponent
    ]
})
export class RegistryModule { }
