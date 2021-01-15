import * as d3 from 'd3';

import { HierarchyType } from '@registry/model/hierarchy';
import { GeoObjectType } from '@registry/model/registry';

import { HierarchyComponent } from '../hierarchy.component';
import { SvgHierarchyNode } from './svg-hierarchy-node';
import { calculateTextWidth, svgPoint } from './svg-util';
import { INHERITED_NODE_BANNER_COLOR, DEFAULT_NODE_BANNER_COLOR, RELATED_NODE_BANNER_COLOR, DEFAULT_NODE_FILL, INHERITED_NODE_FILL } from '../hierarchy.component';

import { LocalizationService } from '@shared/service';
import { BsModalService } from 'ngx-bootstrap/modal';

import { TREE_SCALE_FACTOR_X, TREE_SCALE_FACTOR_Y } from '../hierarchy.component'; 

export class SvgHierarchyType {

	public static gotRectW: number = 150;
	public static gotRectH: number = 25;
	public static gotBodyFontSize: number = 8;
	public static gotOptionsButtonFontSize: number = 12;

	public static gotHeaderW: number = 150;
	public static gotHeaderH: number = 14;
	public static gotHeaderFontSize: number = 8;
	
	public static MOUSE_HOVER_EVENT_ENTER = "mouseenter";
	
	public static MOUSE_HOVER_EVENT_MOVE = "mousemove";
	
	public static MOUSE_HOVER_EVENT_EXIT = "mouseleave";
	
	hierarchyComponent: HierarchyComponent;

	hierarchyType: HierarchyType;

	svgEl: any;

	d3Hierarchy: any;

	d3Tree: any;

	isPrimary: boolean;
	
	tooltip: any;

	public constructor(hierarchyComponent: HierarchyComponent, svgEl: any, ht: HierarchyType, isPrimary: boolean, public localizationService: LocalizationService, public modalService: BsModalService) {
		const hierarchyType = ht;

		this.hierarchyComponent = hierarchyComponent;
		this.hierarchyType = hierarchyType;
		this.svgEl = svgEl;

		this.d3Hierarchy = d3.hierarchy(hierarchyType.rootGeoObjectTypes[0]);
		this.isPrimary = isPrimary;

		this.d3Tree = d3.tree().nodeSize([ SvgHierarchyType.gotRectW*TREE_SCALE_FACTOR_X, (SvgHierarchyType.gotRectH + SvgHierarchyType.gotHeaderH)*TREE_SCALE_FACTOR_Y ]).separation((a, b) => 0.8)(this.d3Hierarchy);
	}
	
	public getD3Tree() {
		return this.d3Tree;
	}

	public getCode(): string {
		return this.hierarchyType.code
	}

	public getNodeByCode(gotCode: string): SvgHierarchyNode {
		let treeNode = this.getD3Tree().find((node) => { return node.data.geoObjectType === gotCode; });
		
		if (treeNode == null)
		{
		  return null;
		}
		
		return new SvgHierarchyNode(this.hierarchyComponent, this, this.hierarchyComponent.findGeoObjectTypeByCode(gotCode), treeNode, this.localizationService, this.modalService);
	}

