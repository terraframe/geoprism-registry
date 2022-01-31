import { Component, OnDestroy, ViewChild } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { Observer, Subject, Subscription } from "rxjs";
import { TreeComponent, TreeNode } from "@circlon/angular-tree-component";
import { ContextMenuComponent, ContextMenuService } from "ngx-contextmenu";

import { ErrorHandler } from "@shared/component";
import { Classification } from "@registry/model/classification-type";
import { ClassificationService } from "@registry/service/classification.service";
import { PageResult } from "@shared/model/core";
import { AttributeType } from "@registry/model/registry";
import { BsModalRef } from "ngx-bootstrap/modal";

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
    selector: "classification-field-modal",
    templateUrl: "./classification-field-modal.component.html",
    styleUrls: []
})
export class ClassificationFieldModalComponent implements OnDestroy {

    message: string = null;

    attributeType: AttributeType = null;

    disabled: boolean = false;

    select: Subject<Classification> = new Subject<Classification>();

    nodes: ClassificationNode[] = null;

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
                }
            }
        },
        allowDrag: false,
        allowDrop: false,
        animateExpand: true,
        scrollOnActivate: true,
        animateSpeed: 2,
        animateAcceleration: 1.01
    }

    constructor(
        private bsModalRef: BsModalRef,
        private contextMenuService: ContextMenuService,
        private service: ClassificationService
    ) { }

    init(attributeType: AttributeType, disabled: boolean, observer: Observer<Classification>): Subscription {
        this.attributeType = attributeType;
        this.disabled = disabled;

        this.getChildren(null).then(nodes => {
            this.nodes = nodes;
        });

        return this.select.subscribe(observer);
    }

    ngOnDestroy(): void {
        this.select.unsubscribe();
    }

    getChildren(treeNode: TreeNode): Promise<ClassificationNode[]> {
        const node: ClassificationNode = treeNode != null ? treeNode.data : null;

        const code = node != null ? node.classification.code : null;

        return this.service.getChildren(this.attributeType.classificationType, code, 1, PAGE_SIZE).then(page => {
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
        if (!this.disabled) {
            this.contextMenuService.show.next({
                contextMenu: this.nodeMenuComponent,
                event: $event,
                item: node
            });
            $event.preventDefault();
            $event.stopPropagation();
        }
    }

    treeNodeOnClick(treeNode: TreeNode, $event: any): void {
        const node: ClassificationNode = treeNode != null ? treeNode.data : null;

        if (node != null && node.type === NodeType.LINK) {
            if (treeNode.parent != null) {
                const parentNode: ClassificationNode = treeNode.parent.data;
                const code = parentNode.classification.code;
                const pageNumber = node.pageNumber;

                this.service.getChildren(this.attributeType.classificationType, code, pageNumber, PAGE_SIZE).then(page => {
                    const nodes = this.createNodes(parentNode, page);

                    parentNode.children = parentNode.children.filter(node => node.code !== "...");
                    parentNode.children = parentNode.children.concat(nodes);

                    this.tree.treeModel.update();
                }).catch(ex => {
                });
            }
        } else {
            treeNode.treeModel.setFocusedNode(node);

            if (treeNode.isExpanded) {
                treeNode.collapse();
            } else {
                treeNode.expand();
                // treeNode.treeModel.expandAll();
            }
        }
    }

    onSelect(treeNode: TreeNode): void {
        const node: ClassificationNode = treeNode != null ? treeNode.data : null;

        if (node.type === NodeType.CLASSIFICATION) {
            this.select.next(node.classification);

            this.bsModalRef.hide();
        }
    }

    onCancel(): void {
        this.bsModalRef.hide();
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
