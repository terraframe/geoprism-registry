import { Component, Input, OnDestroy, OnInit, ViewChild } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { Subscription } from "rxjs";
import { TreeComponent, TreeModel, TreeNode, TREE_ACTIONS } from "@circlon/angular-tree-component";
import { ContextMenuComponent, ContextMenuService } from "ngx-contextmenu";

import { ConfirmModalComponent, ErrorHandler } from "@shared/component";
import { Classification, ClassificationType } from "@registry/model/classification-type";
import { LocalizationService } from "@shared/service/localization.service";
import { ClassificationService } from "@registry/service/classification.service";
import { ClassificationPublishModalComponent } from "./classification-publish-modal.component";
import { PageResult } from "@shared/model/core";

const PAGE_SIZE: number = 100;

// eslint-disable-next-line no-unused-vars
enum NodeType {
    // eslint-disable-next-line no-unused-vars
    CLASSIFICATION = 0, LINK = 1
}

class ClassificationNode {

    name: string;
    code: string;
    type: NodeType;
    classification?: Classification;
    hasChildren: boolean;
    children?: ClassificationNode[];
    parent?: ClassificationNode;
    pageNumber?: number;

}

@Component({
    selector: "classification-type",
    templateUrl: "./classification-type.component.html",
    styleUrls: ["./classification-type-manager.css"]
})
export class ClassificationTypeComponent implements OnInit, OnDestroy {

    message: string = null;

    @Input() classificationType: ClassificationType = null;

    nodes: ClassificationNode[] = null;

    subscription: Subscription = null;

    /*
    * Reference to the modal current showing
    */
    bsModalRef: BsModalRef;

    /*
     * Tree component
     */
    @ViewChild(TreeComponent)
    private tree: TreeComponent;

    /*
     * Template for tree node menu
     */
    @ViewChild("nodeMenu") public nodeMenuComponent: ContextMenuComponent;

    options = {
        idField: "code",
        getChildren: (node: TreeNode) => {
            return this.getChildren(node);
        },
        actionMapping: {
            mouse: {
                click: (tree: TreeComponent, node: TreeNode, $event: any) => {
                    this.treeNodeOnClick(node, $event);
                },
                contextMenu: (tree: any, node: TreeNode, $event: any) => {
                    this.handleOnMenu(node, $event);
                },
                drop: (tree: TreeModel, node: TreeNode, $event: any, obj: {
                    from: any;
                    to: any;
                }) => {
                    this.onMoveNode(tree, node, $event, obj);
                }
            }
        },
        allowDrag: (node: TreeNode) => {
            if (node.data.type === NodeType.CLASSIFICATION) {
                const code = node.data.classification.code;

                return this.nodes.findIndex(root => root.classification.code === code) === -1;
            }

            return false;
        },
        allowDrop: (node: TreeNode, event: { parent: TreeNode, index: number }) => {
            if (event != null && event.parent != null) {
                return event.parent.data.type === NodeType.CLASSIFICATION;
            }

            return false;
        },
        animateExpand: true,
        scrollOnActivate: true,
        animateSpeed: 2,
        animateAcceleration: 1.01
    }

    constructor(
        private contextMenuService: ContextMenuService,
        private modalService: BsModalService,
        private service: ClassificationService,
        private lService: LocalizationService
    ) { }

    ngOnInit(): void {
        this.getChildren(null).then(nodes => {
            this.nodes = nodes;

            if (this.nodes.length > 0) {
                window.setTimeout(() => {
                    this.tree.treeModel.getFirstRoot().expand();
                }, 50);
            }
        });
    }

    ngOnDestroy(): void {
        if (this.subscription != null) {
            this.subscription.unsubscribe();
        }

        this.subscription = null;
    }

    getChildren(treeNode: TreeNode): Promise<ClassificationNode[]> {
        const node: ClassificationNode = treeNode != null ? treeNode.data : null;

        const code = node != null ? node.classification.code : null;

        return this.service.getChildren(this.classificationType.code, code, 1, PAGE_SIZE).then(page => {
            const nodes = this.createNodes(node, page);

            if (node != null) {
                if (node.children == null) {
                    node.children = [];
                }

                node.children.concat(nodes);
            }

            return nodes;
        }).catch(ex => {
            return [];
        });
    }

