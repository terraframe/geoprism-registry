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

import { Component, OnInit, ViewChild, ElementRef, Input, Output, EventEmitter, SimpleChanges } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { debounceTime, distinctUntilChanged, filter, tap } from "rxjs/operators";
import { fromEvent } from "rxjs";
import { BsModalService } from "ngx-bootstrap/modal";
import * as lodash from 'lodash';

import * as d3 from "d3";


import { ConfirmModalComponent } from "@shared/component";
import { LocalizationService, AuthService } from "@shared/service";

import { HierarchyType, HierarchyNode } from "@registry/model/hierarchy";
import { GeoObjectType } from "@registry/model/registry";
import { Organization } from "@shared/model/core";
import { RegistryService, HierarchyService } from "@registry/service";

import { SvgHierarchyType } from "./d3/svg-hierarchy-type";
import { svgPoint, isPointWithin, calculateTextWidth, getBboxFromSelection } from "./d3/svg-util";
import { ImportHistoryModalComponent } from "@registry/component/import-history/modals/import-history-modal.component";

export const TREE_SCALE_FACTOR_X: number = 1.4;
export const TREE_SCALE_FACTOR_Y: number = 1.4;

export const DEFAULT_NODE_FILL = "#e6e6e6";
export const DEFAULT_NODE_BANNER_COLOR = "#A29BAB";
export const INHERITED_NODE_FILL = "#d4d4d4";
export const INHERITED_NODE_BANNER_COLOR = "#a0a0a0";
export const RELATED_NODE_BANNER_COLOR = INHERITED_NODE_BANNER_COLOR;

export class Instance {
    active: boolean;
    label: string;
}

export interface DropTarget {
    dropSelector: string;
    onDrag(dragEl: Element, dropEl: Element, event: any): void;
    onDrop(dragEl: Element, event: any): void;
    [others: string]: any;
}

enum Action {
    VIEW = 0, CREATE = 1, EDIT = 2
}

enum Tab {
    METADATA = 0, HIERARCHY = 1
}

interface Selection {
    action: Action
    type: HierarchyType;
}

@Component({
    selector: "hierarchy-type-page",
    templateUrl: "./hierarchy-type-page.component.html",
    styleUrls: ["./hierarchy-type-page.css"]
})
export class HierarchyTypePageComponent implements OnInit {
    Action = Action;
    Tab = Tab;

    @Input() userOrganization: string = null;
    @Input() organizations: Organization[] = [];
    @Input() geoObjectTypes: GeoObjectType[] = [];
    @Input() hierarchies: HierarchyType[] = [];

    @Output() onError: EventEmitter<HttpErrorResponse> = new EventEmitter<HttpErrorResponse>()
    @Output() onHierarchyType: EventEmitter<HierarchyType> = new EventEmitter<HierarchyType>()
    @Output() onRemove: EventEmitter<string> = new EventEmitter<string>()
    @Output() onGeoObjectType: EventEmitter<GeoObjectType> = new EventEmitter<GeoObjectType>()

    primarySvgHierarchy: SvgHierarchyType;

    instance: Instance = new Instance();

    hierarchiesByOrg: { org: Organization, hierarchies: HierarchyType[] }[] = [];
    typesByOrg: { org: Organization, types: GeoObjectType[] }[] = [];

    filter: string = "";
    filteredHierarchiesByOrg: { org: Organization, hierarchies: HierarchyType[] }[] = [];

    @ViewChild("searchInput", { static: true }) searchInput: ElementRef;

    isSRA: boolean = false;

    dropTargets: DropTarget[] = []

    selection: Selection;
    tab: Tab = Tab.METADATA;

    constructor(
        private hierarchyService: HierarchyService,
        private modalService: BsModalService,
        private localizeService: LocalizationService,
        private registryService: RegistryService,
        private authService: AuthService) {
        this.isSRA = authService.isSRA();
    }

