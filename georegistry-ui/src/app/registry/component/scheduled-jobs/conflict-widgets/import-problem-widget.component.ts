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

import { Component, OnInit, Input, Output, EventEmitter } from "@angular/core";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { HttpErrorResponse } from "@angular/common/http";

import { ScheduledJob, ImportError } from "@registry/model/registry";

import { GeoObjectEditorComponent } from "../../geoobject-editor/geoobject-editor.component";

import { RegistryService } from "@registry/service";
import { DateService } from "@shared/service/date.service";
import { ErrorHandler } from "@shared/component";
import { LocalizationService } from "@shared/service/localization.service";

@Component({
    selector: "import-problem-widget",
    templateUrl: "./import-problem-widget.component.html",
    styleUrls: []
})
export class ImportProblemWidgetComponent implements OnInit {

    message: string = null;
    @Input() problem: ImportError;
    @Input() job: ScheduledJob;
    @Output() public onProblemResolved = new EventEmitter<any>();

    readonly: boolean = false;
    edit: boolean = false;

    // eslint-disable-next-line no-useless-constructor
    constructor(private service: RegistryService, private dateService: DateService,
        private lService: LocalizationService, public bsModalRef: BsModalRef, private modalService: BsModalService
    ) { }

    ngOnInit(): void {

    }

    onEditGeoObject(): void {
        let editModal = this.modalService.show(GeoObjectEditorComponent, {
            backdrop: true,
            ignoreBackdropClick: true
        });

        editModal.content.configureFromImportError(this.problem, this.job.historyId, this.job.configuration.startDate, true);
        editModal.content.setMasterListId(null);
        editModal.content.setOnSuccessCallback(() => {
            this.onProblemResolved.emit(this.problem);
            this.bsModalRef.hide();
        });
    }

    getFriendlyProblemType(probType: string): string {
        if (probType === "net.geoprism.registry.io.ParentCodeException") {
            return this.lService.decode("scheduledjobs.job.problem.type.parent.lookup");
        }

        if (probType === "net.geoprism.registry.io.PostalCodeLocationException") {
            return this.lService.decode("scheduledjobs.job.problem.type.postal.code.lookup");
        }

        if (probType === "net.geoprism.registry.io.AmbiguousParentException") {
            return this.lService.decode("scheduledjobs.job.problem.type.multi.parent.lookup");
        }

        if (probType === "net.geoprism.registry.io.InvalidGeometryException") {
            return this.lService.decode("scheduledjobs.job.problem.type.invalid.geom.lookup");
        }

        if (probType === "net.geoprism.registry.DataNotFoundException") {
            return this.lService.decode("scheduledjobs.job.problem.type.datanotfound");
        }

        if (
            probType === "net.geoprism.registry.roles.CreateGeoObjectPermissionException" ||
            probType === "net.geoprism.registry.roles.WriteGeoObjectPermissionException" ||
            probType === "net.geoprism.registry.roles.DeleteGeoObjectPermissionException" ||
            probType === "net.geoprism.registry.roles.ReadGeoObjectPermissionException"
        ) {
            return this.lService.decode("scheduledjobs.job.problem.type.permission");
        }

        // if(probType === "net.geoprism.registry.io.TermValueException"){
        //   return this.localizeService.decode( "scheduledjobs.job.problem.type.postal.code.lookup" );
        // }
        if (
            probType === "com.runwaysdk.dataaccess.DuplicateDataException" ||
            probType === "net.geoprism.registry.DuplicateGeoObjectException" ||
            probType === "net.geoprism.registry.DuplicateGeoObjectCodeException"
        ) {
            return this.lService.decode("scheduledjobs.job.problem.type.duplicate.data.lookup");
        }

        return probType;
    }

    onSubmit(): void {

    }

    onCancel(): void {
        this.bsModalRef.hide();
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
