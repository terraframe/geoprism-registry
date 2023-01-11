import { Component, OnDestroy, OnInit } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";

import { ConfirmModalComponent, ErrorHandler } from "@shared/component";
import { ClassificationTypeService } from "@registry/service/classification-type.service";
import { ClassificationType } from "@registry/model/classification-type";
import { PageResult } from "@shared/model/core";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { LocalizationService } from "@shared/service/localization.service";
import { Subscription } from "rxjs";
import { ClassificationTypePublishModalComponent } from "./classification-type-publish-modal.component";
import { ActivatedRoute, Params, Router } from "@angular/router";

@Component({
    selector: "classification-type-manager",
    templateUrl: "./classification-type-manager.component.html",
    styleUrls: ["./classification-type-manager.css"]
})
export class ClassificationTypeManagerComponent implements OnInit, OnDestroy {

    message: string = null;

    page: PageResult<ClassificationType> = {
        count: 0,
        pageNumber: 1,
        pageSize: 10,
        resultSet: []
    };

    querySubscription: Subscription = null;

    subscription: Subscription = null;

    classificationType: ClassificationType = null;

    /*
    * Reference to the modal current showing
    */
    bsModalRef: BsModalRef;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private service: ClassificationTypeService,
        private lService: LocalizationService,
        private modalService: BsModalService) { }

    ngOnInit(): void {
        this.querySubscription = this.route.queryParams.subscribe((params: Params) => {
            const typeCode = params.typeCode;

            if (typeCode != null && typeCode.length > 0) {
                if (this.classificationType == null || this.classificationType.code !== typeCode) {
                    this.classificationType = null;

                    this.service.get(typeCode).then(classificationType => {
                        this.classificationType = classificationType;
                    }).catch((err: HttpErrorResponse) => {
                        this.error(err);
                    });
                }
            } else {
                this.classificationType = null;
            }
        });

        this.refresh();
    }

    ngOnDestroy(): void {
        if (this.querySubscription != null) {
            this.querySubscription.unsubscribe();
        }

        this.querySubscription = null;

        if (this.subscription != null) {
            this.subscription.unsubscribe();
        }

        this.subscription = null;
    }

    onCreate(): void {
        if (this.subscription != null) {
            this.subscription.unsubscribe();
        }

        this.bsModalRef = this.modalService.show(ClassificationTypePublishModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.subscription = this.bsModalRef.content.init(() => this.refresh());
    }

    onEdit(type: ClassificationType): void {
        if (this.subscription != null) {
            this.subscription.unsubscribe();
        }

        this.bsModalRef = this.modalService.show(ClassificationTypePublishModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.subscription = this.bsModalRef.content.init(() => this.refresh(), type);
    }

    onDelete(type: ClassificationType): void {
        this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.message = this.lService.decode("confirm.modal.verify.delete") + " [" + type.displayLabel.localizedValue + "]";
        this.bsModalRef.content.submitText = this.lService.decode("modal.button.delete");
        this.bsModalRef.content.type = "danger";

        this.bsModalRef.content.onConfirm.subscribe(() => {
            this.service.remove(type).then(() => {
                const index = this.page.resultSet.findIndex(t => t.oid === type.oid);

                if (index !== -1) {
                    this.page.resultSet.splice(index, 1);
                }

                if (this.classificationType != null && type.code === this.classificationType.code) {
                    this.router.navigate([], {
                        relativeTo: this.route,
                        queryParams: { typeCode: null }
                    });
                }
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        });
    }

    refresh(): void {
        this.service.page({}).then(page => {
            this.page = page;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
