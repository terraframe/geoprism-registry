import * as d3 from 'd3';

import { HierarchyType } from '../hierarchy';
import { GeoObjectType } from '../registry';

import { SvgHierarchyNode } from './svg-hierarchy-node';
import { calculateTextWidth } from './svg-util';
import { SvgController, INHERITED_NODE_BANNER_COLOR, DEFAULT_NODE_BANNER_COLOR, RELATED_NODE_BANNER_COLOR, DEFAULT_NODE_FILL, INHERITED_NODE_FILL } from './svg-controller';

export class SvgHierarchyType {

	public static gotRectW: number = 150;
	public static gotRectH: number = 25;

	public static gotHeaderW: number = 150;
	public static gotHeaderH: number = 14;

	hierarchyComponent: SvgController;

	hierarchyType: HierarchyType;

	svgEl: any;

	d3Hierarchy: any;

	d3Tree: any;

	isPrimary: boolean;

	public constructor(hierarchyComponent: SvgController, svgEl: any, ht: HierarchyType, isPrimary: boolean) {
		const hierarchyType = JSON.parse(JSON.stringify(ht));
//		const hierarchyType = ht;

		this.hierarchyComponent = hierarchyComponent;
		this.hierarchyType = hierarchyType;
		this.svgEl = svgEl;

		this.d3Hierarchy = d3.hierarchy(hierarchyType.rootGeoObjectTypes[0]);
		this.isPrimary = isPrimary;

		this.d3Tree = d3.tree().nodeSize([300, 85]).separation((a, b) => 0.8)(this.d3Hierarchy);
	}

	public getD3Tree() {
		return this.d3Tree;
	}

	public getCode(): string {
		return this.hierarchyType.code
	}

	public getNodeByCode(gotCode: string): SvgHierarchyNode {
		let treeNode = this.getD3Tree().find((node) => { return node.data.geoObjectType === gotCode; });

		return new SvgHierarchyNode(this.hierarchyComponent, this, this.hierarchyComponent.findGeoObjectTypeByCode(gotCode), treeNode);
	}

