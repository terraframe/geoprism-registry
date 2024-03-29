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

/* eslint-disable no-floating-decimal */
import * as d3 from "d3";
import { calculateTextWidth } from "./svg-util";

import { GeoObjectType } from "@registry/model/registry";
import { HierarchyType } from "@registry/model/hierarchy";

import { SvgHierarchyType } from "./svg-hierarchy-type";
import { HierarchyComponent, RELATED_NODE_BANNER_COLOR } from "../hierarchy.component";

import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import { ErrorHandler, ConfirmModalComponent, ErrorModalComponent } from "@shared/component";
import { LocalizationService, AuthService } from "@shared/service";

export class SvgHierarchyNode {

    private hierarchyComponent: HierarchyComponent;

    private svgHierarchyType: SvgHierarchyType;

    private geoObjectType: GeoObjectType;

    private treeNode: any;

    /*
     * Reference to the modal current showing
    */
    private bsModalRef: BsModalRef;

    constructor(hierarchyComponent: HierarchyComponent, svgHierarchyType: SvgHierarchyType, geoObjectType: GeoObjectType, treeNode: any,
        public localizeService: LocalizationService, public modalService: BsModalService, public authService: AuthService) {
        this.hierarchyComponent = hierarchyComponent;
        this.svgHierarchyType = svgHierarchyType;
        this.geoObjectType = geoObjectType;
        this.treeNode = treeNode;
    }

    getCode(): string {
        return this.geoObjectType.code;
    }

    setPos(x: number, y: number, dragging: boolean) {
        let bbox = this.getBbox();

        // Move the GeoObjectType with the pointer when they move their mouse
        d3.select(".g-hierarchy[data-primary=true] .svg-got-body-rect[data-gotCode=\"" + this.getCode() + "\"]")
            .classed("dragging", dragging)
            .attr("x", x)
            .attr("y", y);

        d3.select(".g-hierarchy[data-primary=true] .svg-got-header-rect[data-gotCode=\"" + this.getCode() + "\"]")
            .classed("dragging", dragging)
            .attr("x", x)
            .attr("y", y - SvgHierarchyType.gotRectH / 2 + 2);

        d3.select(".g-hierarchy[data-primary=true] .svg-got-label-text[data-gotCode=\"" + this.getCode() + "\"]")
            .classed("dragging", dragging)
            .attr("x", x + 5)
            .attr("y", y + 1);

        d3.select(".g-hierarchy[data-primary=true] .svg-got-relatedhiers-button[data-gotCode=\"" + this.getCode() + "\"]")
            .classed("dragging", dragging)
            .attr("x", x + bbox.width - 20)
            .attr("y", y + 17);

        // Move inherit and uninherit buttons with the node they're moving

        let inheritNode: any = d3.select(".g-hierarchy[data-primary=true] .hierarchy-inherit-button[data-gotCode=\"" + this.getCode() + "\"]").node();
        if (inheritNode != null) {
            const heritX = (x + bbox.width - 60);
            const heritY = (y + bbox.height - 24);
            let inheritBbox = inheritNode.getBBox();
            d3.select(".g-hierarchy[data-primary=true] .hierarchy-inherit-button[data-gotCode=\"" + this.getCode() + "\"]")
                .classed("dragging", dragging)
                .attr("transform", "translate(" + (heritX - inheritBbox.x) + " " + (heritY - inheritBbox.y) + ")");
        }

        let uninheritNode: any = d3.select(".g-hierarchy[data-primary=true] .hierarchy-uninherit-button[data-gotCode=\"" + this.getCode() + "\"]").node();
        if (uninheritNode != null) {
            const heritX = (x + bbox.width - 71);
            const heritY = (y + bbox.height - 24);
            let uninheritBbox = uninheritNode.getBBox();
            d3.select(".g-hierarchy[data-primary=true] .hierarchy-uninherit-button[data-gotCode=\"" + this.getCode() + "\"]")
                .classed("dragging", dragging)
                .attr("transform", "translate(" + (heritX - uninheritBbox.x) + " " + (heritY - uninheritBbox.y) + ")");
        }
    }

