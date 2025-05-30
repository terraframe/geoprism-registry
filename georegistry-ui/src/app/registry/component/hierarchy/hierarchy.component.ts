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

import { Component, OnInit, ViewChild, ElementRef } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { debounceTime, distinctUntilChanged, filter, tap } from "rxjs/operators";
import { fromEvent } from "rxjs";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import * as d3 from "d3";

import { CreateHierarchyTypeModalComponent } from "./modals/create-hierarchy-type-modal.component";
import { CreateGeoObjTypeModalComponent } from "./modals/create-geoobjtype-modal.component";
import { ManageGeoObjectTypeModalComponent } from "./modals/manage-geoobjecttype-modal.component";

import { ErrorHandler, ConfirmModalComponent } from "@shared/component";
import { LocalizationService, AuthService } from "@shared/service";
import { ModalTypes } from "@shared/model/modal";

import { HierarchyType, HierarchyNode } from "@registry/model/hierarchy";
import { GeoObjectType } from "@registry/model/registry";
import { Organization } from "@shared/model/core";
import { RegistryService, HierarchyService } from "@registry/service";

import { SvgHierarchyType } from "./d3/svg-hierarchy-type";
import { svgPoint, isPointWithin, calculateTextWidth, getBboxFromSelection } from "./d3/svg-util";
import { SvgHierarchyNode } from "./d3/svg-hierarchy-node";
import { ImportTypesModalComponent } from "./modals/import-types-modal.component";
import Utils from "@registry/utility/Utils";
import { ExportTypesModalComponent } from "./modals/export-types-modal.component";
import { environment } from "src/environments/environment";
import { RDFExportModalComponent } from "./modals/rdf-export-modal.component";

export const TREE_SCALE_FACTOR_X: number = 1.8;
export const TREE_SCALE_FACTOR_Y: number = 1.8;

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

@Component({

    selector: "hierarchies",
    templateUrl: "./hierarchy.component.html",
    styleUrls: ["./hierarchy.css"]
})
export class HierarchyComponent implements OnInit {

    userOrganization: string = null;

    primarySvgHierarchy: SvgHierarchyType;
    currentHierarchy: HierarchyType = null;

    instance: Instance = new Instance();
    hierarchies: HierarchyType[];
    organizations: Organization[];
    geoObjectTypes: GeoObjectType[] = [];

    hierarchiesByOrg: { org: Organization, hierarchies: HierarchyType[] }[] = [];
    typesByOrg: { org: Organization, types: GeoObjectType[] }[] = [];

    filter: string = "";
    filteredHierarchiesByOrg: { org: Organization, hierarchies: HierarchyType[] }[] = [];
    filteredTypesByOrg: { org: Organization, types: GeoObjectType[] }[] = [];
    @ViewChild("searchInput", { static: true }) searchInput: ElementRef;

    hierarchyTypeDeleteExclusions: string[] = ["AllowedIn", "IsARelationship"];
    geoObjectTypeDeleteExclusions: string[] = ["ROOT"];

    _opened: boolean = false;

    /*
     * Reference to the modal current showing
    */
    private bsModalRef: BsModalRef;

    /*
     * Currently clicked on id for delete confirmation modal
     */
    current: any;

    isSRA: boolean = false;

    hierarchyService: HierarchyService;

    localizeService: LocalizationService;

    constructor(hierarchyService: HierarchyService, private modalService: BsModalService,
        localizeService: LocalizationService, private registryService: RegistryService, private authService: AuthService) {
        this.isSRA = authService.isSRA();

        this.hierarchyService = hierarchyService;
        this.localizeService = localizeService;
    }

    ngOnInit(): void {
        this.refreshAll(null);

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
    }

    localize(key: string): string {
        return this.localizeService.decode(key);
    }