	public renderHierarchyHeader(hg: any, colHeaderLabel: string) {
		let bbox = hg.node().getBBox();

		let colHeader = hg.append("g").classed("g-hierarchy-header", true);

		let headerg = hg.append("g").classed("g-hierarchy-header", true);

		const headerFontSize: number = 10;
		const iconWidth: number = 20;
		const maxHierarchyLabelLength = 200;
		
		let hierarchyLabelW: number = calculateTextWidth(this.hierarchyType.label.localizedValue, headerFontSize);
		let hierarchyLabelH:number = headerFontSize*2;
		let hierarchyLabelY:number = bbox.y + headerFontSize*1.6;
		if (hierarchyLabelW > maxHierarchyLabelLength)
		{
		  hierarchyLabelW = maxHierarchyLabelLength;
		  hierarchyLabelH = headerFontSize*3;
		  hierarchyLabelY = bbox.y + headerFontSize;
		}

		let lineWidth = bbox.width;
		let textWidth = hierarchyLabelW + iconWidth;

		if (textWidth > lineWidth) {
			lineWidth = textWidth;
		}

		// Hierarchy icon (font awesome)
		headerg.append("text").classed("hierarchy-header-icon", true)
			.attr("x", bbox.x)
			.attr("y", bbox.y)
			.style("font-family", "FontAwesome")
			.attr("fill", "grey")
			.attr("font-size", 12)
			.text('\uf0e8');

		// Hierarchy display label
		colHeader.append("foreignObject").classed("hierarchy-header-label", true)
      .attr("font-size", headerFontSize)
      .attr("stroke-linejoin", "round")
      .attr("stroke-width", 3)
      .attr("x", bbox.x + iconWidth)
      .attr("y", hierarchyLabelY)
      .attr("width", hierarchyLabelW)
      .attr("height", hierarchyLabelH)
      .append("xhtml:p")
      .attr("xmlns", "http://www.w3.org/1999/xhtml")
      .attr("text-anchor", "start")
      .attr("text-align", "left")
      .style("vertical-align", "middle")
      .style("display", "table-cell")
      .style("color", "gray")
      //.style("width", SvgHierarchyType.gotRectW - 32 + 5 + "px")
      .style("height", SvgHierarchyType.gotRectH - 4 + "px")
      .html((d: any) => this.hierarchyType.label.localizedValue)

		// Line underneath the header
		headerg.append("line").classed("hierarchy-header-line", true)
			.attr("x1", bbox.x)
			.attr("y1", bbox.y + headerFontSize)
			.attr("x2", bbox.x + lineWidth)
			.attr("y2", bbox.y + headerFontSize)
			.attr("stroke", "grey")
			.attr("stroke-width", .5);

		let headerGBbox = headerg.node().getBBox();
		headerg.attr("transform", "translate(0 -" + headerGBbox.height + ")");

		// Col header label
		colHeader.append("text").classed("hierarchy-header-label", true)
			.attr("font-size", headerFontSize + 2)
			.attr("font-weight", "bold")
			.attr("stroke-linejoin", "round")
			.attr("stroke-width", 3)
			.attr("fill", "grey")
			.attr("x", bbox.x)
			.attr("y", bbox.y)
			.text(colHeaderLabel);
	  
		colHeader.attr("transform", "translate(0 -" + headerGBbox.height * 2.5 + ")");

		return headerg;
	}

	getRelatedHierarchies(gotCode: string): string[] {
		let got: GeoObjectType = this.hierarchyComponent.findGeoObjectTypeByCode(gotCode);

		if (got.relatedHierarchies == null) {
			got.relatedHierarchies = this.hierarchyComponent.calculateRelatedHierarchies(got);
		}

    let relatedHiers: string[] = got.relatedHierarchies;

		let index = null;
		for (let i = 0; i < relatedHiers.length; ++i) {
			if (relatedHiers[i] === this.getCode()) {
				index = i;
			}
		}

		if (index != null) {
			relatedHiers.splice(index, 1);
		}

		return relatedHiers;
	}
	
	private nodeMouseover(d: any, element: any, data:any)
	{
	  d3.select("#NodeTooltip")
      .style("opacity", 1);
	}
	
	private nodeMousemove(event: any, element: any, data:any)
	{
    d3.select("#hierarchyLabel").html(this.hierarchyType.label.localizedValue);
    d3.select("#hierarchyCodeLabel").html(this.hierarchyType.code);
    
    d3.select("#geoObjectTypeLabel").html(data.data.label);
    d3.select("#geoObjectTypeCodeLabel").html(data.data.geoObjectType);
    
    d3.select("#hierarchyOrganizationLabel").html(this.hierarchyComponent.findOrganizationByCode(this.hierarchyType.organizationCode).label.localizedValue);
    d3.select("#geoObjectTypeOrganizationLabel").html(this.hierarchyComponent.findOrganizationByCode(this.hierarchyComponent.findGeoObjectTypeByCode(data.data.geoObjectType).organizationCode).label.localizedValue);
    
    let nodeTooltip: any = d3.select("#NodeTooltip").node();
    let nodeTooltipBbox: DOMRect = nodeTooltip.getBoundingClientRect();
    
    let pos = {x: event.pageX, y: event.pageY};
    const yPointerOffset: number = 50;
    const xPointerOffset: number = 0;
    
    // If overflow off bottom of page
    if ((event.pageY + nodeTooltipBbox.height + yPointerOffset) > document.documentElement.scrollHeight)
    {
      // render above mouse pointer
      pos.y = event.pageY - nodeTooltipBbox.height - yPointerOffset;
    }
    
    // If overflow off right side of page
    if ((event.pageX + nodeTooltipBbox.width + xPointerOffset) > document.documentElement.scrollWidth)
    {
      // render to the left of the mouse pointer
      pos.x = event.pageX - nodeTooltipBbox.width - xPointerOffset;
    }
    
    d3.select("#NodeTooltip")
      .style("left", pos.x + "px")
      .style("top", pos.y + "px");
	}
	
