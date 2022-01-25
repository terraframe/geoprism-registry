import { Component, Input, OnDestroy, OnInit, ViewChild } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { Subscription } from "rxjs";
import { TreeComponent, TreeModel, TreeNode, TREE_ACTIONS } from "@circlon/angular-tree-component";
import { ContextMenuComponent, ContextMenuService } from "ngx-contextmenu";

import { ConfirmModalComponent, ErrorHandler } from "@shared/component";
import { Classification, ClassificationType } from "@registry/model/classification-type";
import { LocalizationService } from "@shared/service";
import { ClassificationService } from "@registry/service/classification.service";
import { ClassificationPublishModalComponent } from "./classification-publish-modal.component";

class ClassificationNode {

    name: string;
    classification: Classification;
    hasChildren: boolean;
    children?: ClassificationNode[];
    parent?: ClassificationNode;

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
            const code = node.data.classification.code;

            return this.nodes.findIndex(root => root.classification.code === code) === -1;
        },
        allowDrop: (node: TreeNode) => true
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
        });
    }

    ngOnDestroy(): void {
        if (this.subscription != null) {
            this.subscription.unsubscribe();
        }

        this.subscription = null;
    }

    getChildren(node: TreeNode): Promise<ClassificationNode[]> {
        const code = node != null ? node.data.classification.code : null;

        return this.service.getChildren(this.classificationType.code, code).then(children => {
            const nodes = children.map(child => {
                return {
                    name: child.displayLabel.localizedValue,
                    classification: child,
                    hasChildren: true
                };
            });

            if (node != null) {
                const parent: ClassificationNode = node.data.classification;

                if (parent.children == null) {
                    parent.children = [];
                }

                parent.children.concat(nodes);
            }

            return nodes;
        }).catch(ex => {
            return [];
        });
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

    treeNodeOnClick(node: TreeNode, $event: any): void {
        node.treeModel.setFocusedNode(node);

        if (node.treeModel.isExpanded(node)) {
            node.collapse();
        } else {
            node.treeModel.expandAll();
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
                name: classification.displayLabel.localizedValue,
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
