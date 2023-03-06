import { Component, OnDestroy, ViewChild } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { NextObserver, Observer, Subject, Subscription } from "rxjs";
import { TreeComponent, TreeNode } from "@circlon/angular-tree-component";
import { ContextMenuComponent, ContextMenuService } from "ngx-contextmenu";

import { ErrorHandler } from "@shared/component";
import { Classification, ClassificationNode } from "@registry/model/classification-type";
import { ClassificationService } from "@registry/service/classification.service";
import { PageResult } from "@shared/model/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { timeout } from "d3";

const PAGE_SIZE: number = 100;

// eslint-disable-next-line no-unused-vars
enum NodeType {
    // eslint-disable-next-line no-unused-vars
    CLASSIFICATION = 0, LINK = 1
}

class ClassificationTreeNode {

    name: string;
    code: string;
    type: NodeType;
    classification?: Classification;
    hasChildren: boolean;
    children?: ClassificationTreeNode[];
    parent?: ClassificationTreeNode;
    pageNumber?: number;

}

@Component({
    selector: "classification-field-modal",
    templateUrl: "./classification-field-modal.component.html",
    styleUrls: []
})
export class ClassificationFieldModalComponent implements OnDestroy {

    message: string = null;

    classificationType: string = null;
    rootCode: string = null;

    disabled: boolean = false;

    select: Subject<Classification> = new Subject<Classification>();

    nodes: ClassificationTreeNode[] = [];

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

    init(classificationType: string, rootCode: string, disabled: boolean, value: { code: string }, observer: Partial<Observer<Classification>> | ((value: Classification) => void)): Subscription {
        this.classificationType = classificationType;
        this.rootCode = rootCode;
        this.disabled = disabled;

        if (value != null) {
            this.service.getAncestorTree(this.classificationType, this.rootCode, value.code, PAGE_SIZE).then(ancestor => {
                this.nodes = [this.build(null, ancestor)];

                timeout(() => {
                    const node: TreeNode = this.tree.treeModel.getNodeById(value.code);

                    if (node != null) {
                        node.setActiveAndVisible();
                    }
                }, 100);
            });
        } else if (this.rootCode != null) {
            this.service.get(this.classificationType, this.rootCode).then(classification => {
                this.nodes = [{
                    code: classification.code,
                    name: classification.displayLabel.localizedValue,
                    type: NodeType.CLASSIFICATION,
                    classification: classification,
                    hasChildren: true
                }];
            });
        } else {
            this.getChildren(null).then(nodes => {
                this.nodes = nodes;
            });
        }

        return this.select.subscribe(observer);
    }

    ngOnDestroy(): void {
        this.select.unsubscribe();
    }

    getChildren(treeNode: TreeNode): Promise<ClassificationTreeNode[]> {
        const node: ClassificationTreeNode = treeNode != null ? treeNode.data : null;

        const code = node != null ? node.classification.code : null;

        return this.service.getChildren(this.classificationType, code, 1, PAGE_SIZE).then(page => {
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

    build(parent: ClassificationTreeNode, cNode: ClassificationNode): ClassificationTreeNode {
        const node: ClassificationTreeNode = {
            code: cNode.classification.code,
            name: cNode.classification.displayLabel.localizedValue,
            type: NodeType.CLASSIFICATION,
            classification: cNode.classification,
            hasChildren: true
        };

        if (cNode.children != null) {
            const nodes: ClassificationTreeNode[] = cNode.children.resultSet.map(child => this.build(parent, child));

            const page = cNode.children;

            // Add page node if needed
            if (page.count > page.pageNumber * page.pageSize) {
                nodes.push({
                    code: "...",
                    name: "...",
                    type: NodeType.LINK,
                    hasChildren: false,
                    pageNumber: page.pageNumber + 1,
                    parent: parent
                } as ClassificationTreeNode);
            }

            node.children = nodes;
        }

        return node;
    }

    createNodes(parent: ClassificationTreeNode, page: PageResult<Classification>): ClassificationTreeNode[] {
        const nodes = page.resultSet.map(child => {
            return {
                code: child.code,
                name: child.displayLabel.localizedValue,
                type: NodeType.CLASSIFICATION,
                classification: child,
                hasChildren: true
            } as ClassificationTreeNode;
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
            } as ClassificationTreeNode);
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
        const node: ClassificationTreeNode = treeNode != null ? treeNode.data : null;

        if (node != null && node.type === NodeType.LINK) {
            if (treeNode.parent != null) {
                const parentNode: ClassificationTreeNode = treeNode.parent.data;
                const code = parentNode.classification.code;
                const pageNumber = node.pageNumber;

                this.service.getChildren(this.classificationType, code, pageNumber, PAGE_SIZE).then(page => {
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

    onSelect(treeNode: TreeNode): void {
        const node: ClassificationTreeNode = treeNode != null ? treeNode.data : null;

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