    createNodes(parent: ClassificationNode, page: PageResult<Classification>): ClassificationNode[] {
        const nodes = page.resultSet.map(child => {
            return {
                code: child.code,
                name: child.displayLabel.localizedValue,
                type: NodeType.CLASSIFICATION,
                classification: child,
                hasChildren: true
            } as ClassificationNode;
        });

        // Add page node if needed
        if (page.count > page.pageNumber * page.pageSize) {
            nodes.push({
                code: "...",
                name: "...",
                type: NodeType.LINK,
                hasChildren: false,
                pageNumber: page.pageNumber + 1,
                parent: parent
            } as ClassificationNode);
        }

        return nodes;
    }

    handleOnMenu(node: TreeNode, $event: any): void {
        this.contextMenuService.show.next({
            contextMenu: this.nodeMenuComponent,
            event: $event,
            item: node
        });
        $event.preventDefault();
        $event.stopPropagation();
    }

    treeNodeOnClick(treeNode: TreeNode, $event: any): void {
        const node: ClassificationNode = treeNode != null ? treeNode.data : null;

        if (node != null && node.type === NodeType.LINK) {
            if (treeNode.parent != null) {
                const parentNode: ClassificationNode = treeNode.parent.data;
                const code = parentNode.classification.code;
                const pageNumber = node.pageNumber;

                this.service.getChildren(this.classificationType.code, code, pageNumber, PAGE_SIZE).then(page => {
                    const nodes = this.createNodes(parentNode, page);

                    parentNode.children = parentNode.children.filter(node => node.code !== "...");
                    parentNode.children = parentNode.children.concat(nodes);

                    this.tree.treeModel.update();
                }).catch(ex => {
                });
            }
        } else {
            if (treeNode.isExpanded) {
                treeNode.collapse();
            } else {
                treeNode.expand();
            }

            treeNode.setActiveAndVisible();
        }
    }

    onCreate(parentNode: TreeNode): void {
        if (this.subscription != null) {
            this.subscription.unsubscribe();
        }

        const parent: ClassificationNode = parentNode != null ? parentNode.data : null;

        this.bsModalRef = this.modalService.show(ClassificationPublishModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.subscription = this.bsModalRef.content.init(classification => {
            const node: ClassificationNode = {
                code: classification.code,
                name: classification.displayLabel.localizedValue,
                type: NodeType.CLASSIFICATION,
                classification: classification,
                hasChildren: true
            };

            if (parentNode != null) {
                parent.children.push(node);
            } else {
                this.nodes.push(node);
            }

            this.tree.treeModel.update();
        }, this.classificationType, (parent != null ? parent.classification : null));
    }

    onEdit(node: TreeNode): void {
        if (this.subscription != null) {
            this.subscription.unsubscribe();
        }

        this.bsModalRef = this.modalService.show(ClassificationPublishModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.subscription = this.bsModalRef.content.init(classification => {
            const classificationNode: ClassificationNode = node.data;
            classificationNode.classification = classification;
            classificationNode.name = classification.displayLabel.localizedValue;

            this.tree.treeModel.update();
        }, this.classificationType, null, node.data.classification);
    }

    onRemove(node: TreeNode): void {
        this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.message = this.lService.decode("confirm.modal.verify.delete") + " [" + node.data.classification.code + "]";
        this.bsModalRef.content.submitText = this.lService.decode("modal.button.delete");
        this.bsModalRef.content.type = "danger";

        this.bsModalRef.content.onConfirm.subscribe(() => {
            this.removeTreeNode(node);
        });
    }

    onMoveNode(tree: TreeModel, node: TreeNode, $event: any, obj: {
        from: any;
        to: any;
    }): void {
        const parent: Classification = node.data.classification;
        const classification: Classification = obj.from.data.classification;
        const parentCode = parent.code;
        const code = classification.code;

        let message = this.lService.decode("classification.move.message");
        message = message.replace("{0}", classification.displayLabel.localizedValue);
        message = message.replace("{1}", parent.displayLabel.localizedValue);

        this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.message = message;
        this.bsModalRef.content.type = "danger";

        this.bsModalRef.content.onConfirm.subscribe(() => {
            this.message = null;

            this.service.move(this.classificationType.code, code, parentCode).then(() => {
                TREE_ACTIONS.MOVE_NODE(tree, node, $event, obj);
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        });
    }

    removeTreeNode(node: TreeNode): void {
        this.message = null;

        this.service.remove(this.classificationType.code, node.data.classification.code).then(() => {
            if (node.parent.data.classification == null) {
                this.nodes = [];
            }

            const parent: TreeNode = node.parent;
            const children = parent.data.children;

            // Update the tree
            parent.data.children = children.filter((n: any) => n.id !== node.data.id);

            if (parent.data.children.length === 0) {
                parent.data.hasChildren = false;
            }
            this.tree.treeModel.update();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    /*

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
