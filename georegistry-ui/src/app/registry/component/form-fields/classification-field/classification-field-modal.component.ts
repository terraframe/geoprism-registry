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

import { Component, OnDestroy, ViewChild } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { Observer, Subject, Subscription } from "rxjs";
import { TreeComponent, TreeNode, TreeModule } from "@ali-hm/angular-tree-component";
import { ContextMenuComponent, ContextMenuService, ContextMenuModule } from "@perfectmemory/ngx-contextmenu";

import { ErrorHandler } from "@shared/component";
import { PageResult } from "@shared/model/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { LocalizeComponent } from "@shared/component/localize/localize.component";
import { FormsModule } from "@angular/forms";
import { ObjectOverTime, ObjectOverTimeNode } from "@registry/model/object-class";
import { ConceptObjectService } from "@registry/service/concept-object.service";
import { Page } from "ngx-pagination";
import { AttributedType, AttributeType } from "@registry/model/registry";

const PAGE_SIZE: number = 100;

// eslint-disable-next-line no-unused-vars
enum NodeType {
    // eslint-disable-next-line no-unused-vars
    CLASSIFICATION = 0, LINK = 1
}

class ClassificationTreeNode {

    name: string;
    type: NodeType;
    classification?: ObjectOverTime;
    hasChildren: boolean;
    children?: ClassificationTreeNode[];
    parent?: ClassificationTreeNode;
    pageNumber?: number;

}

@Component({
    selector: "classification-field-modal",
    templateUrl: "./classification-field-modal.component.html",
    styleUrls: [],
    standalone: true,
    imports: [ContextMenuModule, FormsModule, TreeModule, LocalizeComponent]
})
export class ClassificationFieldModalComponent implements OnDestroy {

    message: string = null;

    type: AttributedType;

    attribute: AttributeType;

    disabled: boolean = false;

    select: Subject<ObjectOverTime> = new Subject<ObjectOverTime>();

    nodes: ClassificationTreeNode[] = [];

    /*
     * Tree component
     */
    @ViewChild(TreeComponent)
    private tree: TreeComponent;

    /*
     * Template for tree node menu
     */
    @ViewChild("nodeMenu") public nodeMenuComponent: ContextMenuComponent<TreeNode>;

    options = {
        idField: "name",
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
        private contextMenuService: ContextMenuService<TreeNode>,
        private service: ConceptObjectService
    ) { }

    init(type: AttributedType, attribute: AttributeType, disabled: boolean, value: { code: string }, observer: Partial<Observer<ObjectOverTime>> | ((value: ObjectOverTime) => void)): Subscription {
        this.type = type;
        this.attribute = attribute;
        this.disabled = disabled;

        if (value != null) {
            this.service.getAncestorTree(this.type, this.attribute, value.code, PAGE_SIZE).then(ancestor => {
                this.nodes = [this.build(null, ancestor)];

                window.setTimeout(() => {
                    const node: TreeNode = this.tree.treeModel.getNodeById(value.code);

                    if (node != null) {
                        node.setActiveAndVisible();
                    }
                }, 100);
            });
        } else
            this.service.get(this.attribute.rootTerm.type, this.attribute.rootTerm.code).then(classification => {
                this.nodes = [{
                    name: classification.code,
                    type: NodeType.CLASSIFICATION,
                    classification: classification,
                    hasChildren: true
                }];
            });

        return this.select.subscribe(observer);
    }

    ngOnDestroy(): void {
        this.select.unsubscribe();
    }

    getChildren(treeNode: TreeNode): Promise<ClassificationTreeNode[]> {

        const node: ClassificationTreeNode = treeNode != null ? treeNode.data : null;

        const code = node != null ? node.classification.code : null;

        return this.service.getChildren(this.type, this.attribute, code, 1, PAGE_SIZE).then(page => {
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

    build(parent: ClassificationTreeNode, cNode: ObjectOverTimeNode): ClassificationTreeNode {
        const node: ClassificationTreeNode = {
            name: cNode.object.code,
            type: NodeType.CLASSIFICATION,
            classification: cNode.object,
            hasChildren: true
        };

        if (cNode.children != null) {
            const nodes: ClassificationTreeNode[] = cNode.children.resultSet.map(child => this.build(parent, child));

            const page: PageResult<ObjectOverTimeNode> = cNode.children;

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

    createNodes(parent: ClassificationTreeNode, page: PageResult<ObjectOverTime>): ClassificationTreeNode[] {
        const nodes = page.resultSet.map(child => {
            return {
                name: child.code,
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
            this.contextMenuService.show(this.nodeMenuComponent, {
                value: node,
                x: $event.x,
                y: $event.y
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

                this.service.getChildren(this.type, this.attribute, code, pageNumber, PAGE_SIZE).then(page => {
                    const nodes = this.createNodes(parentNode, page);

                    parentNode.children = parentNode.children.filter(node => node.name !== "...");
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