    getPos() {
        let select = d3.select(".g-hierarchy[data-primary=true] .svg-got-body-rect[data-gotCode=\"" + this.getCode() + "\"]");

        return { x: parseInt(select.attr("x")), y: parseInt(select.attr("y")) };
    }

    getBbox() {
        let select = d3.select(".g-hierarchy[data-primary=true] .svg-got-body-rect[data-gotCode=\"" + this.getCode() + "\"]");

        return { x: parseInt(select.attr("x")), y: parseInt(select.attr("y")) - 3, width: parseInt(select.attr("width")), height: parseInt(select.attr("height")) + 3 };
    }

    getTreeNode() {
        return this.treeNode;
    }

    hideRelatedHierarchy(): string {
        let existingSecondary = d3.select(".g-hierarchy[data-primary=\"false\"]");
        if (existingSecondary.node() != null) {
            existingSecondary.remove();
            this.hierarchyComponent.calculateSvgViewBox();

            let existingSecondaryCode = existingSecondary.attr("data-code");
            return existingSecondaryCode;
        }
    }

    renderRelatedHierarchiesMenu() {
        let that = this;
        let existingMenu = d3.select(".g-context-menu");

        if (existingMenu.node() == null) {
            //let parent = d3.select('g.g-hierarchy-tree[data-code="' + this.svgHierarchyType.hierarchyType.code + '"]');
            let parent = d3.select("#svg");

            let contextMenuGroup = parent.append("g").classed("g-context-menu", true);

            let relatedHierarchies = this.svgHierarchyType.getRelatedHierarchies(this.getCode());

            const hasActionsPermissions = this.authService.isSRA() || this.authService.isOrganizationRA(this.svgHierarchyType.hierarchyType.organizationCode);

            let bbox = this.getBbox();
            let x = bbox.x + bbox.width - 5;
            let y = bbox.y + bbox.height / 2 - 8;
            const height = 20;
            const fontSize = 8;
            const widthPadding = 10;
            const borderColor = "#aaaaaa";
            const dividerColor = "#e4e4e4";
            const fontFamily = "sans-serif";
            const titleFontSize = 9;

            const titleLabel = this.hierarchyComponent.localize("hierarchy.content.relatedHierarchies");
            const actionsTitle = this.hierarchyComponent.localize("hierarchy.content.actionsTitle");
            const removeFromHierarchyLabel = this.hierarchyComponent.localize("hierarchy.content.removeFromHierarchy");
            const noRelatedHierLabel = this.hierarchyComponent.localize("hierarchy.content.noRelatedHierarchies");
            const hideRelatedHierarchyLabel = this.hierarchyComponent.localize("hierarchy.content.hideRelatedHierarchy");
            const uninheritLabel = this.hierarchyComponent.localize("hierarchy.content.uninherit");
            const inheritLabel = this.hierarchyComponent.localize("hierarchy.content.inherit");

            let isSecondaryHierarchyRendered = (d3.select(".g-hierarchy[data-primary=\"false\"]").node() != null);

            let numActions = hasActionsPermissions ? (isSecondaryHierarchyRendered ? 2 : 1) : 0;

            // Calculate the width of our title
            let width = calculateTextWidth(titleLabel, titleFontSize);

            // Calculate with of remove text
            let removeWidth = calculateTextWidth(removeFromHierarchyLabel, fontSize);
            width = removeWidth > width ? removeWidth : width;

            if (isSecondaryHierarchyRendered) {
              // Calculate width of "hide related hierarchy" label
                let hideRelatedWidth = calculateTextWidth(hideRelatedHierarchyLabel, fontSize);
                width = hideRelatedWidth > width ? hideRelatedWidth : width;
            }

            if (this.treeNode.parent != null && this.treeNode.parent.data.inheritedHierarchyCode != null && this.treeNode.parent.data.inheritedHierarchyCode != "") {
                let uninheritWidth = calculateTextWidth(uninheritLabel, fontSize);
                width = uninheritWidth > width ? uninheritWidth : width;
                numActions++;
            } else {
                let existingSecondary = d3.select(".g-hierarchy[data-primary=\"false\"]");
                if (existingSecondary.node() != null) {
                    let existingSecondaryCode = existingSecondary.attr("data-code");
                    let secondaryHierarchy = this.hierarchyComponent.findHierarchyByCode(existingSecondaryCode);

                    let svgSecondaryHierarchy = new SvgHierarchyType(this.hierarchyComponent, d3.select("#svg"), secondaryHierarchy, true, this.localizeService, this.modalService, this.authService);
                    let relatedGotHasParents = svgSecondaryHierarchy.getNodeByCode(this.getCode()).getTreeNode().parent != null;

                    if (this.treeNode.parent == null && relatedGotHasParents) {
                        let inheritWidth = calculateTextWidth(inheritLabel, fontSize);
                        width = inheritWidth > width ? inheritWidth : width;
                        numActions++;
                    }
                }
            }

            // Calculate the width of our context menu, which is based on how long the text inside it will be.
            // We don't know how long text is until we render it. So we'll need to loop over all the text and
            // render and destroy all of it.
            if (relatedHierarchies.length > 0) {
                relatedHierarchies.forEach((relatedHierarchyCode: string) => {
                    let relatedHierarchy = this.hierarchyComponent.findHierarchyByCode(relatedHierarchyCode);

                    let relatedHierarchyLabel = relatedHierarchy.label.localizedValue;
                    if (this.treeNode.parent != null && this.treeNode.parent.data.inheritedHierarchyCode === relatedHierarchy.code) {
                        relatedHierarchyLabel = relatedHierarchyLabel + " (" + this.hierarchyComponent.localize("hierarchy.content.inherited") + ")";
                    }

                    let textWidth = calculateTextWidth(relatedHierarchyLabel, fontSize);

                    if (textWidth > width) {
                        width = textWidth;
                    }
                });
            } else {
                let noHierLabelWidth = calculateTextWidth(noRelatedHierLabel, fontSize);
                width = noHierLabelWidth > width ? noHierLabelWidth : width;
            }

            width = width + widthPadding;

            let heightAdditions: number = relatedHierarchies.length > 0 ? (numActions + 2) : (numActions + 3);

            // Background rectangle with border
            contextMenuGroup.append("rect")
                .classed("contextmenu-relatedhiers-background", true)
                .attr("x", x)
                .attr("y", y)
                .attr("rx", 5)
                .attr("width", width)
                .attr("height", height * (relatedHierarchies.length + heightAdditions))
                .attr("fill", "white")
                .attr("stroke-width", .5)
                .attr("stroke", borderColor);

            // Related Hierarchies Title
            contextMenuGroup.append("text")
                .classed("contextmenu-relatedhiers-title", true)
                .attr("x", x + widthPadding / 2)
                .attr("y", y + (height / 2) + (titleFontSize / 2))
                .attr("font-size", titleFontSize)
                .attr("font-family", fontFamily)
                .attr("font-weight", "bold")
                .text(titleLabel);

            y = y + height;

            // Dividing line at the bottom
            contextMenuGroup.append("line")
                .classed("contextmenu-relatedhiers-divider", true)
                .attr("x1", x)
                .attr("y1", y)
                .attr("x2", x + width)
                .attr("y2", y)
                .attr("stroke", borderColor)
                .attr("stroke-width", .5);


            if (relatedHierarchies.length > 0) {
                // Loop over all related hierarchies and draw them as list items
                for (let i = 0; i < relatedHierarchies.length; ++i) {
                    let relatedHierarchyCode = relatedHierarchies[i];
                    let relatedHierarchy = this.hierarchyComponent.findHierarchyByCode(relatedHierarchyCode);

                    let relatedHierarchyLabel = relatedHierarchy.label.localizedValue;
                    if (this.treeNode.parent != null && this.treeNode.parent.data.inheritedHierarchyCode === relatedHierarchy.code) {
                        relatedHierarchyLabel = relatedHierarchyLabel + " (" + this.hierarchyComponent.localize("hierarchy.content.inherited") + ")";
                    }

                    // Text that says the hierarchy's display label
                    contextMenuGroup.append("text")
                        .classed("contextmenu-relatedhiers-text", true)
                        .attr("data-hierCode", relatedHierarchyCode)
                        .attr("x", x + widthPadding / 2)
                        .attr("y", y + (height / 2) + (fontSize / 2))
                        .attr("font-size", fontSize)
                        .attr("font-family", fontFamily)
                        .text(relatedHierarchyLabel)
                        .style("cursor", "pointer")
                        .on("click", function(event, node) { that.renderSecondaryHierarchy(relatedHierarchy); });

                    y = y + height;

                    // Dividing line at the bottom
                    if (i < relatedHierarchies.length) {
                        contextMenuGroup.append("line")
                            .classed("contextmenu-relatedhiers-divider", true)
                            .attr("data-hierCode", relatedHierarchyCode)
                            .attr("x1", x + 5)
                            .attr("y1", y)
                            .attr("x2", x + width - 5)
                            .attr("y2", y)
                            .attr("stroke", dividerColor)
                            .attr("stroke-width", .5);
                    }
                }
            } else {
                // Text that says "No related Hierarchies"
                contextMenuGroup.append("text")
                    .classed("contextmenu-relatedhiers-text", true)
                    .attr("x", x + widthPadding / 2)
                    .attr("y", y + (height / 2) + (fontSize / 2))
                    .attr("font-size", fontSize)
                    .attr("font-family", fontFamily)
                    .text(noRelatedHierLabel);

                y = y + height;

                    // Dividing line at the bottom
                contextMenuGroup.append("line")
                    .classed("contextmenu-relatedhiers-divider", true)
                    .attr("x1", x + 5)
                    .attr("y1", y)
                    .attr("x2", x + width - 5)
                    .attr("y2", y)
                    .attr("stroke", dividerColor)
                    .attr("stroke-width", .5);
            }

      // Actions Section
            if (hasActionsPermissions) {
              // Actions Title
                contextMenuGroup.append("text")
                    .classed("contextmenu-relatedhiers-title", true)
                    .attr("x", x + widthPadding / 2)
                    .attr("y", y + (height / 2) + (titleFontSize / 2))
                    .attr("font-size", titleFontSize)
                    .attr("font-family", fontFamily)
                    .attr("font-weight", "bold")
                    .text(actionsTitle);

                y = y + height;

              // Dividing line at the bottom
                contextMenuGroup.append("line")
                    .classed("contextmenu-relatedhiers-divider", true)
                    .attr("x1", x)
                    .attr("y1", y)
                    .attr("x2", x + width)
                    .attr("y2", y)
                    .attr("stroke", borderColor)
                    .attr("stroke-width", .5);

        // "Remove from hierarchy" button
                contextMenuGroup.append("text")
                    .classed("contextmenu-relatedhiers-text", true)
                    .attr("data-remove", "REPLACE---gotCode")
                    .attr("x", x + widthPadding / 2)
                    .attr("y", y + (height / 2) + (fontSize / 2))
                    .attr("font-size", fontSize)
                    .attr("font-family", fontFamily)
                    .text(removeFromHierarchyLabel)
                    .style("cursor", "pointer")
                    .on("click", function(event, node) { that.removeGotFromHierarchy(); });

                y = y + height;

            // Inherit / Uninherit buttons
                if (this.treeNode.parent != null && this.treeNode.parent.data.inheritedHierarchyCode != null && this.treeNode.parent.data.inheritedHierarchyCode != "") {
                    contextMenuGroup.append("line")
                        .classed("contextmenu-relatedhiers-divider", true)
                        .attr("x1", x)
                        .attr("y1", y)
                        .attr("x2", x + width)
                        .attr("y2", y)
                        .attr("stroke", borderColor)
                        .attr("stroke-width", .5);

                    contextMenuGroup.append("text")
                        .classed("contextmenu-relatedhiers-text", true)
                        .attr("x", x + widthPadding / 2)
                        .attr("y", y + (height / 2) + (fontSize / 2))
                        .attr("font-size", fontSize)
                        .attr("font-family", fontFamily)
                        .text(uninheritLabel)
                        .style("cursor", "pointer")
                        .on("click", function(event, node) { that.onClickUninheritHierarchy(); });

                    y = y + height;
                } else {
                    let existingSecondary = d3.select(".g-hierarchy[data-primary=\"false\"]");
                    if (existingSecondary.node() != null) {
                        let existingSecondaryCode = existingSecondary.attr("data-code");
                        let secondaryHierarchy = this.hierarchyComponent.findHierarchyByCode(existingSecondaryCode);

                        let svgSecondaryHierarchy = new SvgHierarchyType(this.hierarchyComponent, d3.select("#svg"), secondaryHierarchy, true, this.localizeService, this.modalService, this.authService);
                        let relatedGotHasParents = svgSecondaryHierarchy.getNodeByCode(this.getCode()).getTreeNode().parent != null;

                        if (this.treeNode.parent == null && relatedGotHasParents) {
                            contextMenuGroup.append("line")
                                .classed("contextmenu-relatedhiers-divider", true)
                                .attr("x1", x)
                                .attr("y1", y)
                                .attr("x2", x + width)
                                .attr("y2", y)
                                .attr("stroke", borderColor)
                                .attr("stroke-width", .5);

                            contextMenuGroup.append("text")
                                .classed("contextmenu-relatedhiers-text", true)
                                .attr("x", x + widthPadding / 2)
                                .attr("y", y + (height / 2) + (fontSize / 2))
                                .attr("font-size", fontSize)
                                .attr("font-family", fontFamily)
                                .text(inheritLabel)
                                .style("cursor", "pointer")
                                .on("click", function(event, node) { that.onClickInheritHierarchy(secondaryHierarchy); });

                            y = y + height;
                        }
                    }
                }
            }

            if (isSecondaryHierarchyRendered) {
                contextMenuGroup.append("line")
                    .classed("contextmenu-relatedhiers-divider", true)
                    .attr("x1", x)
                    .attr("y1", y)
                    .attr("x2", x + width)
                    .attr("y2", y)
                    .attr("stroke", borderColor)
                    .attr("stroke-width", .5);

                contextMenuGroup.append("text")
                    .classed("contextmenu-relatedhiers-text", true)
                    .attr("x", x + widthPadding / 2)
                    .attr("y", y + (height / 2) + (fontSize / 2))
                    .attr("font-size", fontSize)
                    .attr("font-family", fontFamily)
                    .text(hideRelatedHierarchyLabel)
                    .style("cursor", "pointer")
                    .on("click", function(event, node) {
                        that.hideRelatedHierarchy();

                        let existingMenu = d3.select(".g-context-menu");
                        if (existingMenu.node() != null) {
                            existingMenu.remove();
                        }

                        d3.select(".g-hierarchy-got-connector").remove();
                    });
            }

            this.hierarchyComponent.calculateSvgViewBox();
        } else {
            existingMenu.remove();
        }
    }

