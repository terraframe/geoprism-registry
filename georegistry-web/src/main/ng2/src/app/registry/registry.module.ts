import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TreeModule } from 'angular-tree-component';
import { ContextMenuModule } from 'ngx-contextmenu';
import { ModalModule } from 'ngx-bootstrap/modal';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { ButtonsModule } from 'ngx-bootstrap/buttons';
import { TypeaheadModule } from 'ngx-bootstrap/typeahead';
import { CookieService } from 'ngx-cookie-service';
import { FileUploadModule } from 'ng2-file-upload/ng2-file-upload';
import { NgxPaginationModule } from 'ngx-pagination';
import { ProgressbarModule } from 'ngx-bootstrap/progressbar';
import { CollapseModule } from 'ngx-bootstrap/collapse';
import { DatePipe } from '@angular/common';
import { CustomFormsModule } from 'ng2-validation'

import { LocalizedInputComponent } from './component/form-fields/localized-input/localized-input.component';
import { LocalizedTextComponent } from './component/form-fields/localized-text/localized-text.component';
import { HierarchyComponent } from './component/hierarchy/hierarchy.component';
import { LocalizationManagerComponent } from './component/localization-manager/localization-manager.component';
import { CreateHierarchyTypeModalComponent } from './component/hierarchy/modals/create-hierarchy-type-modal.component';
import { AddChildToHierarchyModalComponent } from './component/hierarchy/modals/add-child-to-hierarchy-modal.component';
import { CreateGeoObjTypeModalComponent } from './component/hierarchy/modals/create-geoobjtype-modal.component';
import { ManageAttributesModalComponent } from './component/hierarchy/geoobjecttype-management/manage-attributes-modal.component';
import { DefineAttributeModalContentComponent } from './component/hierarchy/geoobjecttype-management/define-attribute-modal-content.component';
import { EditAttributeModalContentComponent } from './component/hierarchy/geoobjecttype-management/edit-attribute-modal-content.component';
import { ShapefileModalComponent } from './component/importer/modals/shapefile-modal.component';
import { AttributesPageComponent } from './component/importer/modals/attributes-page.component';
import { LocationPageComponent } from './component/importer/modals/location-page.component';
import { LocationProblemPageComponent } from './component/importer/modals/location-problem-page.component';
import { LocationProblemComponent } from './component/importer/modals/location-problem.component';
import { TermProblemPageComponent } from './component/importer/modals/term-problem-page.component';
import { TermProblemComponent } from './component/importer/modals/term-problem.component';
import { SpreadsheetModalComponent } from './component/importer/modals/spreadsheet-modal.component';
import { DataPageComponent } from './component/data-page/data-page.component';
import { NewLocaleModalComponent } from './component/localization-manager/new-locale-modal.component';
import { TermOptionWidgetComponent } from './component/hierarchy/geoobjecttype-management/term-option-widget.component';
import { AttributeInputComponent } from './component/hierarchy/geoobjecttype-management/attribute-input.component';
import { EditTermOptionInputComponent } from './component/hierarchy/geoobjecttype-management/edit-term-option-input.component';
import { ManageTermOptionsComponent } from './component/hierarchy/geoobjecttype-management/manage-term-options.component';
import { GeoObjectTypeInputComponent } from './component/hierarchy/geoobjecttype-management/geoobjecttype-input.component';
import { ManageGeoObjectTypeModalComponent } from './component/hierarchy/modals/manage-geoobjecttype-modal.component';
import { MasterListManagerComponent } from './component/master-list/master-list-manager.component';
import { PublishModalComponent } from './component/master-list/publish-modal.component';
import { ExportFormatModalComponent } from './component/master-list/export-format-modal.component';
import { RequestTableComponent } from './component/crtable/request-table.component';
import { CreateUpdateGeoObjectDetailComponent } from './component/crtable/action-detail/create-update-geo-object/detail.component';
import { AddRemoveChildDetailComponent } from './component/crtable/action-detail/add-remove-child/detail.component';
import { SetParentDetailComponent } from './component/crtable/action-detail/set-parent/set-parent-detail.component';
import { GeoObjectSharedAttributeEditorComponent } from './component/geoobject-shared-attribute-editor/geoobject-shared-attribute-editor.component';
import { ManageVersionsModalComponent } from './component/geoobject-shared-attribute-editor/manage-versions-modal.component';
import { SubmitChangeRequestComponent } from './component/submit-change-request/submit-change-request.component';
import { ChangeRequestPageComponent } from './component/change-request-page/change-request-page.component';
import { GeoObjectEditorComponent } from './component/geoobject-editor/geoobject-editor.component';
import { GeoObjectEditorMapComponent } from './component/geoobject-editor-map/geoobject-editor-map.component';
import { CascadingGeoSelector } from './component/cascading-geo-selector/cascading-geo-selector';
import { ManageParentVersionsModalComponent } from './component/cascading-geo-selector/manage-parent-versions-modal.component';
import { ActionDetailModalComponent } from './component/crtable/action-detail/action-detail-modal.component';
import { ShapefileComponent } from './component/importer/shapefile.component';
import { SpreadsheetComponent } from './component/importer/spreadsheet.component';
import { DataExportComponent } from './component/data-export/data-export.component';
import { MasterListComponent } from './component/master-list/master-list.component';
import { MasterListHistoryComponent } from './component/master-list/master-list-history.component';

