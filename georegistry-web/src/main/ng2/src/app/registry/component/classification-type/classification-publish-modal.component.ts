import { Component, OnDestroy, OnInit } from "@angular/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Observer, Subject, Subscription } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";

import { ErrorHandler } from "@shared/component";
import { LocalizationService } from "@shared/service";
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

    edit: boolean = false;

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
                code: ""
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