    removeGotFromHierarchy() {
        let svgGot = this.hierarchyComponent.primarySvgHierarchy.getNodeByCode(this.geoObjectType.code);

        let obj = this.hierarchyComponent.findGeoObjectTypeByCode(svgGot.getCode());

        this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });

        let message = this.localizeService.decode("confirm.modal.verify.remove.hierarchy");
        message = message.replace("{label}", obj.label.localizedValue);

        this.bsModalRef.content.message = message;
        this.bsModalRef.content.data = obj.code;

        (<ConfirmModalComponent> this.bsModalRef.content).onConfirm.subscribe(data => {
            let treeNode = svgGot.getTreeNode();
            let parent = null;
            if (treeNode.parent == null) {
                parent = "ROOT";
            } else {
                if (treeNode.parent.data.inheritedHierarchyCode != null) {
                    parent = "ROOT";
                } else {
                    parent = treeNode.parent.data.geoObjectType;
                }
            }

            this.hierarchyComponent.removeFromHierarchy(parent, svgGot.getCode(), (err: any) => { console.log(err); });
        });
    }

    renderSecondaryHierarchy(relatedHierarchy: HierarchyType) {
        d3.select(".g-context-menu").remove();
        d3.select(".g-hierarchy-got-connector").remove();

        let myBbox = this.getBbox();
        let svg = d3.select("#svg");

        // Remove any secondary hierarchy that may already be rendered
        if (this.hideRelatedHierarchy() === relatedHierarchy.code) {
            return;
        }

        // Get the bounding box for our primary hierarchy
        let primaryHierBbox = (d3.select(".g-hierarchy[data-primary=true]").node() as any).getBBox();

        // Render the secondary hierarchy
        let svgHt: SvgHierarchyType = new SvgHierarchyType(this.hierarchyComponent, svg, relatedHierarchy, false, this.localizeService, this.modalService, this.authService);
        svgHt.render();
        let gSecondary = d3.select(".g-hierarchy[data-primary=\"false\"]");

        // Translate the secondary hierarchy to the right of the primary hierarchy
        let gHierarchy: any = d3.select(".g-hierarchy[data-primary=\"false\"]").node();
        let bbox = gHierarchy.getBBox();
        let paddingLeft: number = primaryHierBbox.width + 40 + (primaryHierBbox.x - bbox.x);
        gSecondary.attr("transform", "translate(" + paddingLeft + " 0)");

        // Draw dotted line between the shared node in the hierarchies
        let secondaryGot = d3.select(".g-hierarchy[data-primary=false] .svg-got-body-rect[data-gotCode=\"" + this.getCode() + "\"]");
        let secondaryGotBbox = { x: parseInt(secondaryGot.attr("x")), y: parseInt(secondaryGot.attr("y")) - 3, width: parseInt(secondaryGot.attr("width")), height: parseInt(secondaryGot.attr("height")) + 3 };
        secondaryGotBbox.x = secondaryGotBbox.x + paddingLeft; // Apply transformation
        d3.select(".g-hierarchy-got-connector").remove();
        let gConnector = d3.select("#svg").append("g").classed("g-hierarchy-got-connector", true);
        gConnector.append("path")
            .classed("hierarchy-got-connector", true)
            .attr("fill", "none")
            .attr("stroke", "#555")
            .attr("stroke-opacity", 0.4)
            .attr("stroke-dasharray", "5,5")
            .attr("stroke-width", 1.5)
            .attr(
                "d",
                "M" + (myBbox.x + myBbox.width) + "," + (myBbox.y + myBbox.height / 2) +
                "H" + (((secondaryGotBbox.x) - (myBbox.x + myBbox.width)) / 2 + myBbox.x + myBbox.width) +
                "V" + (secondaryGotBbox.y + secondaryGotBbox.height / 2) +
                "H" + secondaryGotBbox.x
            );

        // Draw arrow for dotted line
        const arrowRectD = { height: 10, width: 7 };
        let gArrow = gConnector.append("g").classed("g-hierarchy-got-connector-arrow", true);
        gArrow.append("rect")
            .classed("hierarchy-got-connector-arrow-rect", true)
            .attr("x", myBbox.x + myBbox.width - arrowRectD.width / 2)
            .attr("y", myBbox.y + myBbox.height / 2 - arrowRectD.height / 2)
            .attr("width", arrowRectD.width)
            .attr("height", arrowRectD.height)
            .attr("fill", RELATED_NODE_BANNER_COLOR);
        gArrow.append("path")
            .classed("hierarchy-got-connector-arrow-path", true)
            .attr("fill", "none")
            .attr("stroke", "white")
            .attr("stroke-width", 1.5)
            .attr("d",
                "M" + (myBbox.x + myBbox.width - arrowRectD.width / 2 + ((arrowRectD.width * 2) / 3)) + "," + (myBbox.y + myBbox.height / 2 - arrowRectD.height / 2 + ((arrowRectD.height * 2) / 3)) +
                "L" + (myBbox.x + myBbox.width + arrowRectD.width / 2 - ((arrowRectD.width * 2) / 3)) + "," + (myBbox.y + myBbox.height / 2) +
                "L" + (myBbox.x + myBbox.width - arrowRectD.width / 2 + ((arrowRectD.width * 2) / 3)) + "," + (myBbox.y + myBbox.height / 2 - arrowRectD.height / 2 + arrowRectD.height / 3)
            );

        // Recalculate the viewbox (should probably be the last thing that happens)
        this.hierarchyComponent.calculateSvgViewBox();
    }

    onClickInheritHierarchy(hierarchy: HierarchyType) {
        this.hierarchyComponent.handleInheritHierarchy(this.svgHierarchyType.getCode(), hierarchy.code, this.getCode());
    }

    onClickUninheritHierarchy() {
        this.hierarchyComponent.handleUninheritHierarchy(this.svgHierarchyType.getCode(), this.getCode());
    }

}
