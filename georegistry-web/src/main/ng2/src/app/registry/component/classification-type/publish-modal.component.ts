import { Component, OnDestroy, OnInit } from "@angular/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Observer, Subject, Subscription } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";

import { ErrorHandler } from "@shared/component";
import { LocalizationService } from "@shared/service";
import { ClassificationType } from "@registry/model/classification-type";
import { ClassificationTypeService } from "@registry/service/classification-type.service";

@Component({
    selector: "classification-type-publish-modal",
    templateUrl: "./publish-modal.component.html",
    styleUrls: ["./classification-type-manager.css"]
})
export class ClassificationTypePublishModalComponent implements OnInit, OnDestroy {

    currentDate: Date = new Date();
    message: string = null;
    onClassificationTypeChange: Subject<ClassificationType> = null;

    type: ClassificationType = null;

    readonly: boolean = false;

    edit: boolean = false;

    isNew: boolean = false;

    valid: boolean = true;

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private service: ClassificationTypeService,
        private lService: LocalizationService,
        private bsModalRef: BsModalRef) { }

    ngOnInit(): void {
        this.onClassificationTypeChange = new Subject();
    }

    ngOnDestroy(): void {
        if (this.onClassificationTypeChange != null) {
            this.onClassificationTypeChange.unsubscribe();
        }
    }

    init(observer: Observer<ClassificationType>, type?: ClassificationType): Subscription {
        if (type == null) {
            this.isNew = true;
            this.type = {
                oid: null,
                displayLabel: this.lService.create(),
                description: this.lService.create(),
                code: ""
            };
        } else {
            this.type = type;
            this.isNew = false;
        }

        return this.onClassificationTypeChange.subscribe(observer);
    }

    onSubmit(): void {
        this.service.apply(this.type).then(response => {
            this.onClassificationTypeChange.next(response);
            this.bsModalRef.hide();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    stringify(obj: any): string {
        return JSON.stringify(obj);
    }

    onCancel(): void {
        this.bsModalRef.hide();
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
