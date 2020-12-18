import * as d3 from 'd3';
import { svgPoint } from './svg-util';

import { GeoObjectType } from '@registry/model/registry';
import { HierarchyType } from '@registry/model/hierarchy';

import { SvgHierarchyType } from './svg-hierarchy-type';
import { calculateTextWidth } from './svg-util';
import { SvgController, RELATED_NODE_BANNER_COLOR } from './svg-controller';

import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { ErrorHandler, ConfirmModalComponent, ErrorModalComponent } from '@shared/component';
import { LocalizationService } from '@shared/service';

export class SvgHierarchyNode {

	private hierarchyComponent: SvgController;

	private svgHierarchyType: SvgHierarchyType;

	private geoObjectType: GeoObjectType;

	private treeNode: any;

	/*
     * Reference to the modal current showing
    */
	private bsModalRef: BsModalRef;

	constructor(hierarchyComponent: SvgController, svgHierarchyType: SvgHierarchyType, geoObjectType: GeoObjectType, treeNode: any,
		public localizeService: LocalizationService, public modalService: BsModalService) {
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
		d3.select('.g-hierarchy[data-primary=true] .svg-got-body-rect[data-gotCode="' + this.getCode() + '"]')
			.classed("dragging", dragging)
			.attr("x", x)
			.attr("y", y);

		d3.select('.g-hierarchy[data-primary=true] .svg-got-header-rect[data-gotCode="' + this.getCode() + '"]')
			.classed("dragging", dragging)
			.attr("x", x)
			.attr("y", y - SvgHierarchyType.gotRectH / 2 + 2);

		d3.select('.g-hierarchy[data-primary=true] .svg-got-label-text[data-gotCode="' + this.getCode() + '"]')
			.classed("dragging", dragging)
			.attr("x", x + 5)
			.attr("y", y + 1);

		d3.select('.g-hierarchy[data-primary=true] .svg-got-relatedhiers-button[data-gotCode="' + this.getCode() + '"]')
			.classed("dragging", dragging)
			.attr("x", x + bbox.width - 20)
			.attr("y", y + 17);


		// Move inherit and uninherit buttons with the node they're moving

		let inheritNode: any = d3.select('.g-hierarchy[data-primary=true] .hierarchy-inherit-button[data-gotCode="' + this.getCode() + '"]').node();
		if (inheritNode != null) {
			const heritX = (x + bbox.width - 60);
			const heritY = (y + bbox.height - 24);
			let inheritBbox = inheritNode.getBBox();
			d3.select('.g-hierarchy[data-primary=true] .hierarchy-inherit-button[data-gotCode="' + this.getCode() + '"]')
				.classed("dragging", dragging)
				.attr("transform", "translate(" + (heritX - inheritBbox.x) + " " + (heritY - inheritBbox.y) + ")");
		}

		let uninheritNode: any = d3.select('.g-hierarchy[data-primary=true] .hierarchy-uninherit-button[data-gotCode="' + this.getCode() + '"]').node();
		if (uninheritNode != null) {
			const heritX = (x + bbox.width - 71);
			const heritY = (y + bbox.height - 24);
			let uninheritBbox = uninheritNode.getBBox();
			d3.select('.g-hierarchy[data-primary=true] .hierarchy-uninherit-button[data-gotCode="' + this.getCode() + '"]')
				.classed("dragging", dragging)
				.attr("transform", "translate(" + (heritX - uninheritBbox.x) + " " + (heritY - uninheritBbox.y) + ")");
		}
	}

	getPos() {
		let select = d3.select('.g-hierarchy[data-primary=true] .svg-got-body-rect[data-gotCode="' + this.getCode() + '"]');

		return { x: parseInt(select.attr("x")), y: parseInt(select.attr("y")) };
	}

	getBbox() {
		let select = d3.select('.g-hierarchy[data-primary=true] .svg-got-body-rect[data-gotCode="' + this.getCode() + '"]');

		return { x: parseInt(select.attr("x")), y: parseInt(select.attr("y")) - 3, width: parseInt(select.attr("width")), height: parseInt(select.attr("height")) + 3 };
	}

	getTreeNode() {
		return this.treeNode;
	}
	
