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

import { Component } from "@angular/core";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { HttpErrorResponse } from "@angular/common/http";

import { ErrorHandler } from "@shared/component";
import { CurationJob, CurationProblem, ListTypeVersion } from "@registry/model/list-type";
import { GeoObjectEditorComponent } from "../geoobject-editor/geoobject-editor.component";
import { DateService } from "@shared/service";
import { ListTypeService } from "@registry/service/list-type.service";

@Component({
    selector: "curation-problem-modal",
    templateUrl: "./curation-problem-modal.component.html",
    styleUrls: []
})
export class CurationProblemModalComponent {

    message: string = null;
    version: ListTypeVersion;
    problem: CurationProblem;
    job: CurationJob;
    callback: Function;

    readonly: boolean = false;
    edit: boolean = false;

    constructor(public service: ListTypeService, public bsModalRef: BsModalRef, private modalService: BsModalService, private dateService: DateService) {
    }

    init(version: ListTypeVersion, problem: CurationProblem, job: CurationJob, callback: Function): void {
        this.version = version;
        this.problem = problem;
        this.job = job;
        this.callback = callback;
    }

    getFriendlyProblemType(probType: string): string {
        if (probType === "NO_GEOMETRY") {
            // return this.localizeService.decode("scheduledjobs.job.problem.type.parent.lookup");
            return "Missing geometry";
        }

        return probType;
    }

    onEditGeoObject(): void {
        const editModal = this.modalService.show(GeoObjectEditorComponent, {
            backdrop: true,
            ignoreBackdropClick: true
        });

        editModal.content.configureAsExisting(this.problem.goCode, this.problem.typeCode, this.version.forDate, true);
        editModal.content.setMasterListId(this.version.oid);
        editModal.content.submitFunction = (geoObject, hierarchies, attributeEditor) => {
            // THERE HAS TO BE A BETTER WAY TO DO THIS
            if (attributeEditor.changeRequest != null) {
                const changeRequest = attributeEditor.changeRequest;

                let config = {
                    historyId: this.job.historyId,
                    problemId: this.problem.id,
                    resolution: "APPLY_GEO_OBJECT",
                    code: this.problem.goCode,
                    typeCode: this.problem.typeCode,
                    actions: changeRequest.actions
                };

                this.service.submitErrorResolve(config).then(() => {
                    this.callback({ action: "RESOLVED", data: this.problem });
                    editModal.hide();
                }).catch((err: HttpErrorResponse) => {
                    editModal.content.error(err);
                });
            }
        };

        editModal.content.setOnSuccessCallback(() => {
            this.onProblemResolvedListener(this.problem);
            this.bsModalRef.hide();
        });
    }

    onProblemResolvedListener(problem: CurationProblem): void {
        this.callback({ action: "RESOLVED", data: problem });
    }

    onCancel(): void {
        this.bsModalRef.hide();
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