    ngOnInit(): void {
        fromEvent(this.searchInput.nativeElement, "keyup").pipe(

            // get value
            filter(Boolean),
            debounceTime(500),
            distinctUntilChanged(),
            tap(() => {
                this.onFilterChange();
            })
            // subscription for response
        ).subscribe();

        this.registerDragHandlers();
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes['geoObjectTypes'] || changes['organizations']) {
            const organizations = changes['organizations'] ? changes['organizations'].currentValue : this.organizations;
            const types = changes['geoObjectTypes'] ? changes['geoObjectTypes'].currentValue : this.geoObjectTypes;

            this.typesByOrg = organizations.map(org => {
                return { org: org, types: types.filter(t => t.organizationCode === org.code) };
            });

            this.updateGeoObjectTypeStatus();
        }

        if (changes['hierarchies'] || changes['organizations']) {
            const organizations = changes['organizations'] ? changes['organizations'].currentValue : this.organizations;
            const hierarchies = changes['hierarchies'] ? changes['hierarchies'].currentValue : this.hierarchies;

            this.hierarchiesByOrg = organizations.map(org => {
                return { org: org, hierarchies: hierarchies.filter(t => t.organizationCode === org.code) };
            });

            this.onFilterChange();
        }

    }

    ngAfterViewInit() {
    }

    localize(key: string): string {
        return this.localizeService.decode(key);
    }

    handleTabChange(tab: Tab): void {
        this.tab = tab;

        if (this.tab === Tab.HIERARCHY) {
            setTimeout(() => {
                this.renderTree();
            }, 100);
        }
    }


    getNodeByCode(code: string): HierarchyNode {
        if (this.selection != null && this.selection.type.rootGeoObjectTypes != null) {

            for (let i = 0; i < this.selection.type.rootGeoObjectTypes.length; i++) {
                const root = this.selection.type.rootGeoObjectTypes[i];

                const node = this.findNodeByCode(code, root);

                if (node != null) {
                    return node;
                }
            }
        }

        return null;
    }

    findNodeByCode(code: string, node: HierarchyNode): HierarchyNode {

        if (node.geoObjectType === code) {
            return node;
        }

        if (node.children != null) {

            for (let i = 0; i < node.children.length; i++) {

                const child = this.findNodeByCode(code, node.children[i]);

                if (child != null) {
                    return child;
                }
            }
        }

        return null;
    }


    calculateCanDrag(got: GeoObjectType): boolean {

        if (this.selection != null) {
            // Check permissions against GOT and Hierarchy org
            if (!(this.authService.isSRA() || this.authService.isOrganizationRA(this.selection.type.organizationCode))) {
                return false;
            }

            // If the child is already on the graph, they cannot drag.
            if (this.getNodeByCode(got.code) != null) {
                return false;
            }

            // If we are abstract, and one of our children is on the graph, they cannot drag.
            if (got.isAbstract) {
                let isChildOnGraph = false;

                this.geoObjectTypes.forEach((child: GeoObjectType) => {
                    if (child.superTypeCode === got.code) {
                        if (this.getNodeByCode(child.code) != null) {
                            isChildOnGraph = true;
                        }
                    }
                });

                if (isChildOnGraph) {
                    return false;
                }
            }
            // If we are a child of an abstract type, and our abstract type is on the graph, we cannot drag.
            else if (got.superTypeCode != null) {
                if (this.getNodeByCode(got.superTypeCode) != null) {
                    return false;
                }
            }
        } else {
            // If there is no selected hierarchy, they cannot drag.
            return false;
        }

        return true;
    }

    calculateRelatedHierarchies(got: GeoObjectType): string[] {
        let relatedHiers = [];

        for (let i = 0; i < this.hierarchies.length; ++i) {
            let hierarchyType = this.hierarchies[i];

            if (hierarchyType.rootGeoObjectTypes != null && hierarchyType.rootGeoObjectTypes.length > 0) {
                let d3Hierarchy = d3.hierarchy(hierarchyType.rootGeoObjectTypes[0]).descendants();

                let found = d3Hierarchy.find((node) => {
                    return node.data.geoObjectType === got.code && node.data.inheritedHierarchyCode == null;
                });

                if (found) {
                    relatedHiers.push(hierarchyType.code);
                }
            }
        }

        return relatedHiers;
    }



    handleDrag(event: { dragEl: Element, dropEl: Element, event: any }): void {
        for (let i = 0; i < this.dropTargets.length; ++i) {
            this.dropTargets[i].onDrag(event.dragEl, event.dropEl, event.event);
        }
    }

    handleDrop(event: { dragEl: Element, event: any }): void {
        for (let i = 0; i < this.dropTargets.length; ++i) {
            this.dropTargets[i].onDrop(event.dragEl, event.event);
        }
    }

    findGeoObjectTypeByCode(code: string): GeoObjectType {
        return this.geoObjectTypes.find(t => t.code === code);
    }

    findHierarchyByCode(code: string): HierarchyType {
        return this.hierarchies.find(t => t.code === code);
    }

    findOrganizationByCode(code: string): Organization {
        return this.organizations.find(t => t.code === code);
    }

    isRA(): boolean {
        return this.authService.isRA();
    }

    isOrganizationRA(orgCode: string, dropZone: boolean = false): boolean {
        return this.isSRA || this.authService.isOrganizationRA(orgCode);
    }

    canEdit(): boolean {
        return this.selection != null && this.selection.action === Action.EDIT && (
            this.authService.isSRA() || this.authService.isOrganizationRA(this.selection.type.organizationCode)
        );
    }

    getTypesByOrg(org: Organization): GeoObjectType[] {
        let orgTypes: GeoObjectType[] = [];

        for (let i = 0; i < this.geoObjectTypes.length; ++i) {
            let geoObjectType: GeoObjectType = this.geoObjectTypes[i];

            if (geoObjectType.organizationCode === org.code) {
                orgTypes.push(geoObjectType);
            }
        }

        return orgTypes;
    }

    getHierarchiesByOrg(org: Organization): HierarchyType[] {
        let orgHierarchies: HierarchyType[] = [];

        for (let i = 0; i < this.hierarchies.length; ++i) {
            let hierarchy: HierarchyType = this.hierarchies[i];

            if (hierarchy.organizationCode === org.code) {
                orgHierarchies.push(hierarchy);
            }
        }

        return orgHierarchies;
    }


    private processHierarchyNodes(node: HierarchyNode) {
        if (node != null) {
            node.label = this.getHierarchyLabel(node.geoObjectType);

            node.children.forEach(child => {
                this.processHierarchyNodes(child);
            });
        }
    }

    private getHierarchyLabel(geoObjectTypeCode: string): string {
        let label: string = null;
        this.geoObjectTypes.forEach(function (gOT) {
            if (gOT.code === geoObjectTypeCode) {
                label = gOT.label.localizedValue;
            }
        });

        return label;
    }

    handleRemoveFromHierarchy(parentGotCode: string, gotCode: string, errCallback: (err: HttpErrorResponse) => void = null): void {

        this.hierarchyService.removeFromHierarchy(this.selection.type.code, parentGotCode, gotCode).then(hierarchyType => {

            const geoObjectType = this.findGeoObjectTypeByCode(gotCode);

            const index = geoObjectType.relatedHierarchies.findIndex(t => t === hierarchyType.code);

            if (index != -1) {

                const got = lodash.cloneDeep(geoObjectType);
                got.relatedHierarchies.splice(index, 1);

                this.onGeoObjectType.emit(got);
            }

            this.refreshPrimaryHierarchy(hierarchyType);
        }).catch((err: HttpErrorResponse) => {
            if (errCallback != null) {
                errCallback(err);
            }

            this.error(err);
        });
    }


    handleAddChild(hierarchyCode: string, parentGeoObjectTypeCode: string, childGeoObjectTypeCode: string): void {
        this.hierarchyService.addChildToHierarchy(hierarchyCode, parentGeoObjectTypeCode, childGeoObjectTypeCode).then((ht: HierarchyType) => {
            const geoObjectType = this.findGeoObjectTypeByCode(childGeoObjectTypeCode);

            const index = geoObjectType.relatedHierarchies.findIndex(t => t === hierarchyCode);

            if (index == -1) {

                const got = lodash.cloneDeep(geoObjectType);
                got.relatedHierarchies.push(hierarchyCode);

                this.onGeoObjectType.emit(got);
            }

            this.refreshPrimaryHierarchy(ht);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    handleInsertBetweenTypes(hierarchyCode: string, parentGeoObjectTypeCode: string, middleGeoObjectTypeCode: string, youngestGeoObjectTypeCode: string): void {
        this.hierarchyService.insertBetweenTypes(hierarchyCode, parentGeoObjectTypeCode, middleGeoObjectTypeCode, youngestGeoObjectTypeCode).then((ht: HierarchyType) => {
            this.refreshPrimaryHierarchy(ht);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    handleCreateHierarchy(organizationCode: string): void {
        this.setSelection({
            action: Action.CREATE,
            type: {
                code: "",
                description: this.localizeService.create(),
                label: this.localizeService.create(),
                rootGeoObjectTypes: [],
                organizationCode: organizationCode
            },
        });
    }

    handleDeleteHierarchyType(obj: HierarchyType): void {
        const bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        bsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + " [" + obj.label.localizedValue + "]";
        bsModalRef.content.data = obj.code;
        bsModalRef.content.type = "DANGER";
        bsModalRef.content.submitText = this.localizeService.decode("modal.button.delete");

        bsModalRef.content.onConfirm.subscribe(data => {
            this.removeHierarchyType(data);
        });
    }

    handleEditHierarchyType(obj: HierarchyType, readOnly: boolean): void {

        this.setSelection({
            action: readOnly ? Action.VIEW : Action.EDIT,
            type: lodash.cloneDeep(obj),
        });
    }

    handleTypeChange(event: { edit: boolean, hierarchy: HierarchyType }): void {
        if (event != null) {

            const bsModalRef = this.modalService.show(ConfirmModalComponent, {
                animated: true,
                backdrop: true,
                ignoreBackdropClick: true
            });
            bsModalRef.content.message = "You have unsaved changes";
            bsModalRef.content.submitText = "Save changes and close"

            bsModalRef.content.onConfirm.subscribe(data => {

                if (event.edit) {
                    this.hierarchyService.updateHierarchyType(event.hierarchy).then(type => {
                        this.onHierarchyType.emit(type);

                        this.setSelection({
                            action: Action.VIEW,
                            type: type,
                        });
                    }).catch((err: HttpErrorResponse) => {
                        this.error(err);
                    });
                } else {
                    this.hierarchyService.createHierarchyType(event.hierarchy).then(type => {
                        this.onHierarchyType.emit(type);

                        this.setSelection({
                            action: Action.VIEW,
                            type: type,
                        });
                    }).catch((err: HttpErrorResponse) => {
                        this.error(err);
                    });
                }
            });

        }
        else {
            this.setSelection({
                action: Action.VIEW,
                type: this.selection.type,
            });
        }
    }

    handleTypeView(hierarchyType: HierarchyType): void {

        this.setSelection({
            type: lodash.cloneDeep(hierarchyType),
            action: Action.VIEW
        });
    }

    updateGeoObjectTypeStatus(): void {

        if (this.typesByOrg != null) {
            // Update the can drag options
            if (this.selection == null || this.selection.type.rootGeoObjectTypes == null || this.selection.type.rootGeoObjectTypes.length == 0) {

                let canDrag = (this.authService.isSRA() || this.authService.isOrganizationRA(this.selection.type.organizationCode));

                this.typesByOrg.forEach(org => org.types.forEach(t => t.canDrag = canDrag));
            } else {
                this.typesByOrg.forEach(org => org.types.forEach(t => {
                    t.canDrag = this.calculateCanDrag(t)
                    t.relatedHierarchies = this.calculateRelatedHierarchies(t);
                }));
            }
        }
    }

    setSelection(selection: Selection): void {
        this.selection = selection;

        this.updateGeoObjectTypeStatus();

        if (this.tab === Tab.HIERARCHY) {
            this.renderTree();
        }
    }

    isPrimaryHierarchy(hierarchy: HierarchyType): boolean {
        return this.selection != null && hierarchy.code === this.selection.type.code;
    }

    removeHierarchyType(code: string): void {
        this.hierarchyService.deleteHierarchyType(code).then(response => {
            this.onRemove.emit(code);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }


    refreshPrimaryHierarchy(hierarchyType: HierarchyType) {
        this.processHierarchyNodes(hierarchyType.rootGeoObjectTypes[0]);

        this.selection.type = hierarchyType;

        this.updateGeoObjectTypeStatus();

        this.renderTree();

        this.onHierarchyType.emit(hierarchyType);
    }


    onFilterChange(): void {
        const label = this.filter.toLowerCase();

        this.filteredHierarchiesByOrg = [];

        this.hierarchiesByOrg.forEach((item: { org: Organization, hierarchies: HierarchyType[] }) => {
            const filtered = item.hierarchies.filter((hierarchy: HierarchyType) => {
                const index = hierarchy.label.localizedValue.toLowerCase().indexOf(label);

                return (index !== -1);
            });

            this.filteredHierarchiesByOrg.push({ org: item.org, hierarchies: filtered });
        });
    }

    handleInheritHierarchy(hierarchyTypeCode: string, inheritedHierarchyTypeCode: string, geoObjectTypeCode: string) {
        this.hierarchyService.setInheritedHierarchy(hierarchyTypeCode, inheritedHierarchyTypeCode, geoObjectTypeCode).then((ht: HierarchyType) => {
            this.refreshPrimaryHierarchy(ht);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    handleUninheritHierarchy(hierarchyTypeCode: string, geoObjectTypeCode: string) {
        this.hierarchyService.removeInheritedHierarchy(hierarchyTypeCode, geoObjectTypeCode).then((ht: HierarchyType) => {
            this.refreshPrimaryHierarchy(ht);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }


    onImportHistory(type: HierarchyType): void {
        this.registryService.getImportHistory('HierarchyType', type.code).then(histories => {
            const bsModalRef = this.modalService.show(ImportHistoryModalComponent, {
                animated: true,
                backdrop: true,
                ignoreBackdropClick: true
            });
            bsModalRef.content.init(type.label, histories);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }



    renderTree(): void {
        console.log('Render Tree')

        // Remove existing tree
        if (this.selection == null || this.selection.type.rootGeoObjectTypes == null || this.selection.type.rootGeoObjectTypes.length == 0) {
            d3.select("#svg").remove();
        }

        d3.select(".g-context-menu").remove();
        d3.select(".hierarchy-inherit-button").remove();
        d3.select(".g-hierarchy-got-connector").remove();

        let overflowDiv: any = d3.select("#overflow-div").node();
        let scrollLeft = overflowDiv.scrollLeft;
        let scrollRight = overflowDiv.scrollRight;

        let svg = d3.select("#svg");

        if (svg.node() == null) {
            svg = d3.select("#svgHolder").append("svg");
            svg.attr("id", "svg");
            // Define zoom behavior
            // const zoom = d3.zoom()
            //     .scaleExtent([0.5, 5]) // Min and max zoom scale
            //     .on("zoom", (event) => {
            //         svg.attr("transform", event.transform); // Apply zoom/pan transform
            //     });

            // // Attach zoom behavior to SVG
            // svg.call(zoom)
            //     .on("wheel.zoom", null) // Remove default wheel handler
            //     .on("wheel", (event) => {
            //         event.preventDefault(); // Prevent page scroll
            //         svg.call(zoom.scaleBy, event.deltaY < 0 ? 1.1 : 0.9); // Zoom in/out
            //     });
        }

        this.primarySvgHierarchy = new SvgHierarchyType(this, svg, this.selection.type, true, this.localizeService, this.modalService, this.authService);
        this.primarySvgHierarchy.render();

        this.calculateSvgViewBox();

        let overflowDiv2: any = d3.select("#overflow-div").node();
        overflowDiv2.scrollLeft = scrollLeft;
        overflowDiv2.scrollRight = scrollRight;
    }

    calculateSvgViewBox(): void {
        let svg: any = d3.select("#svg");
        let svgNode: any = svg.node();

        let { x, y, width, height } = svgNode.getBBox();

        const xPadding = 30;
        const yPadding = 40;
        svg.attr("viewBox", (x - xPadding) + " " + (y - yPadding) + " " + (width + xPadding * 2) + " " + (height + 200 + yPadding * 2 ));

        width = (width + xPadding * 2) * TREE_SCALE_FACTOR_X;
        height = (height + 200 + yPadding * 2) * TREE_SCALE_FACTOR_Y;

        d3.select("#svgHolder").style("width", width + "px");
    }

    private registerDragHandlers(): any {
        let that = this;

        this.dropTargets = [];

        // Empty Hierarchy Drop Zone
        this.dropTargets.push({
            dropSelector: ".drop-box-container",
            onDrag: function (dragEl: Element, dropEl: Element) {
                if (this.dropEl != null) {
                    this.dropEl.style("border-color", null);
                    this.dropEl = null;
                }

                if (dropEl != null) {
                    let emptyHierarchyDropZone = dropEl.closest(".drop-box-container");

                    if (emptyHierarchyDropZone != null) {
                        this.dropEl = d3.select(emptyHierarchyDropZone).style("border-color", "#6BA542");
                    }
                }
            },
            onDrop: function (dragEl: Element) {
                if (this.dropEl != null) {
                    this.dropEl.style("border-color", null);
                    that.handleAddChild(that.selection.type.code, "ROOT", d3.select(dragEl).attr("id"));
                    this.dropEl = null;
                }
            }
        });

        // SVG GeoObjectType Drop Zone
        this.dropTargets.push({
            dropSelector: ".svg-got-body-rect",
            onDrag: function (dragEl: Element, mouseTarget: Element, event: any) {
                this.clearDropZones();

                let lastDropEl = this.dropEl;

                // translate page to SVG co-ordinate
                let svg: any = d3.select("#svg").node();

                if (svg == null) {
                    return;
                }
                let svgMousePoint = svgPoint(event.sourceEvent.pageX, event.sourceEvent.pageY);

                // Find out if we've dragged the GeoObjectType inside of a HierarchyNode. If we have, then
                // we need to expand the HierarchyNode's BoundingBox to accomodate our new drop zones.
                that.primarySvgHierarchy.getD3Tree().descendants().forEach((node: any) => {
                    if (node.data.geoObjectType !== "GhostNode" && isPointWithin(svgMousePoint, node.data.dropZoneBbox)) {
                        this.dropEl = d3.select(".g-hierarchy[data-primary=true] .svg-got-body-rect[data-gotCode=\"" + node.data.geoObjectType + "\"]");
                        node.data.activeDropZones = true;

                        if (node.parent == null) {
                            node.data.dropZoneBbox = { x: node.x - SvgHierarchyType.gotRectW / 2, y: node.y - SvgHierarchyType.gotRectH * 2, width: SvgHierarchyType.gotRectW, height: SvgHierarchyType.gotRectH * 4 };
                        }
                    } else {
                        node.data.activeDropZones = false;

                        if (node.parent == null) {
                            node.data.dropZoneBbox = { x: node.x - SvgHierarchyType.gotRectW / 2, y: node.y - SvgHierarchyType.gotRectH / 2, width: SvgHierarchyType.gotRectW, height: SvgHierarchyType.gotRectH };
                        }
                    }
                });

                if (this.dropEl == null || (lastDropEl != null && this.dropEl != null && lastDropEl.attr("data-gotCode") != this.dropEl.attr("data-gotCode"))) {
                    this.clearGhostNodes(true);
                }

                if (this.dropEl != null) {
                    let isDragGroup = d3.select(dragEl).classed("got-group-parent");
                    const gotCode = this.dropEl.attr("data-gotCode");
                    let dropNode = that.primarySvgHierarchy.getD3Tree().find((node) => { return node.data.geoObjectType === gotCode; });
                    let isDropGroup = that.findGeoObjectTypeByCode(gotCode).isAbstract;

                    this.dropEl.attr("stroke", "blue");

                    const dropElX = parseInt(this.dropEl.attr("x"));
                    const dropElY = parseInt(this.dropEl.attr("y"));

                    // Add drop zones
                    const childW: number = SvgHierarchyType.gotRectW;
                    const childH: number = SvgHierarchyType.gotRectH;

                    let dzg = d3.select("#svg").append("g").classed("svg-dropZone-g", true);

                    // Render Child Drop Zone
                    let dropTargetHasChildren = !(dropNode.children == null || dropNode.children.length == 0);
                    let isChildDZActive = !isDropGroup && (!isDragGroup || !dropTargetHasChildren);
                    if (isChildDZActive) {
                        this.childDzBacker = dzg.append("rect").classed("svg-got-child-dz-backer", true)
                            .attr("x", dropElX + (SvgHierarchyType.gotRectW / 2) - (childW / 2))
                            .attr("y", dropElY + SvgHierarchyType.gotRectH + 10)
                            .attr("width", childW)
                            .attr("height", childH)
                            .attr("fill", "white");

                        this.childDz = dzg.append("rect").classed("svg-got-child-dz", true)
                            .attr("x", dropElX + (SvgHierarchyType.gotRectW / 2) - (childW / 2))
                            .attr("y", dropElY + SvgHierarchyType.gotRectH + 10)
                            .attr("width", childW)
                            .attr("height", childH)
                            .attr("fill", "none")
                            .attr("stroke", "black")
                            .attr("stroke-width", "1")
                            .attr("stroke-dasharray", "5,5");

                        let addChildLabel = dropTargetHasChildren ? that.localizeService.decode("hierarchy.content.intersectChild") : that.localizeService.decode("hierarchy.content.addChild");
                        this.childDzText = dzg.append("text").classed("svg-got-child-dz-text", true)
                            .attr("font-family", "sans-serif")
                            .attr("font-size", 10)
                            .attr("fill", "black")
                            .attr("x", dropElX + (SvgHierarchyType.gotRectW / 2) - calculateTextWidth(addChildLabel, 10) / 2)
                            .attr("y", dropElY + SvgHierarchyType.gotRectH + 10 + childH / 2 + 2)
                            .text(addChildLabel);
                    }

                    // Render Parent Drop Zone
                    if (!isDragGroup) // Don't render it if we're a group
                    {
                        this.parentDzBacker = dzg.append("rect").classed("svg-got-parent-dz-backer", true)
                            .attr("x", dropElX + (SvgHierarchyType.gotRectW / 2) - (childW / 2))
                            .attr("y", dropElY - SvgHierarchyType.gotHeaderH - childH)
                            .attr("width", childW)
                            .attr("height", childH)
                            .attr("fill", "white");

                        this.parentDz = dzg.append("rect").classed("svg-got-parent-dz", true)
                            .attr("x", dropElX + (SvgHierarchyType.gotRectW / 2) - (childW / 2))
                            .attr("y", dropElY - SvgHierarchyType.gotHeaderH - childH)
                            .attr("width", childW)
                            .attr("height", childH)
                            .attr("fill", "none")
                            .attr("stroke", "black")
                            .attr("stroke-width", "1")
                            .attr("stroke-dasharray", "5,5");

                        d3.select(".svg-got-parent-dz-text").remove();
                        let addParentLabel = dropNode.parent == null ? that.localizeService.decode("hierarchy.content.addParent") : that.localizeService.decode("hierarchy.content.intersectParent");
                        this.parentDzText = dzg.append("text").classed("svg-got-parent-dz-text", true)
                            .attr("font-family", "sans-serif")
                            .attr("font-size", 10)
                            .attr("fill", "black")
                            .attr("x", dropElX + (SvgHierarchyType.gotRectW / 2) - calculateTextWidth(addParentLabel, 10) / 2)
                            .attr("y", dropElY - SvgHierarchyType.gotHeaderH - childH / 2 + 2)
                            .text(addParentLabel);
                    }

                    // Render Sibling Drop Zone
                    if (this.ghostCode != gotCode) {
                        if (this.ghostCode != null) {
                            this.clearGhostNodes(dropNode.parent == null);
                        }

                        if (dropNode.parent != null) {
                            let parentIndex = null;
                            for (let i = 0; i < dropNode.parent.data.children.length; ++i) {
                                let hn: any = dropNode.parent.data.children[i];

                                if (hn.geoObjectType === gotCode) {
                                    parentIndex = i + 1;
                                }
                            }

                            let addSiblingLabel = that.localizeService.decode("hierarchy.content.addChild");
                            dropNode.parent.data.children.splice(parentIndex, 0, { ghostingCode: gotCode, geoObjectType: "GhostNode", label: addSiblingLabel, children: [] });

                            that.renderTree();
                            this.ghostCode = gotCode;
                        }
                    }

                    let siblingGhostBody = d3.select(".svg-sibling-ghost-body-dz");

                    if (!isDragGroup && isPointWithin(svgMousePoint, getBboxFromSelection(this.parentDz))) {
                        this.parentDz.attr("stroke", "blue");
                        this.parentDzText.attr("fill", "blue");
                        isChildDZActive && this.childDz.attr("stroke", "black");
                        isChildDZActive && this.childDzText.attr("fill", "black");
                        siblingGhostBody.attr("stroke", "black");
                        this.activeDz = this.parentDz;
                    } else if (isChildDZActive && isPointWithin(svgMousePoint, getBboxFromSelection(this.childDz))) {
                        !isDragGroup && this.parentDz.attr("stroke", "black");
                        !isDragGroup && this.parentDzText.attr("fill", "black");
                        this.childDz.attr("stroke", "blue");
                        this.childDzText.attr("fill", "blue");
                        siblingGhostBody.attr("stroke", "black");
                        this.activeDz = this.childDz;
                    } else if (siblingGhostBody.node() != null && isPointWithin(svgMousePoint, getBboxFromSelection(siblingGhostBody))) {
                        !isDragGroup && this.parentDz.attr("stroke", "black");
                        !isDragGroup && this.parentDzText.attr("fill", "black");
                        isChildDZActive && this.childDz.attr("stroke", "black");
                        isChildDZActive && this.childDzText.attr("fill", "black");
                        siblingGhostBody.attr("stroke", "blue");
                        this.activeDz = "sibling";
                    }
                }
            },
            onDrop: function (dragEl: Element) {
                if (this.dropEl != null && this.activeDz != null) {
                    let dropGot = this.dropEl.attr("data-gotCode");
                    let dropNode = that.primarySvgHierarchy.getD3Tree().find((node) => { return node.data.geoObjectType === dropGot; });
                    let dragGot = d3.select(dragEl).attr("id");

                    if (this.activeDz === this.childDz) {
                        if (dropNode.data.children.length == 0) {
                            that.handleAddChild(that.selection.type.code, dropGot, dragGot);
                        } else {
                            let youngest = "";

                            for (let i = 0; i < dropNode.data.children.length; ++i) {
                                youngest = youngest + dropNode.data.children[i].geoObjectType;

                                if (i < dropNode.data.children.length - 1) {
                                    youngest = youngest + ",";
                                }
                            }

                            that.handleInsertBetweenTypes(that.selection.type.code, dropGot, dragGot, youngest);
                        }
                    } else if (this.activeDz === this.parentDz) {
                        if (dropNode.parent == null) {
                            that.handleInsertBetweenTypes(that.selection.type.code, "ROOT", dragGot, dropGot);
                        } else {
                            that.handleInsertBetweenTypes(that.selection.type.code, dropNode.parent.data.geoObjectType, dragGot, dropGot);
                        }
                    } else if (this.activeDz === "sibling") {
                        that.handleAddChild(that.selection.type.code, dropNode.parent.data.geoObjectType, d3.select(dragEl).attr("id"));
                    }
                }
                this.clearDropZones();
                this.clearGhostNodes(true);
            },
            clearDropZones: function () {
                if (this.dropEl != null) {
                    this.dropEl.attr("stroke", null);
                }

                this.dropEl = null;
                this.activeDz = null;

                this.childDz = null;
                this.parentDz = null;

                d3.select(".svg-dropZone-g").remove();
            },
            clearGhostNodes: function (renderTree: boolean) {
                if (this.ghostCode != null) {
                    let ghostNode = that.primarySvgHierarchy.getD3Tree().find((node) => { return node.data.ghostingCode === this.ghostCode; });

                    if (ghostNode != null) {
                        let parentIndex = null;
                        for (let i = 0; i < ghostNode.parent.data.children.length; ++i) {
                            let hn: any = ghostNode.parent.data.children[i];

                            if (hn.ghostingCode === this.ghostCode) {
                                parentIndex = i;
                            }
                        }

                        if (parentIndex != null) {
                            ghostNode.parent.data.children.splice(parentIndex, 1);
                            if (renderTree) {
                                that.renderTree();
                            }
                        }
                    }

                    this.ghostCode = null;
                }
            }
        });

    }

    public error(err: HttpErrorResponse): void {
        this.onError.emit(err);
    }

}