	hideRelatedHierarchy(): string {
	  let existingSecondary = d3.select('.g-hierarchy[data-primary="false"]');
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
			let parent = d3.select('#svg');

			let contextMenuGroup = parent.append("g").classed("g-context-menu", true);

			let relatedHierarchies = this.svgHierarchyType.getRelatedHierarchies(this.getCode());

			let bbox = this.getBbox();
			let x = bbox.x + bbox.width - 5;
			let y = bbox.y + bbox.height / 2 - 8;
			const height = 20;
			const fontSize = 8;
			const widthPadding = 10;
			const borderColor = "#aaaaaa";
			const dividerColor = "#e4e4e4";
			const fontFamily = "sans-serif";
			const titleFontSize = 10;
			const titleLabel = this.hierarchyComponent.localize("hierarchy.content.relatedHierarchies");
			const removeFromHierarchyLabel = this.hierarchyComponent.localize("hierarchy.content.removeFromHierarchy");
			const noRelatedHierLabel = this.hierarchyComponent.localize("hierarchy.content.noRelatedHierarchies");
			const hideRelatedHierarchyLabel = this.hierarchyComponent.localize("hierarchy.content.hideRelatedHierarchy");
			
      let isSecondaryHierarchyRendered = (d3.select('.g-hierarchy[data-primary="false"]').node() != null);

			// Calculate the width of our title
			let width = calculateTextWidth(titleLabel, titleFontSize);
			
			// Calculate with of remove text
			let removeWidth = calculateTextWidth(removeFromHierarchyLabel, fontSize);
			width = removeWidth > width ? removeWidth : width;
			
			if (isSecondaryHierarchyRendered)
			{
  			// Calculate width of "hide related hierarchy" label
        let hideRelatedWidth = calculateTextWidth(hideRelatedHierarchyLabel, fontSize);
        width = hideRelatedWidth > width ? hideRelatedWidth : width;
      }
			
			// Calculate the width of our context menu, which is based on how long the text inside it will be.
			// We don't know how long text is until we render it. So we'll need to loop over all the text and
			// render and destroy all of it.
			if (relatedHierarchies.length > 0)
			{
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
			}
			else
			{
			  let noHierLabelWidth = calculateTextWidth(noRelatedHierLabel, fontSize);
			  width = noHierLabelWidth > width ? noHierLabelWidth : width;
			}

			width = width + widthPadding;

      let numActions = (isSecondaryHierarchyRendered) ? 2 : 1;
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
						.on('click', function(event, node) { that.renderSecondaryHierarchy(relatedHierarchy); });

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
				};
			}
			else{
				// Text that says "No related Hierarchies"
				contextMenuGroup.append("text")
					.classed("contextmenu-relatedhiers-text", true)
					.attr("x", x + widthPadding / 2)
					.attr("y", y + (height / 2) + (fontSize / 2))
					.attr("font-size", fontSize)
					.attr("font-family", fontFamily)
					.text(noRelatedHierLabel)

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

			// Actions Title
			contextMenuGroup.append("text")
				.classed("contextmenu-relatedhiers-title", true)
				.attr("x", x + widthPadding / 2)
				.attr("y", y + (height / 2) + (titleFontSize / 2))
				.attr("font-size", titleFontSize)
				.attr("font-family", fontFamily)
				.attr("font-weight", "bold")
				.text("Actions");

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

			contextMenuGroup.append("text")
				.classed("contextmenu-relatedhiers-text", true)
				.attr("data-remove", "REPLACE---gotCode")
				.attr("x", x + widthPadding / 2)
				.attr("y", y + (height / 2) + (fontSize / 2))
				.attr("font-size", fontSize) 
				.attr("font-family", fontFamily)
				.text(removeFromHierarchyLabel)
				.style("cursor", "pointer")
				.on('click', function (event, node) { that.removeGotFromHierarchy(); });
			
      if (isSecondaryHierarchyRendered) {
  			y = y + height;
  			
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
          .on('click', function (event, node) {
            that.hideRelatedHierarchy();
            
            let existingMenu = d3.select(".g-context-menu");
            if (existingMenu.node() != null) {
              existingMenu.remove();
            }
            
            d3.select(".g-hierarchy-got-connector").remove();
          });
      }
      
			this.hierarchyComponent.calculateSvgViewBox();
		}
		else {
			existingMenu.remove();
		}
	}

	removeGotFromHierarchy(){
		
		let svgGot = this.hierarchyComponent.primarySvgHierarchy.getNodeByCode(this.geoObjectType.code);

		let startPoint: any = svgGot.getPos();
		// let svgMousePoint: any = svgPoint(event.sourceEvent.pageX, event.sourceEvent.pageY);


		// let deltaX: number = startPoint.x - svgMousePoint.x;
		// let deltaY: number = startPoint.y - svgMousePoint.y;

		let obj = this.hierarchyComponent.findGeoObjectTypeByCode(svgGot.getCode());

		this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
		});

		let message = this.localizeService.decode("confirm.modal.verify.remove.hierarchy");
		message = message.replace("{label}", obj.label.localizedValue);

		this.bsModalRef.content.message = message;
		this.bsModalRef.content.data = obj.code;

		(<ConfirmModalComponent>this.bsModalRef.content).onConfirm.subscribe(data => {
			let treeNode = svgGot.getTreeNode();
			let parent = null;
			if (treeNode.parent == null) {
				parent = "ROOT";
			}
			else {
				if (treeNode.parent.data.inheritedHierarchyCode != null) {
					parent = "ROOT";
				}
				else {
					parent = treeNode.parent.data.geoObjectType;
				}
			}

			this.hierarchyComponent.removeFromHierarchy(parent, svgGot.getCode(), (err: any) => { console.log(err) });
		});

		// (<ConfirmModalComponent>hierarchyComponent.bsModalRef.content).onCancel.subscribe(data => {
		// 	svgGot.setPos(startPoint.x, startPoint.y, false);
		// });
	}

	renderSecondaryHierarchy(relatedHierarchy: HierarchyType) {
		d3.select(".g-context-menu").remove();
		d3.select(".g-hierarchy-got-connector").remove();

		let that = this;
		let myBbox = this.getBbox();
		let svg = d3.select("#svg");

		// Remove any secondary hierarchy that may already be rendered
		if (this.hideRelatedHierarchy() === relatedHierarchy.code)
		{
		  return;
		}
		
		// Get the bounding box for our primary hierarchy
		let primaryHierBbox = (d3.select(".g-hierarchy[data-primary=true]").node() as any).getBBox();

		// Render the secondary hierarchy
		let svgHt: SvgHierarchyType = new SvgHierarchyType(this.hierarchyComponent, svg, relatedHierarchy, false, this.localizeService, this.modalService);
		svgHt.render();
		let gSecondary = d3.select('.g-hierarchy[data-primary="false"]')

		// Translate the secondary hierarchy to the right of the primary hierarchy
		let gHierarchy: any = d3.select('.g-hierarchy[data-primary="false"]').node();
		let bbox = gHierarchy.getBBox();
		let paddingLeft: number = primaryHierBbox.width + 40 + (primaryHierBbox.x - bbox.x);
		gSecondary.attr("transform", "translate(" + paddingLeft + " 0)");

		d3.select(".hierarchy-inherit-button").remove();
		d3.select(".hierarchy-uninherit-button").remove();
		let relatedGotHasParents = svgHt.getNodeByCode(this.getCode()).getTreeNode().parent != null;
		if (relatedHierarchy.organizationCode === this.geoObjectType.organizationCode && this.treeNode.parent == null && relatedGotHasParents) {
			// Add an inherit button
			const height = 15;
			const fontSize = 10;
			const buttonLabelPadding = 3;

			let group = d3.select('.g-hierarchy[data-primary=true] .g-hierarchy-tree[data-code="' + this.svgHierarchyType.getCode() + '"]')
				.append("g")
				.classed("hierarchy-inherit-button", true)
				.attr("data-gotCode", this.getCode());

			let inheritLabel = this.hierarchyComponent.localize("hierarchy.content.inherit");
			const width = calculateTextWidth(inheritLabel, fontSize) + buttonLabelPadding * 2;

			group.append("rect")
				.classed("hierarchy-inherit-bg-rect", true)
				.attr("x", myBbox.x + myBbox.width - 25 - width)
				.attr("y", myBbox.y + myBbox.height / 2 - height / 2)
				.attr("rx", 5)
				.attr("ry", 5)
				.attr("width", width)
				.attr("height", height)
				.attr("fill", "#e0e0e0")
				.attr("cursor", "pointer")
				.attr("stroke", "#6BA542")
				.attr("stroke-width", 1);

			group.append("text")
				.classed("hierarchy-inherit-bg-text", true)
				.attr("x", myBbox.x + myBbox.width - 25 - width + buttonLabelPadding)
				.attr("y", myBbox.y + myBbox.height / 2 + fontSize / 2 - 2)
				.attr("fill", "#6BA542")
				.attr("cursor", "pointer")
				.attr("font-size", fontSize + "px")
				.attr("line-height", fontSize + "px")
				.text(inheritLabel)
				.on('click', function(event, node) { that.onClickInheritHierarchy(relatedHierarchy); });
		}
		else if (relatedHierarchy.organizationCode === this.geoObjectType.organizationCode && (this.treeNode.parent != null && this.treeNode.parent.data.inheritedHierarchyCode === relatedHierarchy.code)) {
			// Add an uninherit button
			const height = 15;
			const fontSize = 10;
			const buttonLabelPadding = 3;

			let group = d3.select('.g-hierarchy[data-primary=true] .g-hierarchy-tree[data-code="' + this.svgHierarchyType.getCode() + '"]')
				.append("g")
				.classed("hierarchy-uninherit-button", true)
				.attr("data-gotCode", this.getCode());

			let inheritLabel = this.hierarchyComponent.localize("hierarchy.content.uninherit");
			const width = calculateTextWidth(inheritLabel, fontSize) + buttonLabelPadding * 2;

			group.append("rect")
				.classed("hierarchy-uninherit-bg-rect", true)
				.attr("x", myBbox.x + myBbox.width - 25 - width)
				.attr("y", myBbox.y + myBbox.height / 2 - height / 2)
				.attr("rx", 5)
				.attr("ry", 5)
				.attr("width", width)
				.attr("height", height)
				.attr("fill", "#e0e0e0")
				.attr("cursor", "pointer")
				.attr("stroke", "#6BA542")
				.attr("stroke-width", 1);

			group.append("text")
				.classed("hierarchy-uninherit-bg-text", true)
				.attr("x", myBbox.x + myBbox.width - 25 - width + buttonLabelPadding)
				.attr("y", myBbox.y + myBbox.height / 2 + fontSize / 2 - 2)
				.attr("fill", "#6BA542")
				.attr("cursor", "pointer")
				.attr("font-size", fontSize + "px")
				.attr("line-height", fontSize + "px")
				.text(inheritLabel)
				.on('click', function(event, node) { that.onClickUninheritHierarchy(); });
		}

		// Draw dotted line between the shared node in the hierarchies
		let secondaryGot = d3.select('.g-hierarchy[data-primary=false] .svg-got-body-rect[data-gotCode="' + this.getCode() + '"]');
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
				  "M" + (myBbox.x + myBbox.width) + "," + (myBbox.y + myBbox.height / 2)
				+ "H" + (((secondaryGotBbox.x) - (myBbox.x + myBbox.width)) / 2 + myBbox.x + myBbox.width)
				+ "V" + (secondaryGotBbox.y + secondaryGotBbox.height / 2)
				+ "H" + secondaryGotBbox.x
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
				"M" + (myBbox.x + myBbox.width - arrowRectD.width / 2 + ((arrowRectD.width * 2) / 3)) + "," + (myBbox.y + myBbox.height / 2 - arrowRectD.height / 2 + ((arrowRectD.height * 2) / 3))
				+ "L" + (myBbox.x + myBbox.width + arrowRectD.width / 2 - ((arrowRectD.width * 2) / 3)) + "," + (myBbox.y + myBbox.height / 2)
				+ "L" + (myBbox.x + myBbox.width - arrowRectD.width / 2 + ((arrowRectD.width * 2) / 3)) + "," + (myBbox.y + myBbox.height / 2 - arrowRectD.height / 2 + arrowRectD.height / 3)
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