	public renderHierarchyHeader(hg: any, colHeaderLabel: string) {
		let bbox = hg.node().getBBox();

		let colHeader = hg.append("g").classed("g-hierarchy-header", true);

		let headerg = hg.append("g").classed("g-hierarchy-header", true);

		const fontSize = 14;
		const iconWidth = 20;

		let lineWidth = bbox.width;
		let textWidth = calculateTextWidth(this.hierarchyType.label.localizedValue, fontSize) + iconWidth;

		if (textWidth > lineWidth) {
			lineWidth = textWidth;
		}

		// Hierarchy icon (font awesome)
		headerg.append("text").classed("hierarchy-header-icon", true)
			.attr("x", bbox.x)
			.attr("y", bbox.y)
			.style("font-family", "FontAwesome")
			.attr("fill", "grey")
			.text('\uf0e8');

		// Hierarchy display label
		headerg.append("text").classed("hierarchy-header-label", true)
			.attr("font-size", fontSize)
			.attr("stroke-linejoin", "round")
			.attr("stroke-width", 3)
			.attr("fill", "grey")
			.attr("x", bbox.x + iconWidth)
			.attr("y", bbox.y)
			.text(this.hierarchyType.label.localizedValue);

		// Line underneath the header
		headerg.append("line").classed("hierarchy-header-line", true)
			.attr("x1", bbox.x)
			.attr("y1", bbox.y + fontSize)
			.attr("x2", bbox.x + lineWidth)
			.attr("y2", bbox.y + fontSize)
			.attr("stroke", "grey")
			.attr("stroke-width", 1);

		let headerGBbox = headerg.node().getBBox();
		headerg.attr("transform", "translate(0 -" + headerGBbox.height + ")");

		// Col header label
		colHeader.append("text").classed("hierarchy-header-label", true)
			.attr("font-size", fontSize + 4)
			.attr("stroke-linejoin", "round")
			.attr("stroke-width", 3)
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

		let relatedHiers: string[] = JSON.parse(JSON.stringify(got.relatedHierarchies));

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
			.attr("y", (d: any) => d.y - SvgHierarchyType.gotRectH + 2)
			.attr("fill", (d: any) => this.isPrimary ? (d.data.inheritedHierarchyCode != null ? INHERITED_NODE_BANNER_COLOR : DEFAULT_NODE_BANNER_COLOR) : RELATED_NODE_BANNER_COLOR)
			.attr("width", SvgHierarchyType.gotHeaderW)
			.attr("height", SvgHierarchyType.gotHeaderH)
			.attr("cursor", (d: any) => this.isPrimary ? (d.data.inheritedHierarchyCode != null ? null : "grab") : null)
			.attr("rx", 3)
			.attr("data-gotCode", (d: any) => d.data.geoObjectType)
			.attr("data-inherited", (d: any) =>
				d.data.inheritedHierarchyCode != null
			);

		// Write the name of the hierarchy on the header if its inherited
		gHeader.selectAll("text")
			.data(descends)
			.join("text")
			.filter(function(d: any) { return d.data.geoObjectType !== "GhostNode" && d.data.inheritedHierarchyCode != null; })
			.classed("svg-got-header-rect", true)
			.attr("x", (d: any) => d.x - calculateTextWidth(that.hierarchyComponent.findHierarchyByCode(d.data.inheritedHierarchyCode).label.localizedValue, 7) / 2)
			.attr("y", (d: any) => d.y - SvgHierarchyType.gotRectH + 4 + 6)
			.attr("font-size", "8px")
			.text((d: any) => that.hierarchyComponent.findHierarchyByCode(d.data.inheritedHierarchyCode).label.localizedValue)
			.attr("cursor", (d: any) => this.isPrimary ? (d.data.inheritedHierarchyCode != null ? null : "grab") : null)
			.attr("data-gotCode", (d: any) => d.data.geoObjectType)
			.attr("data-inherited", (d: any) => d.data.inheritedHierarchyCode != null);

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
					d.data.gotBodySquare = this;
				}
			});

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
			.attr("font-size", 10)
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
			.attr("cursor", (d: any) => this.isPrimary ? (d.data.inheritedHierarchyCode != null ? null : "grab") : null)
			.attr("data-gotCode", (d: any) => d.data.geoObjectType)
			.attr("data-inherited", (d: any) => d.data.inheritedHierarchyCode != null)
			.append("xhtml:p")
			.attr("xmlns", "http://www.w3.org/1999/xhtml")
			.style("text-align", "center")
			.style("vertical-align", "middle")
			.style("display", "table-cell")
			.style("width", SvgHierarchyType.gotRectW - 32 + 5 + "px")
			.style("height", SvgHierarchyType.gotRectH - 4 + "px")
			.html((d: any) => d.data.label)
			.filter(function(d: any) {
				return calculateTextWidth(d.data.label, 10) > SvgHierarchyType.gotRectW - 32 + 5;
			})
			.style("font-size", "8px");

		let headerg;
		if (this.isPrimary) {
			gtree.append("g").classed("g-got-relatedhiers-button", true)
				.selectAll("text")
				.data(descends)
				.join("text")
				.filter(function(d: any) {
					return (d.data.geoObjectType === "GhostNode" ? false : that.getRelatedHierarchies(d.data.geoObjectType).length > 0) && d.data.inheritedHierarchyCode == null;
				})
				.classed("svg-got-relatedhiers-button", true)
				.attr("data-gotCode", (d: any) => d.data.geoObjectType)
				.attr("x", (d: any) => d.x + (SvgHierarchyType.gotRectW / 2) - 20)
				.attr("y", (d: any) => d.y + 5)
				.style("font-family", "FontAwesome")
				.style("cursor", "pointer")
				.text('\uf0c1')
				.on('click', function(event, node) { that.getNodeByCode(node.data.geoObjectType).renderRelatedHierarchiesMenu(); });

			headerg = this.renderHierarchyHeader(hg, "Selected Hierarchy");
		}
		else {
			headerg = this.renderHierarchyHeader(hg, "Inherited Hierarchy");
		}


		let paddingTop = (headerg.node().getBBox().height + 20);
		//gtree.attr("transform", "translate(0 " + paddingTop + ")");
	}
}