    private renderTree(): void {
        if (this.currentHierarchy == null || this.currentHierarchy.rootGeoObjectTypes == null || this.currentHierarchy.rootGeoObjectTypes.length == 0) {
            d3.select("#svg").remove();

            let canDrag = false;
            if (this.currentHierarchy != null) {
                canDrag = (this.authService.isSRA() || this.authService.isOrganizationRA(this.currentHierarchy.organizationCode));
            }

            this.geoObjectTypes.forEach((got: GeoObjectType) => {
                got.canDrag = canDrag;
            });
            return;
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
        }

        this.primarySvgHierarchy = new SvgHierarchyType(this, svg, this.currentHierarchy, true, this.localizeService, this.modalService, this.authService);
        this.primarySvgHierarchy.render();

        this.calculateSvgViewBox();

        let overflowDiv2: any = d3.select("#overflow-div").node();
        overflowDiv2.scrollLeft = scrollLeft;
        overflowDiv2.scrollRight = scrollRight;

        // this.registerSvgHandlers();

        this.geoObjectTypes.forEach((got: GeoObjectType) => {
            got.canDrag = this.calculateCanDrag(got);
        });
    }

    calculateSvgViewBox(): void {
        let svg: any = d3.select("#svg");
        let svgNode: any = svg.node();

        let { x, y, width, height } = svgNode.getBBox();

        const xPadding = 30;
        const yPadding = 40;
        svg.attr("viewBox", (x - xPadding) + " " + (y - yPadding) + " " + (width + xPadding * 2) + " " + (height + yPadding * 2));

        width = (width + xPadding * 2) * TREE_SCALE_FACTOR_X;
        height = (height + yPadding * 2) * TREE_SCALE_FACTOR_Y;

        d3.select("#svgHolder").style("width", width + "px");
        // d3.select("#svgHolder").style("height", height + "px");
    }

