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

import { Component, OnDestroy, OnInit } from "@angular/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Observer, Subject, Subscription } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";

import { ErrorHandler } from "@shared/component";
import { LocalizationService } from "@shared/service/localization.service";
import { Classification, ClassificationType } from "@registry/model/classification-type";
import { ClassificationService } from "@registry/service/classification.service";

@Component({
    selector: "classification-publish-modal",
    templateUrl: "./classification-publish-modal.component.html",
    styleUrls: ["./classification-type-manager.css"]
})
export class ClassificationPublishModalComponent implements OnInit, OnDestroy {

    message: string = null;

    onClassificationChange: Subject<Classification> = null;

    classificationType: ClassificationType = null;

    parent: Classification = null;

    classification: Classification = null;

    readonly: boolean = false;

    isNew: boolean = false;

    valid: boolean = true;

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private service: ClassificationService,
        private lService: LocalizationService,
        private bsModalRef: BsModalRef) { }

    ngOnInit(): void {
        this.onClassificationChange = new Subject();
    }

    ngOnDestroy(): void {
        if (this.onClassificationChange != null) {
            this.onClassificationChange.unsubscribe();
        }
    }

    init(observer: Observer<ClassificationType>, classificationType: ClassificationType, parent: Classification, classification?: Classification): Subscription {
        this.classificationType = classificationType;
        this.parent = parent;

        if (classification == null) {
            this.isNew = true;
            this.classification = {
                code: "",
                displayLabel: this.lService.create(),
                description: this.lService.create()
            };
        } else {
            this.classification = classification;
            this.isNew = false;
        }

        return this.onClassificationChange.subscribe(observer);
    }

    onSubmit(): void {
        const classificationType = this.classificationType.code;
        const parentCode = this.parent != null ? this.parent.code : null;

        // classificationType: string, parentCode: string, classification: Classification, isNew: boolean
        this.service.apply(classificationType, parentCode, this.classification, this.isNew).then(response => {
            this.onClassificationChange.next(response);
            this.bsModalRef.hide();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onCancel(): void {
        this.bsModalRef.hide();
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
