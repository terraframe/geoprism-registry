import { Component, Input, OnDestroy, OnInit } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";

import { ConfirmModalComponent, ErrorHandler } from "@shared/component";
import { Classification, ClassificationType } from "@registry/model/classification-type";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { ClassificationTypePublishModalComponent } from "./classification-type-publish-modal.component";
import { LocalizationService } from "@shared/service";
import { Subscription } from "rxjs";
import { TreeNode } from "@circlon/angular-tree-component";
import { ClassificationService } from "@registry/service/classification.service";
import { ClassificationPublishModalComponent } from "./classification-publish-modal.component";

@Component({
    selector: "classification-type",
    templateUrl: "./classification-type.component.html",
    styleUrls: ["./classification-type-manager.css"]
})
export class ClassificationTypeComponent implements OnInit, OnDestroy {

    message: string = null;

    @Input() classificationType: ClassificationType = null;

    nodes: any[] = [];

    subscription: Subscription = null;

    /*
    * Reference to the modal current showing
    */
    bsModalRef: BsModalRef;

    options = {
        getChildren: (node: TreeNode) => {
            return this.getChildren(node);
        }
    }

    constructor(
        private service: ClassificationService,
        private lService: LocalizationService,
        private modalService: BsModalService) { }

    ngOnInit(): void {
        this.getChildren(null).then(nodes => {
            this.nodes = nodes;
        });
    }

    ngOnDestroy(): void {
        if (this.subscription != null) {
            this.subscription.unsubscribe();
        }

        this.subscription = null;
    }

    getChildren(node: any): Promise<any[]> {
        const code = node != null ? node.data.code : null;

        return this.service.getChildren(this.classificationType.code, code).then(children => {
            return children.map(child => {
                return {
                    name: child.code,
                    data: child,
                    hasChildren: true
                };
            });
        }).catch(ex => {
            return [];
        });
    }

    onCreate(node: any): void {
        if (this.subscription != null) {
            this.subscription.unsubscribe();
        }

        const parent: Classification = node != null ? node.data : null;

        this.bsModalRef = this.modalService.show(ClassificationPublishModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.subscription = this.bsModalRef.content.init(classification => {
            const newNode = {
                name: classification.code,
                data: classification,
                hasChildren: true
            };

            if (node != null) {
                node.children.push(newNode);
            } else {
                this.nodes.push(newNode);
            }
        }, this.classificationType, parent, true);
    }

    /*
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

            this.bsModalRef.content.onConfirm.subscribe(data => {
                this.service.remove(type).then(() => {
                    const index = this.page.resultSet.findIndex(t => t.oid === type.oid);

                    if (index !== -1) {
                        this.page.resultSet.splice(index, 1);
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
     */
    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