import { GeoObjectAttributeCodeValidator } from './factory/form-validation.factory';

import { GeoObjectTypePipe } from './pipe/geoobjecttype.pipe';
import { GeoObjectAttributeExcludesPipe } from './pipe/geoobject-attribute-excludes.pipe';
import { ToEpochDateTimePipe } from './pipe/to-epoch-date-time.pipe';
import { RegistryService } from './service/registry.service';
import { HierarchyService } from './service/hierarchy.service';
import { LocalizationManagerService } from './service/localization-manager.service';
import { ChangeRequestService } from './service/change-request.service';
import { IOService } from './service/io.service';
import { MapService } from './service/map.service';
import { GeoObjectTypeManagementService } from './service/geoobjecttype-management.service'

import { RegistryRoutingModule } from './registry-routing.module';
import { SharedModule } from '../shared/shared.module';

import '../rxjs-extensions';


@NgModule( {
    imports: [
        CommonModule,
        RouterModule,
        FormsModule,
        ReactiveFormsModule,
        FileUploadModule,
//        ModalModule.forRoot(),
        TreeModule,
        ContextMenuModule,
        BsDropdownModule,
        ButtonsModule,
        TypeaheadModule,
        ProgressbarModule,
        CollapseModule,
        NgxPaginationModule,
        CustomFormsModule,
        SharedModule,
        RegistryRoutingModule        
    ],
    declarations: [
        HierarchyComponent,
        LocalizationManagerComponent,
        RequestTableComponent,
        CreateUpdateGeoObjectDetailComponent,
        AddRemoveChildDetailComponent,
        SetParentDetailComponent,
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
        NewLocaleModalComponent,
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
        ManageVersionsModalComponent,
        SubmitChangeRequestComponent,
        GeoObjectEditorComponent,
        GeoObjectAttributeExcludesPipe,
        ToEpochDateTimePipe,
        GeoObjectEditorMapComponent,
        DataPageComponent,
        ChangeRequestPageComponent,
        CascadingGeoSelector,
        ManageParentVersionsModalComponent,        
        ActionDetailModalComponent,
        HierarchyComponent,
        ShapefileComponent,
        SpreadsheetComponent,
        DataExportComponent,
        MasterListComponent,
        MasterListHistoryComponent,
        // Master List screens
        MasterListManagerComponent,
        PublishModalComponent,
        ExportFormatModalComponent,
    ],
    providers: [
        MapService,
        HierarchyService,
        LocalizationManagerService,
        ChangeRequestService,
        IOService,
        GeoObjectTypeManagementService,
        RegistryService,
        DatePipe,
        ToEpochDateTimePipe,
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
        NewLocaleModalComponent,
        TermOptionWidgetComponent,
        AttributeInputComponent,
        EditTermOptionInputComponent,
        ManageGeoObjectTypeModalComponent,
        GeoObjectTypeInputComponent,
        ManageTermOptionsComponent,
        GeoObjectSharedAttributeEditorComponent,
        ManageVersionsModalComponent,
        ManageParentVersionsModalComponent,
        SubmitChangeRequestComponent,
        GeoObjectEditorComponent,
        PublishModalComponent,
        ExportFormatModalComponent,
        DataPageComponent,
        ChangeRequestPageComponent,
        ActionDetailModalComponent,
    ]
} )
export class RegistryModule { }
