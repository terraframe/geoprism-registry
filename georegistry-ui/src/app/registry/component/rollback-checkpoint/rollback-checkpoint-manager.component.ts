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

import { Component, OnInit } from "@angular/core";
import { BsModalService } from "ngx-bootstrap/modal";
import { HttpErrorResponse } from "@angular/common/http";

import { ErrorHandler, ConfirmModalComponent } from "@shared/component";
import { LocalizationService } from "@shared/service/localization.service";
import { RollbackCheckpointService } from "@registry/service/rollback-checkpoint.service";
import { PageResult } from "@shared/model/core";
import { RollbackCheckpoint } from "@registry/model/rollback-checkpoint";

@Component({
    selector: "rollback-checkpoint-manager",
    templateUrl: "./rollback-checkpoint-manager.component.html",
    styleUrls: ["./rollback-checkpoint-manager.css"]
})
export class RollbackCheckpointManagerComponent implements OnInit {

    message: string = null;

    page: PageResult<RollbackCheckpoint> = {
        count: 0,
        pageNumber: 1,
        pageSize: 20,
        resultSet: []
    };

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private service: RollbackCheckpointService,
        private localizeService: LocalizationService,
        private modalService: BsModalService) { }

    ngOnInit(): void {
        this.onPageChange(1);
    }


    onRollback(checkpoint: RollbackCheckpoint): void {
        const bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        bsModalRef.content.message = this.localizeService.decode("modal.button.rollback").replaceAll("{filename}", checkpoint.filename);
        bsModalRef.content.submitText = this.localizeService.decode("modal.button.rollback");
        bsModalRef.content.type = "danger";

        bsModalRef.content.onConfirm.subscribe(() => {
            this.service.rollback(checkpoint.oid).then(() => {
                this.onPageChange(1);
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        });
    }

    onPageChange(pageNumber: number): void {
		this.service.getPage(pageNumber, 20).then(page => {
			this.page = page;
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}


    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
