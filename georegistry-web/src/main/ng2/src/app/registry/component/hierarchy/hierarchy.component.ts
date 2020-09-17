import { Component, OnInit, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from "@angular/common/http";

import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { ContextMenuService, ContextMenuComponent } from 'ngx-contextmenu';

import { CreateHierarchyTypeModalComponent } from './modals/create-hierarchy-type-modal.component';
import { AddChildToHierarchyModalComponent } from './modals/add-child-to-hierarchy-modal.component';
import { CreateGeoObjTypeModalComponent } from './modals/create-geoobjtype-modal.component';
import { ManageGeoObjectTypeModalComponent } from './modals/manage-geoobjecttype-modal.component';

import { ErrorHandler, ConfirmModalComponent, ErrorModalComponent } from '@shared/component';
import { LocalizationService, AuthService } from '@shared/service';
import { ModalTypes } from '@shared/model/modal'

import { HierarchyType, HierarchyNode } from '@registry/model/hierarchy';
import { GeoObjectType } from '@registry/model/registry';
import { Organization } from '@shared/model/core';
import { RegistryService, HierarchyService } from '@registry/service';

import * as d3 from 'd3';

class Instance {
	active: boolean;
	label: string;
}

interface DropTarget {
  dropSelector: string;
  onDrag(dragEl: Element, dropEl: Element, event: any): void;
  onDrop(dragEl: Element, event: any);
  [others: string]: any;
}

function svgPoint(x, y) {
  let svg: any = d3.select("#svg").node();
  var pt = svg.createSVGPoint();

  pt.x = x;
  pt.y = y;

  return pt.matrixTransform(svg.getScreenCTM().inverse());
}

let isPointWithin = function(point: {x: number, y: number}, bbox: {x: number, y: number, width: number, height: number}) {
  return point.y > bbox.y && point.y < (bbox.y + bbox.height) && point.x > bbox.x && point.x < (bbox.x + bbox.width);
}
let isBboxPartiallyWithin = function(bbox1: {x: number, y: number, width: number, height: number}, bbox2: {x: number, y: number, width: number, height: number}) {
  return isPointWithin({x:bbox1.x, y:bbox1.y}, bbox2) || isPointWithin({x:bbox1.x + bbox1.width, y:bbox1.y + bbox1.height}, bbox2)
      || isPointWithin({x:bbox1.x + bbox1.width, y:bbox1.y}, bbox2) || isPointWithin({x:bbox1.x, y:bbox1.y + bbox1.height}, bbox2);
}
let isBboxTotallyWithin = function(bbox1: {x: number, y: number, width: number, height: number}, bbox2: {x: number, y: number, width: number, height: number}) {
  return isPointWithin({x:bbox1.x, y:bbox1.y}, bbox2) && isPointWithin({x:bbox1.x + bbox1.width, y:bbox1.y + bbox1.height}, bbox2)
      && isPointWithin({x:bbox1.x + bbox1.width, y:bbox1.y}, bbox2) && isPointWithin({x:bbox1.x, y:bbox1.y + bbox1.height}, bbox2);
}
let getBboxFromSelection = function(selection: any) {
  return {x: parseInt(selection.attr("x")), y: parseInt(selection.attr("y")), width: parseInt(selection.attr("width")), height: parseInt(selection.attr("height"))};
}

let calculateTextWidth = function(text: string, fontSize: number): number
{
  let svg = d3.select("#svg");

  let textCalcGroup = svg.append("g").classed("g-text-calc", true);

  let textEl = textCalcGroup.append("text")
      .attr("x", -5000)
      .attr("y", -5000)
      .style("font-size", fontSize)
      .text(text);
  
  let bbox = textEl.node().getBBox();
  
  d3.select(".g-text-calc").remove();
  
  return bbox.width;
}

export class SvgHierarchyType {

  public static gotRectW: number = 150;
  public static gotRectH: number = 25;
  
  public static gotHeaderW: number = 150;
  public static gotHeaderH: number = 12;

  hierarchyComponent: HierarchyComponent;
  
  hierarchyType: HierarchyType;
  
  svgEl: any;
  
  d3Hierarchy: any;
  
  d3Tree: any;
  
  isPrimary: boolean;
  
  public constructor(hierarchyComponent: HierarchyComponent, svgEl: any, hierarchyType: HierarchyType, isPrimary: boolean)
  {
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
  
  public getNodeByCode(gotCode: string): SvgHierarchyNode
  {
    let treeNode = this.getD3Tree().find((node)=>{return node.data.geoObjectType === gotCode;});
  
    return new SvgHierarchyNode(this.hierarchyComponent, this, this.hierarchyComponent.findGeoObjectTypeByCode(gotCode), treeNode);
  }
  
  public renderHierarchyHeader(hg: any) {
    let bbox = hg.node().getBBox();
  
    let headerg = hg.append("g").classed("g-hierarchy-header", true);
  
    const fontSize = 14;
    const iconWidth = 20;
    
    let lineWidth = bbox.width;
    let textWidth = calculateTextWidth(this.hierarchyType.label.localizedValue, fontSize) + iconWidth;
    
    if (textWidth > lineWidth)
    {
      lineWidth = textWidth;
    }
    
    // Hierarchy icon (font awesome)
    headerg.append("text").classed("hierarchy-header-icon", true)
          .attr("x", bbox.x)
          .attr("y", bbox.y)
          .style("font-family", "FontAwesome")
          .text('\uf0e8');
  
    // Hierarchy display label
    headerg.append("text").classed("hierarchy-header-label", true)
        .attr("font-size", fontSize)
        .attr("stroke-linejoin", "round")
        .attr("stroke-width", 3)
        .attr("x", bbox.x + iconWidth)
        .attr("y", bbox.y)
        .text(this.hierarchyType.label.localizedValue);
    
    // Line underneath the header
    headerg.append("line").classed("hierarchy-header-line", true)
        .attr("x1", bbox.x)
        .attr("y1", bbox.y + fontSize)
        .attr("x2", bbox.x + lineWidth)
        .attr("y2", bbox.y + fontSize)
        .attr("stroke", "black")
        .attr("stroke-width", 1);
        
    let headerGBbox = headerg.node().getBBox();
    headerg.attr("transform", "translate(0 -" + headerGBbox.height + ")");
        
    return headerg;
  }
  
  getRelatedHierarchies(gotCode: string): string[]
  {
    let relatedHiers: string[] = JSON.parse(JSON.stringify(this.hierarchyComponent.findGeoObjectTypeByCode(gotCode).relatedHierarchies));
    
    let index = null;
    for (let i = 0; i < relatedHiers.length; ++i)
    {
      if (relatedHiers[i] === this.getCode())
      {
        index = i;
      }
    }
    
    if (index != null)
    {
      relatedHiers.splice(index, 1);
    }
    
    return relatedHiers;
  }
  
  public render() {
    let that = this;
    let descends:any = this.d3Tree.descendants();
    
    d3.select('.g-hierarchy[data-primary="false"]').remove();
    if (this.isPrimary)
    {
      d3.select('.g-hierarchy[data-primary="true"]').remove();
    }
    
    let hg = this.svgEl.insert("g",".g-hierarchy").classed("g-hierarchy", true).attr("data-code", this.hierarchyType.code).attr("data-primary", this.isPrimary);
    hg.attr("font-family", "sans-serif")
    
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
        .attr("d", (d:any,i) => { // draws edges as square bracket lines
          return "M" + d.source.x + "," + (d.source.y)
                 + "V" + ((d.source.y + d.target.y)/2)
                 + "H" + d.target.x
                 + "V" + (d.target.y);
        });
  
    // Header on square which denotes which hierarchy it's a part of
    gtree.append("g").classed("g-got-header", true)
        .selectAll("rect")
        .data(descends)
        .join("rect")
        .filter(function(d:any){return d.data.geoObjectType !== "GhostNode";})
          .classed("svg-got-header-rect", true)
          .attr("x", (d: any) => d.x - (SvgHierarchyType.gotRectW / 2))
          .attr("y", (d: any) => d.y - SvgHierarchyType.gotRectH + 8)
          .attr("fill", (d: any) => this.isPrimary ? (d.data.inherited ? "#848000" : "#cc0000") : "#b3ad00")
          .attr("width", SvgHierarchyType.gotHeaderW)
          .attr("height", SvgHierarchyType.gotHeaderH)
          .attr("cursor", (d:any) => this.isPrimary ? (d.data.inherited ? null : "grab") : null)
          .attr("rx", 5)
          .attr("data-gotCode", (d: any) => d.data.geoObjectType);
          
    // GeoObjectType Body Square
    gtree.append("g").classed("g-got", true)
        .selectAll("rect")
        .data(descends)
        .join("rect")
        .filter(function(d:any){return d.data.geoObjectType !== "GhostNode";})
          .classed("svg-got-body-rect", true)
          .attr("x", (d: any) => d.x - (SvgHierarchyType.gotRectW / 2))
          .attr("y", (d: any) => d.y - (SvgHierarchyType.gotRectH / 2))
          .attr("fill", (d: any) => d.data.inherited ? "#565656" : "#e0e0e0")
          .attr("width", SvgHierarchyType.gotRectW)
          .attr("height", SvgHierarchyType.gotRectH)
          .attr("rx", 5)
          .attr("cursor", (d:any) => this.isPrimary ? (d.data.inherited ? null : "grab") : null)
          .attr("data-gotCode", (d: any) => d.data.geoObjectType)
          .each(function(d:any) {
            if (d.data.geoObjectType != "GhostNode")
            {
              if (d.data.activeDropZones)
              {
                d.data.dropZoneBbox = {x: d.x - SvgHierarchyType.gotRectW/2, y: d.y - SvgHierarchyType.gotRectH*2, width: SvgHierarchyType.gotRectW*2 + 100, height: SvgHierarchyType.gotRectH*4};
              }
              else
              {
                d.data.dropZoneBbox = {x: d.x - SvgHierarchyType.gotRectW/2, y: d.y - SvgHierarchyType.gotRectH/2, width: SvgHierarchyType.gotRectW, height: SvgHierarchyType.gotRectH};
              }
              d.data.gotBodySquare = this;
            }
          });
          
    // Ghost Drop Zone (Sibling) Backer
    gtree.append("g").classed("g-sibling-ghost-backer", true)
        .selectAll("rect")
        .data(descends)
        .join("rect")
        .filter(function(d:any){return d.data.geoObjectType === "GhostNode";})
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
        .filter(function(d:any){return d.data.geoObjectType === "GhostNode";})
          .classed("svg-sibling-ghost-body-dz", true)
          .attr("x", (d: any) => d.x - (SvgHierarchyType.gotRectW / 2))
          .attr("y", (d: any) => d.y - (SvgHierarchyType.gotRectH / 2))
          .attr("width", SvgHierarchyType.gotRectW)
          .attr("height", SvgHierarchyType.gotRectH)
          .attr("fill", "none")
          .attr("stroke", "black")
          .attr("stroke-width", "1")
          .attr("stroke-dasharray", "5,5")
          .attr("data-gotCode", (d: any) => d.data.geoObjectType)
    
    // GeoObjectType label
    gtree.append("g").classed("g-got-codelabel", true)
        .attr("font-family", "sans-serif")
        .attr("font-size", 10)
        .attr("stroke-linejoin", "round")
        .attr("stroke-width", 3)
      .selectAll("text")
      .data(descends)
      .join("text")
        .classed("svg-got-label-text", true)
        .attr("x", (d:any) => d.x - 70)
        .attr("y", (d:any) => d.y - 4)
        .attr("dx", "0.31em")
        .attr("dy", 6)
        .attr("cursor", (d:any) => this.isPrimary ? (d.data.inherited ? null : "grab") : null)
        .text((d:any) => d.data.label)
        .attr("data-gotCode", (d: any) => d.data.geoObjectType);
        
    if (this.isPrimary)
    {
      gtree.append("g").classed("g-got-relatedhiers-button", true)
          .selectAll("text")
          .data(descends)
          .join("text")
          .filter(function(d:any){
              return (d.data.geoObjectType === "GhostNode" ? false : that.getRelatedHierarchies(d.data.geoObjectType).length > 0) && !d.data.inherited;
            })
            .classed("svg-got-relatedhiers-button", true)
            .attr("data-gotCode", (d: any) => d.data.geoObjectType)
            .attr("x", (d:any) => d.x + (SvgHierarchyType.gotRectW / 2) - 20)
            .attr("y", (d:any) => d.y + 5)
            .style("font-family", "FontAwesome")
            .style("cursor", "pointer")
            .text('\uf0c1')
            .on('click', function(event,node){ that.getNodeByCode(node.data.geoObjectType).onClickShowRelatedHierarchies(event); });
    }
          
    let headerg = this.renderHierarchyHeader(hg);
    
    let paddingTop = (headerg.node().getBBox().height + 20);
    //gtree.attr("transform", "translate(0 " + paddingTop + ")");
  }
}

export class SvgHierarchyNode {
  
  private hierarchyComponent: HierarchyComponent;
  
  private svgHierarchyType: SvgHierarchyType;
  
  private geoObjectType: GeoObjectType;
  
  private treeNode: any;
  
  constructor(hierarchyComponent: HierarchyComponent, svgHierarchyType: SvgHierarchyType, geoObjectType: GeoObjectType, treeNode: any)
  {
    this.hierarchyComponent = hierarchyComponent;
    this.svgHierarchyType = svgHierarchyType;
    this.geoObjectType = geoObjectType;
    this.treeNode = treeNode;
  }
  
  getCode(): string
  {
    return this.geoObjectType.code;
  }
  
  setPos(x: number, y: number, dragging: boolean)
  {
    let bbox = this.getBbox();
  
    // Move the GeoObjectType with the pointer when they move their mouse
    d3.select('.g-hierarchy[data-primary=true] .svg-got-body-rect[data-gotCode="' + this.getCode() + '"]')
        .classed("dragging", dragging)
        .attr("x", x)
        .attr("y", y);
        
    d3.select('.g-hierarchy[data-primary=true] .svg-got-header-rect[data-gotCode="' + this.getCode() + '"]')
        .classed("dragging", dragging)
        .attr("x", x)
        .attr("y", y - SvgHierarchyType.gotRectH/2 + 8);
        
    d3.select('.g-hierarchy[data-primary=true] .svg-got-label-text[data-gotCode="' + this.getCode() + '"]')
        .classed("dragging", dragging)
        .attr("x", x + 5)
        .attr("y", y + 8);
        
    d3.select('.g-hierarchy[data-primary=true] .svg-got-relatedhiers-button[data-gotCode="' + this.getCode() + '"]')
        .classed("dragging", dragging)
        .attr("x", x + bbox.width - 20)
        .attr("y", y + 17);
  }
  
  getPos()
  {
    let select = d3.select('.g-hierarchy[data-primary=true] .svg-got-body-rect[data-gotCode="' + this.getCode() + '"]');
  
    return {x: parseInt(select.attr("x")), y: parseInt(select.attr("y"))};
  }
  
  getBbox()
  {
    let select = d3.select('.g-hierarchy[data-primary=true] .svg-got-body-rect[data-gotCode="' + this.getCode() + '"]');
  
    return {x: parseInt(select.attr("x")), y: parseInt(select.attr("y")) - 3, width: parseInt(select.attr("width")), height: parseInt(select.attr("height")) + 3};
  }
  
  getTreeNode()
  {
    return this.treeNode;
  }
  
  // Renders a context menu
  onClickShowRelatedHierarchies(event: MouseEvent)
  {
    let that = this;
    let existingMenu = d3.select(".g-context-menu");
    
    if (existingMenu.node() == null)
    {
      let parent = d3.select('g.g-hierarchy-tree[data-code="' + this.svgHierarchyType.hierarchyType.code + '"]');
      
      let contextMenuGroup = parent.append("g").classed("g-context-menu", true);
      
      let relatedHierarchies = this.svgHierarchyType.getRelatedHierarchies(this.getCode());
      
      let bbox = this.getBbox();
      let x = bbox.x + bbox.width - 4;
      let y = bbox.y + bbox.height/2 - 8;
      const height = 20;
      const fontSize = 9;
      const widthPadding = 10;
      const borderColor = "#006DBB";
      const fontFamily = "sans-serif";
      const titleFontSize = 12;
      const titleLabel = this.hierarchyComponent.localizeService.decode("hierarchy.content.relatedHierarchies");
      
      // Calculate the width of our title
      let width = calculateTextWidth(titleLabel, titleFontSize);
      
      // Calculate the width of our context menu, which is based on how long the text inside it will be.
      // We don't know how long text is until we render it. So we'll need to loop over all the text and
      // render and destroy all of it.
      relatedHierarchies.forEach((relatedHierarchyCode: string) => {
        let relatedHierarchy = this.hierarchyComponent.findHierarchyByCode(relatedHierarchyCode);
        
        let textWidth = calculateTextWidth(relatedHierarchy.label.localizedValue, fontSize);
        
        if (textWidth > width)
        {
          width = bbox.width;
        }
      });
      
      width = width + widthPadding;
      
      // Background rectangle with border
      contextMenuGroup.append("rect")
        .classed("contextmenu-relatedhiers-background", true)
        .attr("x", x)
        .attr("y", y)
        .attr("rx", 10)
        .attr("width", width)
        .attr("height", height * (relatedHierarchies.length + 1))
        .attr("fill", "#e0e0e0")
        .attr("stroke-width", 1)
        .attr("stroke", borderColor);
      
      // Related Hierarchies Title
      contextMenuGroup.append("text")
            .classed("contextmenu-relatedhiers-title", true)
            .attr("x", x + widthPadding / 2)
            .attr("y", y + (height/2) + (titleFontSize/2))
            .style("font-size", titleFontSize)
            .attr("font-family", fontFamily)
            .text(titleLabel);
        
      y = y + height;
      
      contextMenuGroup.append("line")
          .classed("contextmenu-relatedhiers-divider", true)
          .attr("x1", x)
          .attr("y1", y)
          .attr("x2", x + width)
          .attr("y2", y)
          .attr("stroke", borderColor)
          .attr("stroke-width", 1);
      
      // Loop over all related hierarchies and draw them as list items
      for (let i = 0; i < relatedHierarchies.length; ++i)
      {
        let relatedHierarchyCode = relatedHierarchies[i];
        let relatedHierarchy = this.hierarchyComponent.findHierarchyByCode(relatedHierarchyCode);
      
        // Text that says the hierarchy's display label
        contextMenuGroup.append("text")
            .classed("contextmenu-relatedhiers-text", true)
            .attr("data-hierCode", relatedHierarchyCode)
            .attr("x", x + widthPadding / 2)
            .attr("y", y + (height/2) + (fontSize/2))
            .style("font-size", fontSize)
            .attr("font-family", fontFamily)
            .text(relatedHierarchy.label.localizedValue)
            .style("cursor", "pointer")
            .on('click', function(event, node) {that.renderSecondaryHierarchy(relatedHierarchy);});
        
        y = y + height;
        
        // Dividing line at the bottom
        if (i < relatedHierarchies.length - 1)
        {
          contextMenuGroup.append("line")
              .classed("contextmenu-relatedhiers-divider", true)
              .attr("data-hierCode", relatedHierarchyCode)
              .attr("x1", x)
              .attr("y1", y)
              .attr("x2", x + width)
              .attr("y2", y)
              .attr("stroke", borderColor)
              .attr("stroke-width", 1);
        }
      };
      
      this.hierarchyComponent.calculateSvgViewBox();
    }
    else
    {
      existingMenu.remove();
    }
  }
  
  renderSecondaryHierarchy(relatedHierarchy: HierarchyType)
  {
    d3.select(".g-context-menu").remove();
    let that = this;
    
    // Remove any secondary hierarchy that may already be rendered
    let existingSecondary = d3.select('.g-hierarchy[data-primary="false"]');
    if (existingSecondary.node() != null)
    {
      existingSecondary.remove();
      this.hierarchyComponent.calculateSvgViewBox();
    }
    
    // Get the bounding box for our primary hierarchy
    let svg = d3.select("#svg");
    let primaryHierBbox = (d3.select(".g-hierarchy[data-primary=true]").node() as any).getBBox();
    
    // Render the secondary hierarchy
    let svgHt: SvgHierarchyType = new SvgHierarchyType(this.hierarchyComponent, svg, relatedHierarchy, false);
    svgHt.render();
    
    // Translate the secondary hierarchy to the right of the primary hierarchy
    let gHierarchy: any = d3.select('.g-hierarchy[data-primary="false"]').node();
    let bbox = gHierarchy.getBBox();
    let paddingLeft: number = primaryHierBbox.width + 40 + (primaryHierBbox.x - bbox.x);
    d3.select('.g-hierarchy[data-primary="false"]').attr("transform", "translate(" + paddingLeft + " 0)");
    
    d3.select(".hierarchy-inherit-button").remove();
    let relatedGotHasParents = svgHt.getNodeByCode(this.getCode()).getTreeNode().parent != null;
    if (relatedHierarchy.organizationCode === this.geoObjectType.organizationCode && this.treeNode.parent == null && relatedGotHasParents)
    {
      // Add an inherit button
      let myBbox = this.getBbox();
      
      const height = 15;
      const fontSize = 10;
      const buttonLabelPadding = 3;
      
      let group = d3.select('.g-hierarchy[data-primary=true] .g-hierarchy-tree[data-code="' + this.svgHierarchyType.getCode() + '"]').append("g").classed("hierarchy-inherit-button", true);
      
      let inheritLabel = this.hierarchyComponent.localizeService.decode("hierarchy.content.inherit");
      const width = calculateTextWidth(inheritLabel, fontSize) + buttonLabelPadding*2;
      
      group.append("rect")
        .classed("hierarchy-inherit-bg-rect", true)
        .attr("x", myBbox.x + myBbox.width - 25 - width)
        .attr("y", myBbox.y + myBbox.height / 2 - height/2)
        .attr("rx", 5)
        .attr("ry", 5)
        .attr("width", width)
        .attr("height", height)
        .attr("fill", "none")
        .attr("cursor", "pointer")
        .attr("stroke", "#6BA542")
        .attr("stroke-width", 1);
        
      group.append("text")
        .classed("hierarchy-inherit-bg-text", true)
        .attr("x", myBbox.x + myBbox.width - 25 - width + buttonLabelPadding)
        .attr("y", myBbox.y + myBbox.height / 2 + fontSize/2 - 2)
        .attr("fill", "#6BA542")
        .attr("cursor", "pointer")
        .attr("font-size",  fontSize + "px")
        .attr("line-height", fontSize + "px")
        .text(inheritLabel)
        .on('click', function(event, node) {that.onClickInheritHierarchy(relatedHierarchy);});
    }
    else if (relatedHierarchy.organizationCode === this.geoObjectType.organizationCode && (this.treeNode.parent != null && this.treeNode.parent.data.inherited))
    {
      // Add an uninherit button
      let myBbox = this.getBbox();
      
      const height = 15;
      const fontSize = 10;
      const buttonLabelPadding = 3;
      
      let group = d3.select('.g-hierarchy[data-primary=true] .g-hierarchy-tree[data-code="' + this.svgHierarchyType.getCode() + '"]').append("g").classed("hierarchy-uninherit-button", true);
      
      let inheritLabel = this.hierarchyComponent.localizeService.decode("hierarchy.content.uninherit");
      const width = calculateTextWidth(inheritLabel, fontSize) + buttonLabelPadding*2;
      
      group.append("rect")
        .classed("hierarchy-uninherit-bg-rect", true)
        .attr("x", myBbox.x + myBbox.width - 25 - width)
        .attr("y", myBbox.y + myBbox.height / 2 - height/2)
        .attr("rx", 5)
        .attr("ry", 5)
        .attr("width", width)
        .attr("height", height)
        .attr("fill", "none")
        .attr("cursor", "pointer")
        .attr("stroke", "#6BA542")
        .attr("stroke-width", 1);
        
      group.append("text")
        .classed("hierarchy-uninherit-bg-text", true)
        .attr("x", myBbox.x + myBbox.width - 25 - width + buttonLabelPadding)
        .attr("y", myBbox.y + myBbox.height / 2 + fontSize/2 - 2)
        .attr("fill", "#6BA542")
        .attr("cursor", "pointer")
        .attr("font-size",  fontSize + "px")
        .attr("line-height", fontSize + "px")
        .text(inheritLabel)
        .on('click', function(event, node) {that.onClickUninheritHierarchy(relatedHierarchy);});
    }
    
    // Recalculate the viewbox
    this.hierarchyComponent.calculateSvgViewBox();
  }
  
  onClickInheritHierarchy(hierarchy: HierarchyType)
  {
    this.hierarchyComponent.hierarchyService.setInheritedHierarchy(this.svgHierarchyType.getCode(), hierarchy.code, this.getCode()).then( (ht: HierarchyType) => {
        this.hierarchyComponent.refreshPrimaryHierarchy(ht);
    } ).catch(( err: HttpErrorResponse) => {
        this.hierarchyComponent.error( err );
    } );
  }
  
  onClickUninheritHierarchy(hierarchy: HierarchyType)
  {
    this.hierarchyComponent.hierarchyService.removeInheritedHierarchy(this.svgHierarchyType.getCode(), this.getCode()).then( (ht: HierarchyType) => {
        this.hierarchyComponent.refreshPrimaryHierarchy(ht);
    } ).catch(( err: HttpErrorResponse) => {
        this.hierarchyComponent.error( err );
    } );
  }
  
}

@Component({

  selector: 'hierarchies',
  templateUrl: './hierarchy.component.html',
  styleUrls: ['./hierarchy.css']
})
export class HierarchyComponent implements OnInit {

	private treeScaleFactorX = 1.6;
	private treeScaleFactorY = 1.6;
	
	private svgWidth: number = 200;
	private svgHeight: number = 500;
	
	primarySvgHierarchy: SvgHierarchyType;
	currentHierarchy: HierarchyType = null;
	
	instance: Instance = new Instance();
	hierarchies: HierarchyType[];
	organizations: Organization[];
	geoObjectTypes: GeoObjectType[] = [];
	
	hierarchiesByOrg: { org: Organization, hierarchies: HierarchyType[] }[] = [];
	typesByOrg: { org: Organization, types: GeoObjectType[] }[] = [];

	hierarchyTypeDeleteExclusions: string[] = ['AllowedIn', 'IsARelationship'];
	geoObjectTypeDeleteExclusions: string[] = ['ROOT'];

  _opened: boolean = false;

    /*
     * Reference to the modal current showing
    */
	private bsModalRef: BsModalRef;

    /*
     * Template for tree node menu
     */
	@ViewChild('nodeMenu') public nodeMenuComponent: ContextMenuComponent;

    /*
     * Template for leaf menu
     */
	@ViewChild('leafMenu') public leafMenuComponent: ContextMenuComponent;

    /* 
     * Currently clicked on id for delete confirmation modal 
     */
	current: any;
	
	private root: any;
	
	hierarchyService: HierarchyService;
	
	localizeService: LocalizationService;
	
	constructor(hierarchyService: HierarchyService, private modalService: BsModalService,
		private contextMenuService: ContextMenuService, private changeDetectorRef: ChangeDetectorRef,
		localizeService: LocalizationService, private registryService: RegistryService, private authService: AuthService) {

		// this.admin = authService.isAdmin();
		// this.isMaintainer = this.isAdmin || service.isMaintainer();
		// this.isContributor = this.isAdmin || this.isMaintainer || service.isContributer();
    this.hierarchyService = hierarchyService;
    this.localizeService = localizeService;
	}

	ngOnInit(): void {
		this.refreshAll(null);
	}
	
	private renderTree() {
	  if (this.currentHierarchy == null || this.currentHierarchy.rootGeoObjectTypes == null || this.currentHierarchy.rootGeoObjectTypes.length == 0)
	  {
	    d3.select("#svg").remove();
	    return;
	  }
	  
	  d3.select(".g-context-menu").remove();
	  d3.select(".hierarchy-inherit-button").remove();
	  
	  let overflowDiv: any = d3.select("#overflow-div").node();
	  let scrollLeft = overflowDiv.scrollLeft;
	  let scrollRight = overflowDiv.scrollRight;
	  
	  let that = this;
	  
    let svg = d3.select("#svg");
    
    if (svg.node() == null)
    {
      svg = d3.select("#svgHolder").append("svg");
      svg.attr("id", "svg");
    }
    
    this.primarySvgHierarchy = new SvgHierarchyType(this, svg, this.currentHierarchy, true);
    this.primarySvgHierarchy.render();
    
    this.calculateSvgViewBox();
    
    let overflowDiv2: any = d3.select("#overflow-div").node();
    overflowDiv2.scrollLeft = scrollLeft;
    overflowDiv2.scrollRight = scrollRight;
    
    this.registerSvgHandlers();
	}
	
	public calculateSvgViewBox()
	{
	  let svg: any = d3.select("#svg");
	  let svgNode: any = svg.node();
  
    let {x, y, width, height} = svgNode.getBBox();
  
    const xPadding = 30;
    const yPadding = 40;
    svg.attr("viewBox", (x - xPadding) + " " + (y - yPadding) + " " + (width + xPadding*2) + " " + (height + yPadding*2));
    
    width = (width + xPadding*2) * this.treeScaleFactorX;
    height = (height + yPadding*2) * this.treeScaleFactorY;
    
    d3.select("#svgHolder").style("width", width + "px");
    //d3.select("#svgHolder").style("height", height + "px"); 
	}
  
  private registerDragHandlers(): any {
    let that = this;
    
    let dropTargets: DropTarget[] = [];
    
    // Empty Hierarchy Drop Zone
    dropTargets.push({ dropSelector: ".drop-box-container", onDrag: function(dragEl: Element, dropEl: Element) {
      if (this.dropEl != null)
      {
        this.dropEl.style("border-color", null);
        this.dropEl = null;
      }
    
      if (dropEl != null)
      {
        let emptyHierarchyDropZone = dropEl.closest(".drop-box-container");
            
        if (emptyHierarchyDropZone != null)
        {
          this.dropEl = d3.select(emptyHierarchyDropZone).style("border-color", "blue");
        }
      }
    }, onDrop: function(dragEl: Element) {
      if (this.dropEl != null)
      {
        this.dropEl.style("border-color", null);
        that.addChild(that.currentHierarchy.code, "ROOT", d3.select(dragEl).attr("id"));
        this.dropEl = null;
      }
    }});
    
    // SVG GeoObjectType Drop Zone
    dropTargets.push({ dropSelector: ".svg-got-body-rect", onDrag: function(dragEl: Element, mouseTarget: Element, event: any) {
      this.clearDropZones();
      
      let lastDropEl = this.dropEl;
      
      // translate page to SVG co-ordinate
      let svg: any = d3.select("#svg").node();
      
      if (svg == null)
      {
        return;
      }
      
      let svgMousePoint = svgPoint(event.sourceEvent.pageX, event.sourceEvent.pageY);
      
      // Find out if we've dragged the GeoObjectType inside of a HierarchyNode. If we have, then
      // we need to expand the HierarchyNode's BoundingBox to accomodate our new drop zones. 
      that.primarySvgHierarchy.getD3Tree().descendants().forEach((node:any) => {
        if (node.data.geoObjectType !== "GhostNode" && isPointWithin(svgMousePoint, node.data.dropZoneBbox))
        {
          this.dropEl = d3.select(node.data.gotBodySquare);
          node.data.activeDropZones = true;
          
          if (node.parent == null)
          {
            node.data.dropZoneBbox = {x: node.x - SvgHierarchyType.gotRectW/2, y: node.y - SvgHierarchyType.gotRectH*2, width: SvgHierarchyType.gotRectW, height: SvgHierarchyType.gotRectH*4};
          }
        }
        else
        {
          node.data.activeDropZones = false;
          
          if (node.parent == null)
          {
            node.data.dropZoneBbox = {x: node.x - SvgHierarchyType.gotRectW/2, y: node.y - SvgHierarchyType.gotRectH/2, width: SvgHierarchyType.gotRectW, height: SvgHierarchyType.gotRectH};
          }
        }
      });
    
      if ( this.dropEl == null || (lastDropEl != null && this.dropEl != null && lastDropEl.attr("data-gotCode") != this.dropEl.attr("data-gotCode")) )
      {
        this.clearGhostNodes(true);
      }
      
      if (this.dropEl != null)
      {
        this.dropEl.attr("stroke", "blue");
      
        const dropElX = parseInt(this.dropEl.attr("x"));
        const dropElY = parseInt(this.dropEl.attr("y"));
        
        // Add drop zones
        const childW: number = SvgHierarchyType.gotRectW;
        const childH: number = SvgHierarchyType.gotRectH;
        
        let dzg = d3.select("#svg").append("g").classed("svg-dropZone-g", true);
        
        // Render Child Drop Zone
        this.childDzBacker = dzg.append("rect").classed("svg-got-child-dz-backer", true)
            .attr("x", dropElX + (SvgHierarchyType.gotRectW / 2) - (childW / 2))
            .attr("y", dropElY + SvgHierarchyType.gotRectH + 10)
            .attr("width", childW)
            .attr("height", childH)
            .attr("fill", "white")
        
        this.childDz = dzg.append("rect").classed("svg-got-child-dz", true)
            .attr("x", dropElX + (SvgHierarchyType.gotRectW / 2) - (childW / 2))
            .attr("y", dropElY + SvgHierarchyType.gotRectH + 10)
            .attr("width", childW)
            .attr("height", childH)
            .attr("fill", "none")
            .attr("stroke", "black")
            .attr("stroke-width", "1")
            .attr("stroke-dasharray", "5,5");
        
        this.childDzText = dzg.append("text").classed("svg-got-child-dz-text", true)
          .attr("font-family", "sans-serif")
          .attr("font-size", 10)
          .attr("fill", "black")
          .attr("x", dropElX + (SvgHierarchyType.gotRectW / 2) - 20)
          .attr("y", dropElY + SvgHierarchyType.gotRectH + 10 + childH/2 + 2)
          .text(this.localizeService.decode("hierarchy.content.addChild"));
        
        // Render Parent Drop Zone
        this.parentDzBacker = dzg.append("rect").classed("svg-got-parent-dz-backer", true)
            .attr("x", dropElX + (SvgHierarchyType.gotRectW / 2) - (childW / 2))
            .attr("y", dropElY - SvgHierarchyType.gotHeaderH - childH)
            .attr("width", childW)
            .attr("height", childH)
            .attr("fill", "white")
          
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
        this.parentDzText = dzg.append("text").classed("svg-got-parent-dz-text", true)
          .attr("font-family", "sans-serif")
          .attr("font-size", 10)
          .attr("fill", "black")
          .attr("x", dropElX + (SvgHierarchyType.gotRectW / 2) - 20)
          .attr("y", dropElY - SvgHierarchyType.gotHeaderH - childH/2 + 2)
          .text(this.localizeService.decode("hierarchy.content.addParent"));
          
        // Render Sibling Drop Zone
        let gotCode = this.dropEl.attr("data-gotCode");
        if (this.ghostCode != gotCode)
        {
          let dropNode = that.primarySvgHierarchy.getD3Tree().find((node)=>{return node.data.geoObjectType === gotCode;});
        
          if (this.ghostCode != null)
          {
            this.clearGhostNodes(dropNode.parent == null);
          }
        
          if (dropNode.parent != null)
          {
            let parentIndex = null;
            for (let i = 0; i < dropNode.parent.data.children.length; ++i)
            {
              let hn: any = dropNode.parent.data.children[i];
              
              if (hn.geoObjectType === gotCode)
              {
                parentIndex = i + 1;
              }
            };
          
            let addChildLabel = this.localizeService.decode("hierarchy.content.addChild");
            dropNode.parent.data.children.splice(parentIndex, 0, {ghostingCode: gotCode, geoObjectType:"GhostNode", label:addChildLabel, children:[]});
            
            that.renderTree();
            this.ghostCode = gotCode;
          }
        }
        
        let siblingGhostBody = d3.select(".svg-sibling-ghost-body-dz");
        
        if (isPointWithin(svgMousePoint, getBboxFromSelection(this.parentDz)))
        {
          this.parentDz.attr("stroke", "blue");
          this.parentDzText.attr("fill", "blue");
          this.childDz.attr("stroke", "black");
          this.childDzText.attr("fill", "black");
          siblingGhostBody.attr("stroke", "black");
          this.activeDz = this.parentDz;
        }
        else if (isPointWithin(svgMousePoint, getBboxFromSelection(this.childDz)))
        {
          this.parentDz.attr("stroke", "black");
          this.parentDzText.attr("fill", "black");
          this.childDz.attr("stroke", "blue");
          this.childDzText.attr("fill", "blue");
          siblingGhostBody.attr("stroke", "black");
          this.activeDz = this.childDz;
        }
        else if (siblingGhostBody.node() != null && isPointWithin(svgMousePoint, getBboxFromSelection(siblingGhostBody)))
        {
          this.parentDz.attr("stroke", "black");
          this.parentDzText.attr("fill", "black");
          this.childDz.attr("stroke", "black");
          this.childDzText.attr("fill", "black");
          siblingGhostBody.attr("stroke", "blue");
          this.activeDz = "sibling";
        }
      }
    }, onDrop: function(dragEl: Element) {
      if (this.dropEl != null && this.activeDz != null)
      {
        let dropGot = this.dropEl.attr("data-gotCode");
        let dropNode = that.primarySvgHierarchy.getD3Tree().find((node)=>{return node.data.geoObjectType === dropGot;});
        let dragGot = d3.select(dragEl).attr("id");
      
        if (this.activeDz === this.childDz)
        {
          if (dropNode.data.children.length == 0)
          {
            that.addChild(that.currentHierarchy.code, dropGot, dragGot);
          }
          else
          {
            let youngest = "";
          
            for (let i = 0; i < dropNode.data.children.length; ++i)
            {
              youngest = youngest + dropNode.data.children[i].geoObjectType;
            
              if (i < dropNode.data.children.length - 1)
              {
                youngest = youngest + ",";
              }
            }
          
            that.insertBetweenTypes(that.currentHierarchy.code, dropGot, dragGot, youngest);
          }
        }
        else if (this.activeDz === this.parentDz)
        {
          if (dropNode.parent == null)
          {
            that.insertBetweenTypes(that.currentHierarchy.code, "ROOT", dragGot, dropGot);
          }
          else
          {
            that.insertBetweenTypes(that.currentHierarchy.code, dropNode.parent.data.geoObjectType, dragGot, dropGot);
          }
        }
        else if (this.activeDz === "sibling")
        {
          that.addChild(that.currentHierarchy.code, dropNode.parent.data.geoObjectType, d3.select(dragEl).attr("id"));
        }
      }
      this.clearDropZones();
      this.clearGhostNodes(true);
    }, clearDropZones: function() {
      if (this.dropEl != null)
      {
        this.dropEl.attr("stroke", null);
      }
      
      this.dropEl = null;
      this.activeDz = null;
      
      this.childDz = null;
      this.parentDz = null;
      
      d3.select(".svg-dropZone-g").remove();
      
    }, clearGhostNodes: function(renderTree: boolean) {
      if (this.ghostCode != null)
      {
        let ghostNode = that.primarySvgHierarchy.getD3Tree().find((node)=>{return node.data.ghostingCode === this.ghostCode;});
        
        if (ghostNode != null)
        {
          let parentIndex = null;
          for (let i = 0; i < ghostNode.parent.data.children.length; ++i)
          {
            let hn: any = ghostNode.parent.data.children[i];
            
            if (hn.ghostingCode === this.ghostCode)
            {
              parentIndex = i;
            }
          };
          
          if (parentIndex != null)
          {
            ghostNode.parent.data.children.splice(parentIndex, 1);
            if (renderTree)
            {
              that.renderTree();
            }
          }
        }
        
        this.ghostCode = null;
      }
    }});
    
    // GeoObjectTypes and Hierarchies
    let deltaX: number, deltaY: number, width: number;
    let sidebarDragHandler = d3.drag()
    .on("start", function (event: any) {
        let rect = this.getBoundingClientRect();
        deltaX = rect.left - event.sourceEvent.pageX;
        deltaY = rect.top - event.sourceEvent.pageY;
        width = rect.width;
    })
    .on("drag", function (event: any) {
    
        d3.select(".g-context-menu").remove();
    
        // Kind of a dumb hack, but if we hide our drag element for a sec, then we can check what's underneath it.
        d3.select(this)
            .style("display", "none");
    
        let target = document.elementFromPoint(event.sourceEvent.pageX, event.sourceEvent.pageY);
        
        d3.select(this)
            .style("display", null);
        
        for (let i = 0; i < dropTargets.length; ++i)
        {
          dropTargets[i].onDrag(this, target, event);
        }
    
        // Move the GeoObjectType with the pointer when they move their mouse
        d3.select(this)
            .classed("dragging", true)
            .style("left", (event.sourceEvent.pageX + deltaX) + "px")
            .style("top", (event.sourceEvent.pageY + deltaY) + "px")
            .style("width", width + "px");
        
    }).on("end", function(event: any) {
        let selected = d3.select(this)
            .classed("dragging", false)
            .style("left", null)
            .style("top", null)
            .style("width", null);
        
        for (let i = 0; i < dropTargets.length; ++i)
        {
          dropTargets[i].onDrop(this, event);
        }
    });

    sidebarDragHandler(d3.selectAll(".sidebar-section-content ul.list-group li.got-li-item"));
  }
  
  private registerSvgHandlers(): void
  {
    let hierarchyComponent = this;
  
    // SVG Drag Handler
    let deltaX: number, deltaY: number, width: number;
    let startPoint: any;
    let svgGot: SvgHierarchyNode;
    let svgDragHandler = d3.drag()
    .on("start", function (event: any) {
      let svgMousePoint: any = svgPoint(event.sourceEvent.pageX, event.sourceEvent.pageY);
      let select = d3.select(this);
      
      svgGot = hierarchyComponent.primarySvgHierarchy.getNodeByCode(d3.select(this).attr("data-gotCode"));
      startPoint = svgGot.getPos();
    
      deltaX = startPoint.x - svgMousePoint.x;
      deltaY = startPoint.y - svgMousePoint.y;
    })
    .on("drag", function (event: any) {
    
      d3.select(".g-context-menu").remove();
    
      let svgMousePoint = svgPoint(event.sourceEvent.pageX, event.sourceEvent.pageY);
  
      svgGot = hierarchyComponent.primarySvgHierarchy.getNodeByCode(d3.select(this).attr("data-gotCode"));
      
      svgGot.setPos(svgMousePoint.x + deltaX, svgMousePoint.y + deltaY, true);
        
    }).on("end", function(event: any) {
    
      let bbox: string[] = d3.select("#svg").attr("viewBox").split(" ");
    
      if (!isBboxPartiallyWithin(svgGot.getBbox(), {x: parseInt(bbox[0]), y: parseInt(bbox[1]), width: parseInt(bbox[2]), height: parseInt(bbox[3])}))
      {
        let obj = hierarchyComponent.findGeoObjectTypeByCode(svgGot.getCode());

        hierarchyComponent.bsModalRef = hierarchyComponent.modalService.show(ConfirmModalComponent, {
          animated: true,
          backdrop: true,
          ignoreBackdropClick: true,
        });
        hierarchyComponent.bsModalRef.content.message = hierarchyComponent.localizeService.decode("confirm.modal.verify.delete") + ' [' + obj.label.localizedValue + ']';
        hierarchyComponent.bsModalRef.content.data = obj.code;
    
        (<ConfirmModalComponent>hierarchyComponent.bsModalRef.content).onConfirm.subscribe(data => {
          let treeNode = svgGot.getTreeNode();
          let parent = treeNode.parent == null ? "ROOT" : treeNode.parent.data.geoObjectType;
        
          hierarchyComponent.removeTreeNode(parent, svgGot.getCode(), (err: any) => {svgGot.setPos(startPoint.x, startPoint.y, false);});
        });
        
        (<ConfirmModalComponent>hierarchyComponent.bsModalRef.content).onCancel.subscribe(data => {
          svgGot.setPos(startPoint.x, startPoint.y, false);
        });
      }
      else
      {
        svgGot.setPos(startPoint.x, startPoint.y, false);
      }
      
    });

    svgDragHandler(d3.selectAll(".svg-got-body-rect,.svg-got-label-text,.svg-got-header-rect"));
  }
  
  public findGeoObjectTypeByCode(code: string): GeoObjectType
  {
    for (let i = 0; i < this.geoObjectTypes.length; ++i)
    {
      let got: GeoObjectType = this.geoObjectTypes[i];
      
      if (got.code === code)
      {
        return got;
      }
    }
  }
  
  public findHierarchyByCode(code: string): HierarchyType
  {
    for (let i = 0; i < this.hierarchies.length; ++i)
    {
      let ht: HierarchyType = this.hierarchies[i];
      
      if (ht.code === code)
      {
        return ht;
      }
    }
  }
  
  private addChild(hierarchyCode: string, parentGeoObjectTypeCode: string, childGeoObjectTypeCode: string): void
  {
    this.hierarchyService.addChildToHierarchy(hierarchyCode, parentGeoObjectTypeCode, childGeoObjectTypeCode ).then( (ht: HierarchyType) => {
        this.refreshPrimaryHierarchy(ht);
    } ).catch(( err: HttpErrorResponse) => {
        this.error( err );
    } );
  }
  
  private insertBetweenTypes(hierarchyCode: string, parentGeoObjectTypeCode: string, middleGeoObjectTypeCode: string, youngestGeoObjectTypeCode: string): void
  {
    this.hierarchyService.insertBetweenTypes(hierarchyCode, parentGeoObjectTypeCode, middleGeoObjectTypeCode, youngestGeoObjectTypeCode ).then( (ht: HierarchyType) => {
        this.refreshPrimaryHierarchy(ht);
    } ).catch(( err: HttpErrorResponse) => {
        this.error( err );
    } );
  }

	ngAfterViewInit() {

	}

	isRA(): boolean {
		return this.authService.isRA();
	}

	isOrganizationRA(orgCode: string, dropZone: boolean = false): boolean {
		return this.authService.isOrganizationRA(orgCode);
	}
	
	getTypesByOrg(org: Organization): GeoObjectType[]
  {
    let orgTypes: GeoObjectType[] = [];
    
    for (let i = 0; i < this.geoObjectTypes.length; ++i)
    {
      let geoObjectType: GeoObjectType = this.geoObjectTypes[i];
      
      if (geoObjectType.organizationCode === org.code)
      {
        orgTypes.push(geoObjectType);
      }
    }
    
    return orgTypes;
  }
	
	getHierarchiesByOrg(org: Organization): HierarchyType[]
	{
	  let orgHierarchies: HierarchyType[] = [];
	  
	  for (let i = 0; i < this.hierarchies.length; ++i)
	  {
	    let hierarchy: HierarchyType = this.hierarchies[i];
	    
	    if (hierarchy.organizationCode === org.code)
	    {
	      orgHierarchies.push(hierarchy);
	    }
	  }
	  
	  return orgHierarchies;
	}

	public refreshAll(desiredHierarchy: HierarchyType) {
		this.registryService.init().then(response => {
			this.localizeService.setLocales(response.locales);

			this.geoObjectTypes = response.types;
			
			this.organizations = response.organizations;
			
			this.geoObjectTypes.sort((a, b) => {
				if (a.label.localizedValue.toLowerCase() < b.label.localizedValue.toLowerCase()) return -1;
				else if (a.label.localizedValue.toLowerCase() > b.label.localizedValue.toLowerCase()) return 1;
				else return 0;
			});

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
	
	public updateViewDatastructures(): void
	{
	  this.hierarchiesByOrg = [];
	  this.typesByOrg = [];
	
	  for (let i = 0; i < this.organizations.length; ++i)
    {
      let org: Organization = this.organizations[i];
    
      this.hierarchiesByOrg.push({org: org, hierarchies: this.getHierarchiesByOrg(org)});
      this.typesByOrg.push({org: org, types: this.getTypesByOrg(org)});
    }
    
    setTimeout( () => { this.registerDragHandlers(); }, 500 );
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
		}
		else if (this.hierarchies.length > 0) {
			index = 0;
		}

		if (index > -1) {
			let hierarchy = this.hierarchies[index];

			this.currentHierarchy = hierarchy;
			
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
				})
			}

			hierarchies.push(hierarchyType);

		});

		this.hierarchies = hierarchies

		this.hierarchies.sort((a, b) => {
			if (a.label.localizedValue.toLowerCase() < b.label.localizedValue.toLowerCase()) return -1;
			else if (a.label.localizedValue.toLowerCase() > b.label.localizedValue.toLowerCase()) return 1;
			else return 0;
		});
	}

	private processHierarchyNodes(node: HierarchyNode) {
	  if (node != null)
	  {
  		node.label = this.getHierarchyLabel(node.geoObjectType);
  
  		node.children.forEach(child => {
  			this.processHierarchyNodes(child);
  		})
		}
	}

	private getHierarchyLabel(geoObjectTypeCode: string): string {
		let label: string = null;
		this.geoObjectTypes.forEach(function(gOT) {
			if (gOT.code === geoObjectTypeCode) {
				label = gOT.label.localizedValue;
			}
		});

		return label;
	}

	public handleOnMenu(node: any, $event: any): void {
		if (this.isOrganizationRA(this.currentHierarchy.organizationCode)) {
			this.contextMenuService.show.next({
				contextMenu: (node.data.childType !== null ? this.nodeMenuComponent : this.leafMenuComponent),
				event: $event,
				item: node,
			});
			$event.preventDefault();
			$event.stopPropagation();
		}
		else {
			$event.preventDefault();
			$event.stopPropagation();
		}
	}

	public treeNodeOnClick(node: any, $event: any): void {

		node.treeModel.setFocusedNode(node);

		if (node.treeModel.isExpanded(node)) {
			node.collapse();
		}
		else {
			node.treeModel.expandAll();
		}
	}

	options = {
		//		  allowDrag: (any) => node.isLeaf,
		//		  allowDrop: (element:Element, { parent, index }: {parent:TreeNode,index:number}) => {
		// return true / false based on element, to.parent, to.index. e.g.
		//			    return parent.hasChildren;
		//			  },
		displayField: "label",
		actionMapping: {
			mouse: {
				click: (tree: any, node: any, $event: any) => {
					this.treeNodeOnClick(node, $event);
				},
				contextMenu: (tree: any, node: any, $event: any) => {
					this.handleOnMenu(node, $event);
				}
			}
		},
		mouse: {
			//	            drop: (tree: any, node: TreeNode, $event: any, {from, to}: {from:TreeNode, to:TreeNode}) => {
			//	              console.log('drag', from, to); // from === {name: 'first'}
			//	              // Add a node to `to.parent` at `to.index` based on the data in `from`
			//	              // Then call tree.update()
			//	            }
		}
	};

	public hierarchyOnClick(event: any, item: any) {
		let hierarchyId = item.code;

		this.currentHierarchy = item;
		
		this.renderTree();
	}

	public createHierarchy(): void {
		this.bsModalRef = this.modalService.show(CreateHierarchyTypeModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
			'class': 'upload-modal'
		});

		(<CreateHierarchyTypeModalComponent>this.bsModalRef.content).onHierarchytTypeCreate.subscribe(data => {

			this.hierarchies.push(data);
			
			this.hierarchies.sort( (a: HierarchyType,b: HierarchyType) => {
        var nameA = a.label.localizedValue.toUpperCase(); // ignore upper and lowercase
        var nameB = b.label.localizedValue.toUpperCase(); // ignore upper and lowercase
        
        if (nameA < nameB) {
          return -1; //nameA comes first
        }
        
        if (nameA > nameB) {
          return 1; // nameB comes first
        }
        
        return 0;  // names must be equal
      });
			
			this.updateViewDatastructures();
			
		});
	}

	public deleteHierarchyType(obj: HierarchyType): void {
		this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
		});
		this.bsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + ' [' + obj.label.localizedValue + ']';
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
			'class': 'upload-modal'
		});
		this.bsModalRef.content.edit = true;
		this.bsModalRef.content.readOnly = readOnly;
		this.bsModalRef.content.hierarchyType = obj;
		this.bsModalRef.content.onHierarchytTypeCreate.subscribe(data => {
			let pos = this.getHierarchyTypePosition(data.code);

			this.hierarchies[pos].label = data.label;
			this.hierarchies[pos].description = data.description;
		});
	}

	public removeHierarchyType(code: string): void {
		this.hierarchyService.deleteHierarchyType(code).then(response => {

			let pos = this.getHierarchyTypePosition(code);
			this.hierarchies.splice(pos, 1);
			this.updateViewDatastructures();

		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	public createGeoObjectType(): void {
		this.bsModalRef = this.modalService.show(CreateGeoObjTypeModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
			'class': 'upload-modal'
		});
		this.bsModalRef.content.hierarchyType = this.currentHierarchy;

		(<CreateGeoObjTypeModalComponent>this.bsModalRef.content).onGeoObjTypeCreate.subscribe(data => {
		
			this.geoObjectTypes.push(data);
			
			this.geoObjectTypes.sort( (a: GeoObjectType,b: GeoObjectType) => {
			  var nameA = a.label.localizedValue.toUpperCase(); // ignore upper and lowercase
        var nameB = b.label.localizedValue.toUpperCase(); // ignore upper and lowercase
        
        if (nameA < nameB) {
          return -1; //nameA comes first
        }
        
        if (nameA > nameB) {
          return 1; // nameB comes first
        }
        
        return 0;  // names must be equal
			});
			
			this.updateViewDatastructures();
			
		});
	}

	public deleteGeoObjectType(obj: GeoObjectType): void {
		this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
		});
		this.bsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + ' [' + obj.label.localizedValue + ']';
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
		  if (errCallback != null)
		  {
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
			'class': 'manage-geoobjecttype-modal'
		});

		geoObjectType.attributes.sort((a, b) => {
			if (a.label.localizedValue < b.label.localizedValue) return -1;
			else if (a.label.localizedValue > b.label.localizedValue) return 1;
			else return 0;
		});
		this.bsModalRef.content.geoObjectType = geoObjectType;
		this.bsModalRef.content.readOnly = readOnly;

		(<ManageGeoObjectTypeModalComponent>this.bsModalRef.content).onGeoObjectTypeSubmitted.subscribe(data => {

			let position = this.getGeoObjectTypePosition(data.code);
			if (position) {
				this.geoObjectTypes[position] = data;
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
	
	public refreshPrimaryHierarchy(hierarchyType: HierarchyType)
	{
	  this.processHierarchyNodes(hierarchyType.rootGeoObjectTypes[0]);
	  
    for (let i = 0; i < this.hierarchies.length; ++i)
    {
      let hierarchy = this.hierarchies[i];
      
      if (hierarchy.code === hierarchyType.code) {
        this.hierarchies[i] = hierarchyType;
        this.currentHierarchy = hierarchyType;
      }
    }
    
    this.updateViewDatastructures();
    
    this.renderTree();
	}

	public removeTreeNode(parentGotCode, gotCode, errCallback: (err: HttpErrorResponse) => void = null): void {
	  const that = this;
	
		this.hierarchyService.removeFromHierarchy(this.currentHierarchy.code, parentGotCode, gotCode).then(hierarchyType => {

      that.refreshPrimaryHierarchy(hierarchyType);

		}).catch((err: HttpErrorResponse) => {
		  if (errCallback != null)
		  {
		    errCallback(err);
		  }
		
			this.error(err);
		});
	}

	public isActive(item: any) {
		return this.currentHierarchy === item;
	};

// Older drag/drop logic. May not be relevant anymore since d3 refactor.
/*	public onDrop($event: any) {
		// Dropped $event.element
		this.removeTreeNode($event.element)
	}

	public allowDrop(element: Element) {
		// Return true/false based on element
		return true;
	}*/

	public error(err: HttpErrorResponse): void {
		this.bsModalRef = this.modalService.show(ErrorModalComponent, { backdrop: true });
		this.bsModalRef.content.message = ErrorHandler.getMessageFromError(err);
	}

}
