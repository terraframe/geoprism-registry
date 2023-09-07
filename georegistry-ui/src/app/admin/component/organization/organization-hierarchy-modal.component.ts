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

import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';
import { ConfirmModalComponent, ErrorHandler } from '@shared/component';
import { Organization, PageResult } from '@shared/model/core';

import { LocalizationService, OrganizationService } from '@shared/service';
import { TreeComponent, TreeModel, TreeNode, TREE_ACTIONS } from '@circlon/angular-tree-component';
import { ContextMenuComponent, ContextMenuService } from '@perfectmemory/ngx-contextmenu';

const PAGE_SIZE: number = 100;

enum TreeNodeType {
	// eslint-disable-next-line no-unused-vars
	OBJECT = 0, LINK = 1
}

class PaginatedTreeNode<T> {

	name: string;
	code: string;
	type: TreeNodeType
	object?: T;
	hasChildren: boolean;
	children?: PaginatedTreeNode<T>[];
	parent?: PaginatedTreeNode<T>;
	pageNumber?: number;
}

@Component({
	selector: 'organization-hierarchy-modal',
	templateUrl: './organization-hierarchy-modal.component.html',
	styles: ['.modal-form .check-block .chk-area { margin: 10px 0px 0 0;}']
})
export class OrganizationHierarchyModalComponent implements OnInit {

	/*
	 * Organization Tree component
	 */
	nodes: PaginatedTreeNode<Organization>[] = [];

	/*
	 * Organization Tree component
	 */
	@ViewChild(TreeComponent)
	private tree: TreeComponent;

	/*
	 * Template for tree node menu
	 */
	@ViewChild("nodeMenu") public nodeMenuComponent: ContextMenuComponent<TreeNode>;

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
			return true;
		},
		allowDrop: (node: TreeNode, event: { parent: TreeNode, index: number }) => {
			return true;
		},
		animateExpand: true,
		scrollOnActivate: true,
		animateSpeed: 2,
		animateAcceleration: 1.01
	}

	public onConfirm: Subject<void>;

	message: string = null;

	constructor(
		private orgService: OrganizationService,
		private contextMenuService: ContextMenuService<TreeNode>,
		public bsModalRef: BsModalRef,
		private modalService: BsModalService,
		private localizeService: LocalizationService
	) { }

	ngOnInit(): void {
		this.onConfirm = new Subject();

		this.getChildren(null).then(nodes => {
			this.nodes = nodes;

			if (this.nodes.length > 0) {
				window.setTimeout(() => {
					this.tree.treeModel.getFirstRoot().expand();
				}, 50);
			}
		});
	}

	getChildren(treeNode: TreeNode): Promise<PaginatedTreeNode<Organization>[]> {
		const node: PaginatedTreeNode<Organization> = treeNode != null ? treeNode.data : null;

		const code = node != null ? node.object.code : null;

		return this.orgService.getChildren(code, 1, PAGE_SIZE).then(page => {
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

	createNodes(parent: PaginatedTreeNode<Organization>, page: PageResult<Organization>): PaginatedTreeNode<Organization>[] {
		const nodes = page.resultSet.map(child => {
			return {
				code: child.code,
				name: child.label.localizedValue,
				object: child,
				hasChildren: true
			} as PaginatedTreeNode<Organization>;
		});

		// Add page node if needed
		if (page.count > page.pageNumber * page.pageSize) {
			nodes.push({
				code: "...",
				name: "...",
				type: TreeNodeType.LINK,
				hasChildren: false,
				pageNumber: page.pageNumber + 1,
				parent: parent
			} as PaginatedTreeNode<Organization>);
		}

		return nodes;
	}

	handleOnMenu(node: TreeNode, $event: any): void {

		if (node.data.object.parentCode != null) {

			this.contextMenuService.show(this.nodeMenuComponent, {
				value: node,
				x: $event.x,
				y: $event.y,
			});

			$event.preventDefault();
			$event.stopPropagation();
		}
	}

	treeNodeOnClick(treeNode: TreeNode, $event: any): void {
		const node: PaginatedTreeNode<Organization> = treeNode != null ? treeNode.data : null;

		if (node != null && node.type === TreeNodeType.LINK) {
			if (treeNode.parent != null) {
				const parentNode: PaginatedTreeNode<Organization> = treeNode.parent.data;
				const code = parentNode.object.code;
				const pageNumber = node.pageNumber;

				this.orgService.getChildren(code, pageNumber, PAGE_SIZE).then(page => {
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

	onMoveNode(tree: TreeModel, node: TreeNode, $event: any, obj: {
		from: any;
		to: any;
	}): void {
		const parent: Organization = node.data.object;
		const organization: Organization = obj.from.data.object;

		const parentCode = parent.code;
		const code = organization.code;

		let message = this.localizeService.decode("classification.move.message");
		message = message.replace("{0}", organization.label.localizedValue);
		message = message.replace("{1}", parent.label.localizedValue);

		const modalRef = this.modalService.show(ConfirmModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true
		});
		modalRef.content.message = message;
		modalRef.content.type = "danger";

		modalRef.content.onConfirm.subscribe(() => {
			this.message = null;

			this.orgService.move(code, parentCode).then(() => {
				TREE_ACTIONS.MOVE_NODE(tree, node, $event, obj);
			}).catch((err: HttpErrorResponse) => {
				this.error(err);
			});
		});
	}

	onRemoveParent(node: TreeNode): void {
		const organizationNode: PaginatedTreeNode<Organization> = node.data;
		const organization: Organization = organizationNode.object;

		let message = this.localizeService.decode("classification.move.message");
		message = message.replace("{0}", organization.label.localizedValue);
		message = message.replace("{1}", "ROOT");

		const modalRef = this.modalService.show(ConfirmModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true
		});
		modalRef.content.message = message;
		modalRef.content.type = "danger";

		modalRef.content.onConfirm.subscribe(() => {
			this.message = null;

			this.orgService.removeParent(organization.code).then(() => {
				const parent: TreeNode = node.parent;
				const children = parent.data.children;

				// Update the tree
				parent.data.children = children.filter((n: any) => n.id !== node.data.id);

				if (parent.data.children.length === 0) {
					parent.data.hasChildren = false;
				}

				organizationNode.parent = null;

				this.nodes.push(organizationNode);

				this.tree.treeModel.update();

			}).catch((err: HttpErrorResponse) => {
				this.error(err);
			});
		});
	}


	onClose(): void {
		this.onConfirm.next();

		this.bsModalRef.hide();
	}

	public error(err: HttpErrorResponse): void {
		this.message = ErrorHandler.getMessageFromError(err);
	}

}