	private nodeMouseleave(d: any, element: any, data:any)
	{
	  d3.select("#NodeTooltip")
      .style("opacity", 0)
      .style("left", "-10000px")
      .style("top", "-10000px");
	}

	public render() {
		let that = this;
		let descends: any = this.d3Tree.descendants();

		d3.select('.g-hierarchy[data-primary="false"]').remove();
		if (this.isPrimary) {
			d3.select('.g-hierarchy[data-primary="true"]').remove();
		}

		let hg = this.svgEl.insert("g", ".g-hierarchy").classed("g-hierarchy", true).attr("data-code", this.hierarchyType.code).attr("data-primary", this.isPrimary);
		hg.attr("font-family", "sans-serif");

		let gtree = hg.append("g").classed("g-hierarchy-tree", true).attr("data-code", this.hierarchyType.code);

		// Edge
		gtree.append("g").classed("g-got-edge", true)
			.attr("fill", "none")
			.attr("stroke", "#555")
			.attr("stroke-opacity", 0.4)
			.attr("stroke-width", 1.5)
			.selectAll("path")
			.data(this.d3Tree.links())
			.join("path")
			//.attr("d", d3.linkVertical().x(function(d:any) { return d.x; }).y(function(d:any) { return d.y; })); // draws edges as curved lines
			.attr("d", (d: any, i) => { // draws edges as square bracket lines
				return "M" + d.source.x + "," + (d.source.y)
					+ "V" + ((d.source.y + d.target.y) / 2)
					+ "H" + d.target.x
					+ "V" + (d.target.y);
			});

		// Header on square which denotes which hierarchy it's a part of
		let gHeader = gtree.append("g").classed("g-got-header", true);
		gHeader.selectAll("rect")
			.data(descends)
			.join("rect")
			.filter(function(d: any) { return d.data.geoObjectType !== "GhostNode"; })
			.classed("svg-got-header-rect", true)
			.attr("x", (d: any) => d.x - (SvgHierarchyType.gotRectW / 2))
			.attr("y", (d: any) => d.y - SvgHierarchyType.gotRectH/2 - SvgHierarchyType.gotHeaderH + 4)
			.attr("fill", (d: any) => this.isPrimary ? (d.data.inheritedHierarchyCode != null ? INHERITED_NODE_BANNER_COLOR : DEFAULT_NODE_BANNER_COLOR) : RELATED_NODE_BANNER_COLOR)
			.attr("width", SvgHierarchyType.gotHeaderW)
			.attr("height", SvgHierarchyType.gotHeaderH)
			.attr("cursor", (d: any) => this.isPrimary ? (d.data.inheritedHierarchyCode != null ? null : "grab") : null)
			.attr("rx", 3)
			.attr("data-gotCode", (d: any) => d.data.geoObjectType)
			.attr("data-inherited", (d: any) =>
				d.data.inheritedHierarchyCode != null
			)
			.on(SvgHierarchyType.MOUSE_HOVER_EVENT_ENTER, function(event: any, data: any) {that.nodeMouseover(event, this, data);})
      .on(SvgHierarchyType.MOUSE_HOVER_EVENT_MOVE, function(event: any, data: any) {that.nodeMousemove(event, this, data);})
      .on(SvgHierarchyType.MOUSE_HOVER_EVENT_EXIT, function(event: any, data: any) {that.nodeMouseleave(event, this, data);});

		// Write the name of the hierarchy on the header
		gHeader.selectAll("foreignObject")
			.data(descends)
			.join("foreignObject")
			.filter(function(d: any) { return d.data.geoObjectType !== "GhostNode"; })
			.classed("svg-got-header-rect", true)
			.attr("x", (d: any) => 
			    d.x - (SvgHierarchyType.gotHeaderW / 2)
			  )
			.attr("y", (d: any) => d.y - SvgHierarchyType.gotRectH/2 - SvgHierarchyType.gotHeaderH + 4)
			.attr("font-size", SvgHierarchyType.gotHeaderFontSize + "px")
			.attr("cursor", (d: any) => this.isPrimary ? (d.data.inheritedHierarchyCode != null ? null : "grab") : null)
			.attr("data-gotCode", (d: any) => d.data.geoObjectType)
			.attr("data-inherited", (d: any) => d.data.inheritedHierarchyCode != null)
			.attr("font-family", "sans-serif")
			.attr("font-weight", "bold")
      .attr("font-size", SvgHierarchyType.gotHeaderFontSize)
      .attr("stroke-linejoin", "round")
      .attr("stroke-width", 3)
      .attr("width", SvgHierarchyType.gotHeaderW)
      .attr("height", SvgHierarchyType.gotHeaderH - 4)
			.append("xhtml:p")
      .attr("xmlns", "http://www.w3.org/1999/xhtml")
      .attr("fill", "white")
      .attr("height", SvgHierarchyType.gotHeaderH - 4)
      .style("text-align", "center")
      .style("vertical-align", "middle")
      .style("display", "table-cell")
      .style("color", "white")
      .style("height", (SvgHierarchyType.gotHeaderH - 4) + "px")
      .style("width", SvgHierarchyType.gotHeaderW + "px")
      .html(function (d: any) {
        let name = d.data.inheritedHierarchyCode != null ? that.hierarchyComponent.findHierarchyByCode(d.data.inheritedHierarchyCode).label.localizedValue : that.hierarchyType.label.localizedValue;
        calculateTextWidth(name, SvgHierarchyType.gotHeaderFontSize) > (SvgHierarchyType.gotHeaderW - 5) ? name = name.substring(0,34) + "..." : true;
        return name;
       })
      .on(SvgHierarchyType.MOUSE_HOVER_EVENT_ENTER, function(event: any, data: any) {that.nodeMouseover(event, this, data);})
      .on(SvgHierarchyType.MOUSE_HOVER_EVENT_MOVE, function(event: any, data: any) {that.nodeMousemove(event, this, data);})
      .on(SvgHierarchyType.MOUSE_HOVER_EVENT_EXIT, function(event: any, data: any) {that.nodeMouseleave(event, this, data);});

		// GeoObjectType Body Square 
		gtree.append("g").classed("g-got", true)
			.selectAll("rect")
			.data(descends)
			.join("rect")
			.filter(function(d: any) { return d.data.geoObjectType !== "GhostNode"; })
			.classed("svg-got-body-rect", true)
			.attr("x", (d: any) => d.x - (SvgHierarchyType.gotRectW / 2))
			.attr("y", (d: any) => d.y - (SvgHierarchyType.gotRectH / 2))
			.attr("fill", (d: any) => d.data.inheritedHierarchyCode != null ? INHERITED_NODE_FILL : DEFAULT_NODE_FILL)
			.attr("width", SvgHierarchyType.gotRectW)
			.attr("height", SvgHierarchyType.gotRectH)
			.attr("rx", 3)
			.attr("cursor", (d: any) => this.isPrimary ? (d.data.inheritedHierarchyCode != null ? null : "grab") : null)
			.attr("data-gotCode", (d: any) => d.data.geoObjectType)
			.attr("data-inherited", (d: any) => d.data.inheritedHierarchyCode != null)
			.each(function(d: any) {
				if (d.data.geoObjectType != "GhostNode") {
					if (d.data.activeDropZones) {
						d.data.dropZoneBbox = { x: d.x - SvgHierarchyType.gotRectW / 2, y: d.y - SvgHierarchyType.gotRectH * 2, width: SvgHierarchyType.gotRectW * 2 + 100, height: SvgHierarchyType.gotRectH * 4 };
					}
					else {
						d.data.dropZoneBbox = { x: d.x - SvgHierarchyType.gotRectW / 2, y: d.y - SvgHierarchyType.gotRectH / 2, width: SvgHierarchyType.gotRectW, height: SvgHierarchyType.gotRectH };
					}
				}
			})
			.on(SvgHierarchyType.MOUSE_HOVER_EVENT_ENTER, function(event: any, data: any) {that.nodeMouseover(event, this, data);})
      .on(SvgHierarchyType.MOUSE_HOVER_EVENT_MOVE, function(event: any, data: any) {that.nodeMousemove(event, this, data);})
      .on(SvgHierarchyType.MOUSE_HOVER_EVENT_EXIT, function(event: any, data: any) {that.nodeMouseleave(event, this, data);});

		// Arrows on Edges
		const arrowRectD = { height: 7, width: 10 };
		let gArrow = gtree.append("g").classed("g-got-connector-arrow", true);
		gArrow.selectAll("rect").data(this.d3Tree.links()).join("rect") // .filter(function(d:any){return d.data.geoObjectType !== "GhostNode";})
			.classed("got-connector-arrow-rect", true)
			.attr("x", (d: any) => d.source.x - arrowRectD.width / 2)
			.attr("y", (d: any) => d.source.y + SvgHierarchyType.gotRectH / 2 - arrowRectD.height / 2)
			.attr("width", arrowRectD.width)
			.attr("height", arrowRectD.height)
			.attr("fill", (d: any) => this.isPrimary ? (d.source.data.inheritedHierarchyCode != null ? INHERITED_NODE_BANNER_COLOR : DEFAULT_NODE_BANNER_COLOR) : RELATED_NODE_BANNER_COLOR);
		gArrow.selectAll("path").data(this.d3Tree.links()).join("path") // .filter(function(d:any){return d.data.geoObjectType !== "GhostNode";})
			.classed("got-connector-arrow-path", true)
			.attr("fill", "none")
			.attr("stroke", "white")
			.attr("stroke-width", 1.5)
			.attr("d", (d: any) => "M" + (d.source.x - arrowRectD.width / 2 + ((arrowRectD.width * 2) / 3)) + "," + (d.source.y + SvgHierarchyType.gotRectH / 2 - arrowRectD.height / 2 + ((arrowRectD.height * 2) / 3))
				+ "L" + (d.source.x) + "," + (d.source.y + SvgHierarchyType.gotRectH / 2 - arrowRectD.height / 2 + (arrowRectD.height / 3))
				+ "L" + (d.source.x - arrowRectD.width / 2 + (arrowRectD.width / 3)) + "," + (d.source.y + SvgHierarchyType.gotRectH / 2 + arrowRectD.height / 2 - arrowRectD.height / 3)
			);

		// Ghost Drop Zone (Sibling) Backer
		gtree.append("g").classed("g-sibling-ghost-backer", true)
			.selectAll("rect")
			.data(descends)
			.join("rect")
			.filter(function(d: any) { return d.data.geoObjectType === "GhostNode"; })
			.classed("svg-sibling-ghost-backer-dz", true)
			.attr("x", (d: any) => d.x - (SvgHierarchyType.gotRectW / 2))
			.attr("y", (d: any) => d.y - (SvgHierarchyType.gotRectH / 2))
			.attr("width", SvgHierarchyType.gotRectW)
			.attr("height", SvgHierarchyType.gotRectH)
			.attr("fill", "white")

		// Ghost Drop Zone (Sibling) Body Rectangle
		gtree.append("g").classed("g-sibling-ghost-body", true)
			.selectAll("rect")
			.data(descends)
			.join("rect")
			.filter(function(d: any) { return d.data.geoObjectType === "GhostNode"; })
			.classed("svg-sibling-ghost-body-dz", true)
			.attr("x", (d: any) => d.x - (SvgHierarchyType.gotRectW / 2))
			.attr("y", (d: any) => d.y - (SvgHierarchyType.gotRectH / 2))
			.attr("width", SvgHierarchyType.gotRectW)
			.attr("height", SvgHierarchyType.gotRectH)
			.attr("fill", "none")
			.attr("stroke", "#6BA542")
			.attr("stroke-width", "1")
			.attr("stroke-dasharray", "5,5")
			.attr("data-gotCode", (d: any) => d.data.geoObjectType)
			
		// GeoObjectType label
		gtree.append("g").classed("g-got-codelabel", true)
			.attr("font-family", "sans-serif")
			.attr("font-size", SvgHierarchyType.gotBodyFontSize)
			.attr("stroke-linejoin", "round")
			.attr("stroke-width", 3)
			.selectAll("foreignObject")
			.data(descends)
			.join("foreignObject")
			.classed("svg-got-label-text", true)
			.attr("x", (d: any) => d.x - (SvgHierarchyType.gotRectW / 2) + 5)
			.attr("y", (d: any) => d.y - (SvgHierarchyType.gotRectH / 2) + 2)
			.attr("width", SvgHierarchyType.gotRectW - 32 + 5)
			.attr("height", SvgHierarchyType.gotRectH - 4)
			// .filter(function(d: any) {
			// 	return calculateTextWidth(d.data.label, 10) > SvgHierarchyType.gotRectW - 32 + 5;
			// })
			// .style("height", SvgHierarchyType.gotRectH + 20 + "px")
			.attr("cursor", (d: any) => this.isPrimary ? (d.data.inheritedHierarchyCode != null ? null : "grab") : null)
			.attr("data-gotCode", (d: any) => d.data.geoObjectType)
			.attr("data-inherited", (d: any) => d.data.inheritedHierarchyCode != null)
			.append("xhtml:p")
			.attr("xmlns", "http://www.w3.org/1999/xhtml")
			.attr("text-anchor", "start")
			.attr("text-align", "left")
			.style("vertical-align", "middle")
			.style("display", "table-cell")
			.style("width", SvgHierarchyType.gotRectW - 32 + 5 + "px")
			.style("height", SvgHierarchyType.gotRectH - 4 + "px")
			.html((d: any) => d.data.label)
			.on(SvgHierarchyType.MOUSE_HOVER_EVENT_ENTER, function(event: any, data: any) {that.nodeMouseover(event, this, data);})
      .on(SvgHierarchyType.MOUSE_HOVER_EVENT_MOVE, function(event: any, data: any) {that.nodeMousemove(event, this, data);})
      .on(SvgHierarchyType.MOUSE_HOVER_EVENT_EXIT, function(event: any, data: any) {that.nodeMouseleave(event, this, data);});

		let headerg;
		if (this.isPrimary) {
			gtree.append("g").classed("g-got-relatedhiers-button", true)
				.selectAll("text")
				.data(descends)
				.join("text")
				.filter(function(d: any) {
					return (d.data.geoObjectType === "GhostNode" ? false : true) && d.data.inheritedHierarchyCode == null;
				})
				.classed("svg-got-relatedhiers-button", true)
				.attr("data-gotCode", (d: any) => d.data.geoObjectType)
				.attr("x", (d: any) => d.x + (SvgHierarchyType.gotRectW / 2) - (SvgHierarchyType.gotOptionsButtonFontSize) - 3)
				.attr("y", (d: any) => d.y + (SvgHierarchyType.gotOptionsButtonFontSize*0.8) - SvgHierarchyType.gotOptionsButtonFontSize/2)
				.style("font-family", "FontAwesome")
				.style("cursor", "pointer")
				.style("fill", "#767676")
				.style("font-size", SvgHierarchyType.gotOptionsButtonFontSize + "px")
				.text('\uf013')
				.on('click', function(event, node) { that.getNodeByCode(node.data.geoObjectType).renderRelatedHierarchiesMenu(); });

			headerg = this.renderHierarchyHeader(hg, "Selected Hierarchy");
		}
		else {
			headerg = this.renderHierarchyHeader(hg, "Related Hierarchy");
		}


		let paddingTop = (headerg.node().getBBox().height + 20);
		//gtree.attr("transform", "translate(0 " + paddingTop + ")");
	}
}