    calculateCanDrag(got: GeoObjectType): boolean {
        let hierarchyComponent = this;

        if (this.primarySvgHierarchy != null) {
            // Check permissions against GOT and Hierarchy org
            if (!(this.authService.isSRA() || this.authService.isOrganizationRA(this.currentHierarchy.organizationCode))) {
                return false;
            }

            // If the child is already on the graph, they cannot drag.
            if (this.primarySvgHierarchy.getNodeByCode(got.code) != null) {
                return false;
            }

            // If we are abstract, and one of our children is on the graph, they cannot drag.
            if (got.isAbstract) {
                let isChildOnGraph = false;

                this.geoObjectTypes.forEach((child: GeoObjectType) => {
                    if (child.superTypeCode === got.code) {
                        if (hierarchyComponent.primarySvgHierarchy.getNodeByCode(child.code) != null) {
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
                if (hierarchyComponent.primarySvgHierarchy.getNodeByCode(got.superTypeCode) != null) {
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

    private registerDragHandlers(): any {
        let that = this;

        let dropTargets: DropTarget[] = [];

        // Empty Hierarchy Drop Zone
        dropTargets.push({
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
                    that.addChild(that.currentHierarchy.code, "ROOT", d3.select(dragEl).attr("id"));
                    this.dropEl = null;
                }
            }
        });

        // SVG GeoObjectType Drop Zone
        dropTargets.push({
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
                            that.addChild(that.currentHierarchy.code, dropGot, dragGot);
                        } else {
                            let youngest = "";

                            for (let i = 0; i < dropNode.data.children.length; ++i) {
                                youngest = youngest + dropNode.data.children[i].geoObjectType;

                                if (i < dropNode.data.children.length - 1) {
                                    youngest = youngest + ",";
                                }
                            }

                            that.insertBetweenTypes(that.currentHierarchy.code, dropGot, dragGot, youngest);
                        }
                    } else if (this.activeDz === this.parentDz) {
                        if (dropNode.parent == null) {
                            that.insertBetweenTypes(that.currentHierarchy.code, "ROOT", dragGot, dropGot);
                        } else {
                            that.insertBetweenTypes(that.currentHierarchy.code, dropNode.parent.data.geoObjectType, dragGot, dropGot);
                        }
                    } else if (this.activeDz === "sibling") {
                        that.addChild(that.currentHierarchy.code, dropNode.parent.data.geoObjectType, d3.select(dragEl).attr("id"));
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

        // GeoObjectTypes and Hierarchies
        let deltaX: number, deltaY: number, width: number;
        let sidebarDragHandler = d3.drag()
            .on("start", function (event: any) {
                let canDrag = d3.select(this).attr("data-candrag");
                if (canDrag === "false") {
                    return;
                }

                let rect = this.getBoundingClientRect();
                deltaX = rect.left - event.sourceEvent.pageX;
                deltaY = rect.top - event.sourceEvent.pageY;
                width = rect.width;
            })
            .on("drag", function (event: any) {
                let canDrag = d3.select(this).attr("data-candrag");
                if (canDrag === "false") {
                    return;
                }

                d3.select(".g-context-menu").remove();

                let selThis = d3.select(this);

                // Kind of a dumb hack, but if we hide our drag element for a sec, then we can check what's underneath it.
                selThis.style("display", "none");

                let target = document.elementFromPoint(event.sourceEvent.pageX, event.sourceEvent.pageY);

                selThis.style("display", null);

                for (let i = 0; i < dropTargets.length; ++i) {
                    dropTargets[i].onDrag(this, target, event);
                }

                // Move the GeoObjectType with the pointer when they move their mouse
                selThis
                    .classed("dragging", true)
                    .style("left", (event.sourceEvent.pageX + deltaX) + "px")
                    .style("top", (event.sourceEvent.pageY + deltaY) + "px")
                    .style("width", width + "px");

                // If they are moving a GOT group then we have to move the children as well
                if (selThis.classed("got-group-parent")) {
                    let index = 1;
                    d3.selectAll(".got-group-child[data-superTypeCode=\"" + selThis.attr("id") + "\"]").each(function () {
                        let li: any = this;
                        let child = d3.select(li);

                        child
                            .classed("dragging", true)
                            .style("left", (event.sourceEvent.pageX + deltaX) + "px")
                            .style("top", (event.sourceEvent.pageY + deltaY + (li.getBoundingClientRect().height + 2) * index) + "px")
                            .style("width", width + "px");

                        index++;
                    });
                }
            }).on("end", function (event: any) {
                let selThis = d3.select(this)
                    .classed("dragging", false)
                    .style("left", null)
                    .style("top", null)
                    .style("width", null);

                // If they are moving a GOT group then we have to reset the children as well
                if (selThis.classed("got-group-parent")) {
                    d3.selectAll(".got-group-child[data-superTypeCode=\"" + selThis.attr("id") + "\"]").each(function () {
                        let child = d3.select(this);

                        child
                            .classed("dragging", false)
                            .style("left", null)
                            .style("top", null)
                            .style("width", null);
                    });
                }

                for (let i = 0; i < dropTargets.length; ++i) {
                    dropTargets[i].onDrop(this, event);
                }
            });

        sidebarDragHandler(d3.selectAll(".sidebar-section-content ul.list-group li.got-li-item"));
    }

    private registerSvgHandlers(): void {
        let hierarchyComponent = this;

        // SVG Drag Handler
        let deltaX: number, deltaY: number, width: number;
        let startPoint: any;
        let svgGot: SvgHierarchyNode;
        let svgDragHandler = d3.drag()
            .on("start", function (event: any) {
                let svgMousePoint: any = svgPoint(event.sourceEvent.pageX, event.sourceEvent.pageY);
                // let select = d3.select(this);

                svgGot = hierarchyComponent.primarySvgHierarchy.getNodeByCode(d3.select(this).attr("data-gotCode"));

                // d3.selectAll(".svg-got-relatedhiers-button").sort(function (a: any, b: any) {
                //   if (a.data.geoObjectType !== event.subject.data.geoObjectType) {
                //     return -1
                //   }
                //   else {
                //     return 1
                //   }
                // });

                //   d3.selectAll(".svg-got-body-rect").sort(function (a: any, b: any) {
                //   if (a.data.geoObjectType !== event.subject.data.geoObjectType) {
                //     return -1
                //   }
                //   else {
                //     return 1
                //   }
                // });

                // d3.selectAll(".svg-got-header-rect").sort(function (a: any, b: any) {
                //   if (a.data.geoObjectType !== event.subject.data.geoObjectType) {
                //     console.log("no --> ",a.data.geoObjectType)
                //     return -1
                //   }
                //   else {
                //     console.log("yes --> ",a.data.geoObjectType)
                //     return 1
                //   }
                // });

                startPoint = svgGot.getPos();

                deltaX = startPoint.x - svgMousePoint.x;
                deltaY = startPoint.y - svgMousePoint.y;
            })
            .on("drag", function (event: any) {
                d3.select(".g-context-menu").remove();

                let svgMousePoint = svgPoint(event.sourceEvent.pageX, event.sourceEvent.pageY);

                svgGot = hierarchyComponent.primarySvgHierarchy.getNodeByCode(d3.select(this).attr("data-gotCode"));

                svgGot.setPos(svgMousePoint.x + deltaX, svgMousePoint.y + deltaY, true);
            }).on("end", function (event: any) {
                let bbox: string[] = d3.select("#svg").attr("viewBox").split(" ");

                svgGot.setPos(startPoint.x, startPoint.y, false);

                // if (!isBboxPartiallyWithin(svgGot.getBbox(), { x: parseInt(bbox[0]), y: parseInt(bbox[1]), width: parseInt(bbox[2]), height: parseInt(bbox[3]) })) {

                //   if (hierarchyComponent.isOrganizationRA(hierarchyComponent.currentHierarchy.organizationCode)) {
                //     let obj = hierarchyComponent.findGeoObjectTypeByCode(svgGot.getCode());

                //     hierarchyComponent.bsModalRef = hierarchyComponent.modalService.show(ConfirmModalComponent, {
                //       animated: true,
                //       backdrop: true,
                //       ignoreBackdropClick: true,
                //     });

                //     let message = hierarchyComponent.localizeService.decode("confirm.modal.verify.remove.hierarchy");
                //     message = message.replace("{label}", obj.label.localizedValue);

                //     hierarchyComponent.bsModalRef.content.message = message;
                //     hierarchyComponent.bsModalRef.content.data = obj.code;

                //     (<ConfirmModalComponent>hierarchyComponent.bsModalRef.content).onConfirm.subscribe(data => {
                //       let treeNode = svgGot.getTreeNode();
                //       let parent = null;
                //       if (treeNode.parent == null) {
                //         parent = "ROOT";
                //       }
                //       else {
                //         if (treeNode.parent.data.inheritedHierarchyCode != null) {
                //           parent = "ROOT";
                //         }
                //         else {
                //           parent = treeNode.parent.data.geoObjectType;
                //         }
                //       }

                //       hierarchyComponent.removeFromHierarchy(parent, svgGot.getCode(), (err: any) => { svgGot.setPos(startPoint.x, startPoint.y, false); });
                //     });

                //     (<ConfirmModalComponent>hierarchyComponent.bsModalRef.content).onCancel.subscribe(data => {
                //       svgGot.setPos(startPoint.x, startPoint.y, false);
                //     });
                //   }
                //   else {
                //     svgGot.setPos(startPoint.x, startPoint.y, false);
                //   }

                // }
                // else {
                //   svgGot.setPos(startPoint.x, startPoint.y, false);
                // }
            });

        svgDragHandler(d3.selectAll(".svg-got-body-rect[data-inherited=false],.svg-got-label-text[data-inherited=false],.svg-got-header-rect[data-inherited=false]"));
    }

    public findGeoObjectTypeByCode(code: string): GeoObjectType {
        for (let i = 0; i < this.geoObjectTypes.length; ++i) {
            let got: GeoObjectType = this.geoObjectTypes[i];

            if (got.code === code) {
                return got;
            }
        }
    }

    public findHierarchyByCode(code: string): HierarchyType {
        for (let i = 0; i < this.hierarchies.length; ++i) {
            let ht: HierarchyType = this.hierarchies[i];

            if (ht.code === code) {
                return ht;
            }
        }
    }

    public findOrganizationByCode(code: string): Organization {
        for (let i = 0; i < this.organizations.length; ++i) {
            let org: Organization = this.organizations[i];

            if (org.code === code) {
                return org;
            }
        }
    }

    private addChild(hierarchyCode: string, parentGeoObjectTypeCode: string, childGeoObjectTypeCode: string): void {
        this.hierarchyService.addChildToHierarchy(hierarchyCode, parentGeoObjectTypeCode, childGeoObjectTypeCode).then((ht: HierarchyType) => {
            let got = this.findGeoObjectTypeByCode(childGeoObjectTypeCode);

            let index = null;
            for (let i = 0; i < got.relatedHierarchies.length; ++i) {
                if (got.relatedHierarchies[i] === hierarchyCode) {
                    index = i;
                    break;
                }
            }

            if (index == null) {
                got.relatedHierarchies.push(hierarchyCode);
            }

            this.refreshPrimaryHierarchy(ht);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    private insertBetweenTypes(hierarchyCode: string, parentGeoObjectTypeCode: string, middleGeoObjectTypeCode: string, youngestGeoObjectTypeCode: string): void {
        this.hierarchyService.insertBetweenTypes(hierarchyCode, parentGeoObjectTypeCode, middleGeoObjectTypeCode, youngestGeoObjectTypeCode).then((ht: HierarchyType) => {
            this.refreshPrimaryHierarchy(ht);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    ngAfterViewInit() {

    }

    isRA(): boolean {
        return this.authService.isRA();
    }

    isOrganizationRA(orgCode: string, dropZone: boolean = false): boolean {
        return this.isSRA || this.authService.isOrganizationRA(orgCode);
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

    public refreshAll(desiredHierarchy: HierarchyType) {
        // Clear the types to then refresh
        this.geoObjectTypes = [];

        this.registryService.init().then(response => {
            this.localizeService.setLocales(response.locales);

            this.setGeoObjectTypes(response.types);

            this.organizations = response.organizations;

            this.organizations.forEach(org => {
                if (this.isOrganizationRA(org.code)) {
                    this.userOrganization = org.code;
                }
            });

            if (!this.authService.isSRA()) {
                let myorg = this.authService.getMyOrganizations();

                let pos = response.organizations.findIndex(org => {
                    return org.code === myorg[0];
                });

                if (pos >= 0) {
                    Utils.arrayMove(response.organizations, pos, 0);
                }
            }

            let pos = this.getGeoObjectTypePosition("ROOT");
            if (pos) {
                this.geoObjectTypes.splice(pos, 1);
            }

            this.setHierarchies(response.hierarchies);

            this.setNodesOnInit(desiredHierarchy);

            this.updateViewDatastructures();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    public setGeoObjectTypes(types: GeoObjectType[]): void {
        // Set group parent types
        this.setAbstractTypes(types);

        // Set GeoObjectTypes that aren't part of a group.
        types.forEach(type => {
            if (!type.isAbstract) {
                if (!type.superTypeCode) {
                    this.geoObjectTypes.push(type);
                }
            }
        });

        // Sort aphabetically because all other types to add will be children in a group.
        this.geoObjectTypes.sort((a, b) => {
            if (a.label.localizedValue.toLowerCase() < b.label.localizedValue.toLowerCase()) return -1;
            else if (a.label.localizedValue.toLowerCase() > b.label.localizedValue.toLowerCase()) return 1;
            else return 0;
        });

        // Add group children
        types.forEach(type => {
            if (!type.isAbstract) {
                if (type.superTypeCode && type.superTypeCode.length > 0) {
                    for (let i = 0; i < this.geoObjectTypes.length; i++) {
                        let setType = this.geoObjectTypes[i];
                        if (type.superTypeCode === setType.code) {
                            this.geoObjectTypes.splice(i + 1, 0, type);
                        }
                    }
                }
            }
        });
    }

    private setAbstractTypes(types: GeoObjectType[]): void {
        types.forEach(type => {
            if (type.isAbstract) {
                this.geoObjectTypes.push(type);
            }
        });
    }

    public updateViewDatastructures(): void {
        this.hierarchiesByOrg = [];
        this.typesByOrg = [];

        for (let i = 0; i < this.organizations.length; ++i) {
            let org: Organization = this.organizations[i];

            this.hierarchiesByOrg.push({ org: org, hierarchies: this.getHierarchiesByOrg(org) });
            this.typesByOrg.push({ org: org, types: this.getTypesByOrg(org) });
        }

        this.geoObjectTypes.forEach((got: GeoObjectType) => {
            got.canDrag = this.calculateCanDrag(got);
            got.relatedHierarchies = this.calculateRelatedHierarchies(got);
        });

        this.onFilterChange();
    }

    public excludeHierarchyTypeDeletes(hierarchy: HierarchyType) {
        return (this.hierarchyTypeDeleteExclusions.indexOf(hierarchy.code) !== -1);
    }

    public excludeGeoObjectTypeDeletes(geoObjectType: GeoObjectType) {
        return (this.geoObjectTypeDeleteExclusions.indexOf(geoObjectType.code) !== -1);
    }

    private setNodesOnInit(desiredHierarchy: HierarchyType): void {
        let index = -1;

        if (desiredHierarchy != null) {
            index = this.hierarchies.findIndex(h => h.code === desiredHierarchy.code);
        } else if (this.hierarchies.length > 0) {
            index = 0;
        }

        if (index > -1) {
            const hierarchy = this.hierarchies[index];

            this.setCurrentHierarchy(hierarchy);

            this.renderTree();
        }
    }

    private getHierarchy(hierarchyId: string): HierarchyType {
        let target: HierarchyType = null;
        this.hierarchies.forEach(hierarchy => {
            if (hierarchyId === hierarchy.code) {
                target = hierarchy;
            }
        });

        return target;
    }

    private setHierarchies(data: HierarchyType[]): void {
        let hierarchies: HierarchyType[] = [];
        data.forEach((hierarchyType, index) => {
            if (hierarchyType.rootGeoObjectTypes.length > 0) {
                hierarchyType.rootGeoObjectTypes.forEach(rootGeoObjectType => {
                    this.processHierarchyNodes(rootGeoObjectType);
                });
            }

            hierarchies.push(hierarchyType);
        });

        this.hierarchies = hierarchies;

        this.hierarchies.sort((a, b) => {
            if (a.label.localizedValue.toLowerCase() < b.label.localizedValue.toLowerCase()) return -1;
            else if (a.label.localizedValue.toLowerCase() > b.label.localizedValue.toLowerCase()) return 1;
            else return 0;
        });
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

    public hierarchyOnClick(event: any, item: HierarchyType) {
        this.setCurrentHierarchy(item);
        this.renderTree();
    }

    public createHierarchy(): void {
        this.bsModalRef = this.modalService.show(CreateHierarchyTypeModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
            class: "upload-modal"
        });

        (<CreateHierarchyTypeModalComponent>this.bsModalRef.content).onHierarchytTypeCreate.subscribe(data => {
            this.hierarchies.push(data);

            this.hierarchies.sort((a: HierarchyType, b: HierarchyType) => {
                let nameA = a.label.localizedValue.toUpperCase(); // ignore upper and lowercase
                let nameB = b.label.localizedValue.toUpperCase(); // ignore upper and lowercase

                if (nameA < nameB) {
                    return -1; // nameA comes first
                }

                if (nameA > nameB) {
                    return 1; // nameB comes first
                }

                return 0; // names must be equal
            });

            this.updateViewDatastructures();
        });
    }

    public deleteHierarchyType(obj: HierarchyType): void {
        this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + " [" + obj.label.localizedValue + "]";
        this.bsModalRef.content.data = obj.code;
        this.bsModalRef.content.type = "DANGER";
        this.bsModalRef.content.submitText = this.localizeService.decode("modal.button.delete");

        (<ConfirmModalComponent>this.bsModalRef.content).onConfirm.subscribe(data => {
            this.removeHierarchyType(data);
        });
    }

    public editHierarchyType(obj: HierarchyType, readOnly: boolean): void {
        this.bsModalRef = this.modalService.show(CreateHierarchyTypeModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
            class: "upload-modal"
        });
        this.bsModalRef.content.edit = true;
        this.bsModalRef.content.readOnly = readOnly;
        this.bsModalRef.content.hierarchyType = obj;
        this.bsModalRef.content.onHierarchytTypeCreate.subscribe(data => {
            let pos = this.getHierarchyTypePosition(data.code);

            this.hierarchies[pos].label = data.label;
            this.hierarchies[pos].description = data.description;
            this.hierarchies[pos].progress = data.progress;
            this.hierarchies[pos].acknowledgement = data.acknowledgement;
            this.hierarchies[pos].disclaimer = data.disclaimer;
            this.hierarchies[pos].useConstraints = data.useConstraints;
            this.hierarchies[pos].accessConstraints = data.accessConstraints;
            this.hierarchies[pos].contact = data.contact;
            this.hierarchies[pos].phoneNumber = data.phoneNumber;
            this.hierarchies[pos].email = data.email;

            this.updateViewDatastructures();

            if (this.currentHierarchy.code === data.code) {
                this.setCurrentHierarchy(this.hierarchies[pos]);

                this.renderTree();
            }
        });
    }

    setCurrentHierarchy(hierarchyType: HierarchyType): void {
        this.currentHierarchy = hierarchyType;
    }

    isPrimaryHierarchy(hierarchy: HierarchyType): boolean {
        // return hierarchy.isPrimary;
        return hierarchy.code === this.currentHierarchy.code;
    }

    public removeHierarchyType(code: string): void {
        this.hierarchyService.deleteHierarchyType(code).then(response => {
            let pos = this.getHierarchyTypePosition(code);
            this.hierarchies.splice(pos, 1);
            this.updateViewDatastructures();

            if (this.hierarchies.length > 0) {
                this.setCurrentHierarchy(this.hierarchies[0]);
            } else {
                this.currentHierarchy = null;
            }

            this.renderTree();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    public createGeoObjectType(groupSuperType: GeoObjectType, isAbstract: boolean, org: Organization): void {
        this.bsModalRef = this.modalService.show(CreateGeoObjTypeModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
            class: "upload-modal"
        });
        this.bsModalRef.content.init(org, this.geoObjectTypes, groupSuperType, isAbstract);

        this.bsModalRef.content.onGeoObjTypeCreate.subscribe(data => {
            data.relatedHierarchies = this.calculateRelatedHierarchies(data);

            this.refreshAll(this.currentHierarchy);
        });
    }

    public deleteGeoObjectType(obj: GeoObjectType): void {
        this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + " [" + obj.label.localizedValue + "]";
        this.bsModalRef.content.data = obj.code;
        this.bsModalRef.content.submitText = this.localizeService.decode("modal.button.delete");
        this.bsModalRef.content.type = ModalTypes.danger;

        (<ConfirmModalComponent>this.bsModalRef.content).onConfirm.subscribe(data => {
            this.removeGeoObjectType(data);
        });
    }

    public removeGeoObjectType(code: string, errCallback: (err: HttpErrorResponse) => void = null): void {
        this.registryService.deleteGeoObjectType(code).then(response => {
            let pos = this.getGeoObjectTypePosition(code);
            this.geoObjectTypes.splice(pos, 1);

            this.refreshAll(this.currentHierarchy);
        }).catch((err: HttpErrorResponse) => {
            if (errCallback != null) {
                errCallback(err);
            }
            this.error(err);
        });
    }

    public manageGeoObjectType(geoObjectType: GeoObjectType, readOnly: boolean): void {
        this.bsModalRef = this.modalService.show(ManageGeoObjectTypeModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
            class: "manage-geoobjecttype-modal"
        });

        geoObjectType.attributes.sort((a, b) => {
            if (a.label.localizedValue < b.label.localizedValue) return -1;
            else if (a.label.localizedValue > b.label.localizedValue) return 1;
            else return 0;
        });
        this.bsModalRef.content.geoObjectType = geoObjectType;
        this.bsModalRef.content.readOnly = readOnly;

        (<ManageGeoObjectTypeModalComponent>this.bsModalRef.content).onGeoObjectTypeSubmitted.subscribe(data => {
            if (data.isAbstract) {
                this.refreshAll(this.currentHierarchy);
            } else {
                const position = this.getGeoObjectTypePosition(data.code);

                if (position !== -1) {
                    this.geoObjectTypes[position] = data;
                }

                // Update all of the hierarchies for the new geo object type
                this.updateViewDatastructures();

                this.hierarchies.forEach((hierarchyType: HierarchyType) => {
                    this.processHierarchyNodes(hierarchyType.rootGeoObjectTypes[0]);
                });

                // Update the current hierarchy view
                if (this.currentHierarchy != null) {
                    this.processHierarchyNodes(this.currentHierarchy.rootGeoObjectTypes[0]);
                }

                this.renderTree();
            }
        });
    }

    private getHierarchyTypePosition(code: string): number {
        for (let i = 0; i < this.hierarchies.length; i++) {
            let obj = this.hierarchies[i];
            if (obj.code === code) {
                return i;
            }
        }
    }

    private getGeoObjectTypePosition(code: string): number {
        for (let i = 0; i < this.geoObjectTypes.length; i++) {
            let obj = this.geoObjectTypes[i];
            if (obj.code === code) {
                return i;
            }
        }

        return null;
    }

    public refreshPrimaryHierarchy(hierarchyType: HierarchyType) {
        this.processHierarchyNodes(hierarchyType.rootGeoObjectTypes[0]);

        for (let i = 0; i < this.hierarchies.length; ++i) {
            let hierarchy = this.hierarchies[i];

            if (hierarchy.code === hierarchyType.code) {
                this.hierarchies[i] = hierarchyType;

                this.setCurrentHierarchy(hierarchyType);
            }
        }

        this.updateViewDatastructures();

        this.renderTree();
    }

    public removeFromHierarchy(parentGotCode, gotCode, errCallback: (err: HttpErrorResponse) => void = null): void {
        const that = this;

        this.hierarchyService.removeFromHierarchy(this.currentHierarchy.code, parentGotCode, gotCode).then(hierarchyType => {
            let got = that.findGeoObjectTypeByCode(gotCode);

            let index = null;
            for (let i = 0; i < got.relatedHierarchies.length; ++i) {
                if (got.relatedHierarchies[i] === hierarchyType.code) {
                    index = i;
                    break;
                }
            }

            if (index != null) {
                got.relatedHierarchies.splice(index, 1);
            }

            that.refreshPrimaryHierarchy(hierarchyType);
        }).catch((err: HttpErrorResponse) => {
            if (errCallback != null) {
                errCallback(err);
            }

            this.error(err);
        });
    }

    public isActive(item: HierarchyType) {
        return this.currentHierarchy === item;
    }

    onFilterChange(): void {
        const label = this.filter.toLowerCase();

        this.filteredHierarchiesByOrg = [];
        this.filteredTypesByOrg = [];

        this.hierarchiesByOrg.forEach((item: { org: Organization, hierarchies: HierarchyType[] }) => {
            const filtered = item.hierarchies.filter((hierarchy: HierarchyType) => {
                const index = hierarchy.label.localizedValue.toLowerCase().indexOf(label);

                return (index !== -1);
            });

            this.filteredHierarchiesByOrg.push({ org: item.org, hierarchies: filtered });
        });

        this.typesByOrg.forEach((item: { org: Organization, types: GeoObjectType[] }) => {
            const filtered = item.types.filter((type: GeoObjectType) => {
                const index = type.label.localizedValue.toLowerCase().indexOf(label);

                return (index !== -1);
            });

            this.filteredTypesByOrg.push({ org: item.org, types: filtered });
        });

        setTimeout(() => { this.registerDragHandlers(); }, 500);
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

    public importTypes(): void {
        this.bsModalRef = this.modalService.show(ImportTypesModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
            class: "upload-modal"
        });

        this.bsModalRef.content.init(this.organizations);

        this.bsModalRef.content.onNodeChange.subscribe(data => {
            // Reload the page
            this.refreshAll(null);
        });
    }

    public exportTypes(): void {
        this.bsModalRef = this.modalService.show(ExportTypesModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
            class: "upload-modal"
        });

        this.bsModalRef.content.init(this.organizations);

        this.bsModalRef.content.onNodeChange.subscribe(orgCode => {
            if (orgCode != null && orgCode.length > 0) {
                window.location.href = environment.apiUrl + "/api/cgr/export-types?code=" + orgCode;
            }
        });
    }

    public exportRDF(): void {
        this.bsModalRef = this.modalService.show(RDFExportModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });

        this.bsModalRef.content.init((type) => {
            console.log(type);
            // this.types.push({ oid: type.oid, label: type.displayLabel.localizedValue });

            // this.router.navigate([], {
            //     relativeTo: this.route,
            //     queryParams: { oid: type.oid },
            //     queryParamsHandling: "merge",
            //     replaceUrl: true
            // });
        }, null);
    }


    public error(err: HttpErrorResponse): void {
        this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

}
