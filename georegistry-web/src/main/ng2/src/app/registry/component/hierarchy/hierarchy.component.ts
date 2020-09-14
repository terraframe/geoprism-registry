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

export class SvgHierarchyType {

  public static gotRectW: number = 150;
  public static gotRectH: number = 25;
  
  public static gotHeaderW: number = 150;
  public static gotHeaderH: number = 12;

  public render(svg: any, root: any) {
    let descends:any = root.descendants();
    
    // Edge
    d3.select(".g-got-edge").remove();
    svg.append("g").classed("g-got-edge", true)
      .attr("fill", "none")
      .attr("stroke", "#555")
      .attr("stroke-opacity", 0.4)
      .attr("stroke-width", 1.5)
    .selectAll("path")
      .data(root.links())
      .join("path")
        //.attr("d", d3.linkVertical().x(function(d:any) { return d.x; }).y(function(d:any) { return d.y; })); // draws edges as curved lines
        .attr("d", (d:any,i) => { // draws edges as square bracket lines
          return "M" + d.source.x + "," + (d.source.y)
                 + "V" + ((d.source.y + d.target.y)/2)
                 + "H" + d.target.x
                 + "V" + (d.target.y);
        });
  
    // Header on square which denotes which hierarchy it's a part of
    d3.select(".g-got-header").remove();
    svg.append("g").classed("g-got-header", true)
        .selectAll("rect")
        .data(descends)
        .join("rect")
        .filter(function(d:any){return d.data.geoObjectType !== "GhostNode";})
          .attr("x", (d: any) => d.x - (SvgHierarchyType.gotRectW / 2))
          .attr("y", (d: any) => d.y - SvgHierarchyType.gotRectH + 8)
          .attr("fill", (d: any) => "#b3ad00")
          .attr("width", SvgHierarchyType.gotHeaderW)
          .attr("height", SvgHierarchyType.gotHeaderH)
          .attr("rx", 5)
          
    // GeoObjectType Body Square
    d3.select(".g-got").remove();
    svg.append("g").classed("g-got", true)
        .selectAll("rect")
        .data(descends)
        .join("rect")
        .filter(function(d:any){return d.data.geoObjectType !== "GhostNode";})
          .classed("svg-got-dz", true)
          .attr("x", (d: any) => d.x - (SvgHierarchyType.gotRectW / 2))
          .attr("y", (d: any) => d.y - (SvgHierarchyType.gotRectH / 2))
          .attr("fill", "#e0e0e0")
          .attr("width", SvgHierarchyType.gotRectW)
          .attr("height", SvgHierarchyType.gotRectH)
          .attr("rx", 5)
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
    d3.select(".g-sibling-ghost-backer").remove();
    svg.append("g").classed("g-sibling-ghost-backer", true)
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
    d3.select(".g-sibling-ghost-body").remove();
    svg.append("g").classed("g-sibling-ghost-body", true)
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
    d3.select(".g-got-codelabel").remove();
    svg.append("g").classed("g-got-codelabel", true)
        .attr("font-family", "sans-serif")
        .attr("font-size", 10)
        .attr("stroke-linejoin", "round")
        .attr("stroke-width", 3)
      .selectAll("text")
      .data(descends)
      .join("text")
        .attr("x", (d:any) => d.x - 70)
        .attr("y", (d:any) => d.y - 4)
        .attr("dx", "0.31em")
        .attr("dy", (d:any) => 6)
        .text((d:any) => d.data.label)
  }
}

export class SvgGeoObjectType {
  
  private code: string;
  
  constructor(code: string)
  {
    this.code = code;
  }
  
  setX(newX: number)
  {
    
  }
  
  setY(newY: number)
  {
    
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
	
	instance: Instance = new Instance();
	hierarchies: HierarchyType[];
	organizations: Organization[];
	geoObjectTypes: GeoObjectType[] = [];
	nodes = [] as HierarchyNode[];
	currentHierarchy: HierarchyType = null;
	
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
	
	constructor(private hierarchyService: HierarchyService, private modalService: BsModalService,
		private contextMenuService: ContextMenuService, private changeDetectorRef: ChangeDetectorRef,
		private localizeService: LocalizationService, private registryService: RegistryService, private authService: AuthService) {

		// this.admin = authService.isAdmin();
		// this.isMaintainer = this.isAdmin || service.isMaintainer();
		// this.isContributor = this.isAdmin || this.isMaintainer || service.isContributer();

	}

	ngOnInit(): void {
		this.refreshAll(null);
	}
	
	private renderTree() {
	  if (this.nodes == null || this.nodes.length === 0 || this.nodes[0] == null)
	  {
	    d3.select("#svg").remove();
	    return;
	  }
	  
	  console.log("re-rendering entire tree");
	  
	  let overflowDiv: any = d3.select("#overflow-div").node();
	  let scrollLeft = overflowDiv.scrollLeft;
	  let scrollRight = overflowDiv.scrollRight;
	  
	  let that = this;
	  let data = this.nodes[0];
	  
	  this.myTree(data);

    //const svg = d3.create("svg");
    let svg = d3.select("#svg");
    
    if (svg.node() == null)
    {
      svg = d3.create("svg");
    }
    
    new SvgHierarchyType().render(svg, this.root);
    
    let svgNode: any = svg.node();
  
    // Calculate the svg viewport size
    document.body.appendChild(svgNode);
    let {x, y, width, height} = svgNode.getBBox();
    document.body.removeChild(svgNode);
  
    svg.attr("id", "svg");
    
    const xPadding = 30;
    const yPadding = 40;
    svg.attr("viewBox", (x - xPadding) + " " + (y - yPadding) + " " + (width + xPadding*2) + " " + (height + yPadding*2));
    
    if (svgNode.parentNode == null)
    {
      document.getElementById("svgHolder").appendChild(svgNode);
    }
    
    width = (width + xPadding*2) * this.treeScaleFactorX;
    height = (height + yPadding*2) * this.treeScaleFactorY;
    
    // This sort of clipping screws up the dropzone logic we have for relationship management
    //if (width > 1200)
    //{
      //width = 1200;
    //}
    
    d3.select("#svgHolder").style("width", width + "px");
    //d3.select("#svgHolder").style("height", height + "px"); 
    
    let overflowDiv2: any = d3.select("#overflow-div").node();
    overflowDiv2.scrollLeft = scrollLeft;
    overflowDiv2.scrollRight = scrollRight;
	}
  
  private myTree(data): any {
    //const root2: any = d3.hierarchy(data).sort((a, b) => d3.descending(a.height, b.height) || d3.ascending(a.data.name, b.data.name));
    const root2: any = d3.hierarchy(data);
    this.root = d3.tree().nodeSize([300, 85]).separation((a, b) => 0.8)(root2);
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
    dropTargets.push({ dropSelector: ".svg-got-dz", onDrag: function(dragEl: Element, mouseTarget: Element, event: any) {
      this.clearDropZones();
      
      let lastDropEl = this.dropEl;
      
      // translate page to SVG co-ordinate
      let svg: any = d3.select("#svg").node();
      
      if (svg == null)
      {
        return;
      }
      
      let svgMousePoint = svgPoint(event.sourceEvent.pageX, event.sourceEvent.pageY);
      
      that.root.descendants().forEach((node:any) => {
        if (node.data.geoObjectType !== "GhostNode" && this.isWithin(svgMousePoint, node.data.dropZoneBbox))
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
          .text("Add Child"); // TODO Localize
        
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
          .text("Add Parent"); // TODO Localize
          
        // Render Sibling Drop Zone
        let gotCode = this.dropEl.attr("data-gotCode");
        if (this.ghostCode != gotCode)
        {
          let dropNode = that.root.find((node)=>{return node.data.geoObjectType === gotCode;});
        
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
          
            dropNode.parent.data.children.splice(parentIndex, 0, {ghostingCode: gotCode, geoObjectType:"GhostNode", label:"Add Child", children:[]}); // TODO : localize
            
            that.renderTree();
            this.ghostCode = gotCode;
          }
        }
        
        let siblingGhostBody = d3.select(".svg-sibling-ghost-body-dz");
        
        if (this.isWithin(svgMousePoint, this.getBboxFromSelection(this.parentDz)))
        {
          this.parentDz.attr("stroke", "blue");
          this.parentDzText.attr("fill", "blue");
          this.childDz.attr("stroke", "black");
          this.childDzText.attr("fill", "black");
          siblingGhostBody.attr("stroke", "black");
          this.activeDz = this.parentDz;
        }
        else if (this.isWithin(svgMousePoint, this.getBboxFromSelection(this.childDz)))
        {
          this.parentDz.attr("stroke", "black");
          this.parentDzText.attr("fill", "black");
          this.childDz.attr("stroke", "blue");
          this.childDzText.attr("fill", "blue");
          siblingGhostBody.attr("stroke", "black");
          this.activeDz = this.childDz;
        }
        else if (siblingGhostBody.node() != null && this.isWithin(svgMousePoint, this.getBboxFromSelection(siblingGhostBody)))
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
        let dropNode = that.root.find((node)=>{return node.data.geoObjectType === dropGot;});
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
        let ghostNode = that.root.find((node)=>{return node.data.ghostingCode === this.ghostCode;});
        
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
    }, isWithin : function(point: {x: number, y: number}, bbox: {x: number, y: number, width: number, height: number}) {
      return point.y > bbox.y && point.y < (bbox.y + bbox.height) && point.x > bbox.x && point.x < (bbox.x + bbox.width);
    }, getBboxFromSelection : function(selection: any) {
      return {x: parseInt(selection.attr("x")), y: parseInt(selection.attr("y")), width: parseInt(selection.attr("width")), height: parseInt(selection.attr("height"))};
    }});
    
    // GeoObjectTypes and Hierarchies
    let deltaX, deltaY, width: number;
    let sidebarDragHandler = d3.drag()
    .on("start", function (event: any) {
        let rect = this.getBoundingClientRect();
        deltaX = rect.left - event.sourceEvent.pageX;
        deltaY = rect.top - event.sourceEvent.pageY;
        width = rect.width;
    })
    .on("drag", function (event: any) {
    
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
    
    // SVG Drag Handler
    let startX: number, startY: number;
    let svgDragHandler = d3.drag()
    .on("start", function (event: any) {
      startX = parseInt(d3.select(this).attr("x"));
      startY = parseInt(d3.select(this).attr("y"));
    })
    .on("drag", function (event: any) {
    
        let svgMousePoint = svgPoint(event.sourceEvent.pageX, event.sourceEvent.pageY);
    
        // Move the GeoObjectType with the pointer when they move their mouse
        d3.select(this)
            .classed("dragging", true)
            .style("x", svgMousePoint.x + "px")
            .style("y", svgMousePoint.y + "px");
        
    }).on("end", function(event: any) {
        let selected = d3.select(this)
            .classed("dragging", false)
            .style("x", startX)
            .style("y", startY);
    });

    svgDragHandler(d3.selectAll(".svg-got-dz"));
  }
  
  private findGeoObjectTypeByCode(code: string): GeoObjectType
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
  
  private findHierarchyByCode(code: string): HierarchyType
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
        this.processHierarchyNodes(ht.rootGeoObjectTypes[0]);
        this.updateHierarchy(ht.code, ht.rootGeoObjectTypes)
    
        this.setNodesForHierarchy(ht);
    } ).catch(( err: HttpErrorResponse) => {
        this.error( err );
    } );
  }
  
  private insertBetweenTypes(hierarchyCode: string, parentGeoObjectTypeCode: string, middleGeoObjectTypeCode: string, youngestGeoObjectTypeCode: string): void
  {
    this.hierarchyService.insertBetweenTypes(hierarchyCode, parentGeoObjectTypeCode, middleGeoObjectTypeCode, youngestGeoObjectTypeCode ).then( (ht: HierarchyType) => {
        this.processHierarchyNodes(ht.rootGeoObjectTypes[0]);
        this.updateHierarchy(ht.code, ht.rootGeoObjectTypes)
    
        this.setNodesForHierarchy(ht);
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

	public refreshAll(desiredHierarchy) {
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

	private setNodesOnInit(desiredHierarchy): void {

		let index = -1;

		if (desiredHierarchy != null) {
			index = this.hierarchies.findIndex(h => h.code === desiredHierarchy.code);
		}
		else if (this.hierarchies.length > 0) {
			index = 0;
		}

		if (index > -1) {
			let hierarchy = this.hierarchies[index];

			this.nodes = hierarchy.rootGeoObjectTypes;

			this.currentHierarchy = hierarchy;

			//setTimeout(() => {
			//	if (this.tree) {
			//		this.tree.treeModel.expandAll();
			//	}
			//}, 1)
			
			this.renderTree();
		}
	}

	private setNodesForHierarchy(hierarchyType: HierarchyType): void {
		for (let i = 0; i < this.hierarchies.length; i++) {
			let hierarchy = this.hierarchies[i];
			if (hierarchy.code === hierarchyType.code) {
				this.nodes = hierarchyType.rootGeoObjectTypes;
				this.currentHierarchy = hierarchy;
				break;
			}
		}

		//setTimeout(() => {
		//	this.tree.treeModel.expandAll();
		//}, 1)
		
		this.renderTree();
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

	private updateHierarchy(code: string, rootGeoObjectTypes: HierarchyNode[]): void {
		this.hierarchies.forEach(hierarchy => {
			if (hierarchy.code === code) {
				hierarchy.rootGeoObjectTypes = rootGeoObjectTypes;
			}
		})
	}

    /**
     * Set properties required by angular-tree-component using recursion.
     */
	private processHierarchyNodes(node: HierarchyNode) {
		node.label = this.getHierarchyLabel(node.geoObjectType);

		node.children.forEach(child => {
			this.processHierarchyNodes(child);
		})
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

		this.nodes = [];

		if (this.getHierarchy(hierarchyId).rootGeoObjectTypes.length > 0) {
			// TODO: should rootGeoObjectTypes be hardcoded to only one entry in the array?
			this.nodes.push(this.getHierarchy(hierarchyId).rootGeoObjectTypes[0]);

			//setTimeout(() => {
			//	if (this && this.tree) {
			//		this.tree.treeModel.expandAll();
			//	}
			//}, 1)
		}

		//if (this.tree) {
		//	this.tree.treeModel.update();
		//}
		
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

	public removeGeoObjectType(code: string): void {
		this.registryService.deleteGeoObjectType(code).then(response => {

			let pos = this.getGeoObjectTypePosition(code);
			this.geoObjectTypes.splice(pos, 1);

			//          const parent = node.parent;
			//          let children = parent.data.children;
			//
			//          parent.data.children = children.filter(( n: any ) => n.id !== node.data.id );
			//
			//          if ( parent.data.children.length === 0 ) {
			//              parent.data.hasChildren = false;
			//          }
			//
			//        this.tree.treeModel.update();
			//this.setNodesOnInit();

			this.refreshAll(this.currentHierarchy);

		}).catch((err: HttpErrorResponse) => {
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

// TODO : This code is deprecated after the d3 hierarchy redesign
	/*public addChildAndRootToHierarchy(): void {
		const that = this;

		this.bsModalRef = this.modalService.show(AddChildToHierarchyModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
			'class': 'upload-modal'
		});
		this.bsModalRef.content.allGeoObjectTypes = this.geoObjectTypes;
		this.bsModalRef.content.parent = "ROOT";
		this.bsModalRef.content.toRoot = true;
		this.bsModalRef.content.hierarchyType = this.currentHierarchy;
		this.bsModalRef.content.nodes = this.nodes;

		(<AddChildToHierarchyModalComponent>this.bsModalRef.content).onNodeChange.subscribe(hierarchyType => {

			that.processHierarchyNodes(hierarchyType.rootGeoObjectTypes[0]);
			that.updateHierarchy(hierarchyType.code, hierarchyType.rootGeoObjectTypes)

			that.setNodesForHierarchy(hierarchyType);

			//if (this.tree) {
			//	this.tree.treeModel.update();
			//}
		});
	}*/

	public addChildToHierarchy(parent: any): void {
		const that = this;
		that.current = parent;

		this.bsModalRef = this.modalService.show(AddChildToHierarchyModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
			'class': 'upload-modal'
		});
		this.bsModalRef.content.allGeoObjectTypes = this.geoObjectTypes;
		this.bsModalRef.content.parent = parent;
		this.bsModalRef.content.toRoot = false;
		this.bsModalRef.content.hierarchyType = this.currentHierarchy;
		this.bsModalRef.content.nodes = this.nodes;

		(<AddChildToHierarchyModalComponent>this.bsModalRef.content).onNodeChange.subscribe(hierarchyType => {
			const d = that.current.data;


			that.processHierarchyNodes(hierarchyType.rootGeoObjectTypes[0]);
			that.updateHierarchy(hierarchyType.code, hierarchyType.rootGeoObjectTypes)

			that.setNodesForHierarchy(hierarchyType);

			//if (this.tree) {
			//	this.tree.treeModel.update();
			//}
		});
	}

	public deleteTreeNode(node: any): void {
		this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
		});
		this.bsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + ' [' + node.data.label + ']';
		this.bsModalRef.content.data = node;

		(<ConfirmModalComponent>this.bsModalRef.content).onConfirm.subscribe(data => {
			this.removeTreeNode(data);
		});
	}

	public removeTreeNode(node: any): void {
		this.hierarchyService.removeFromHierarchy(this.currentHierarchy.code, node.parent.data.geoObjectType, node.data.geoObjectType).then(data => {

			if (node.parent.data.geoObjectType == null) {
				this.nodes = [];
				// this.refreshAll(null);
				//return;
			}

			const parent = node.parent;
			let children = parent.data.children;

			// Update the tree
			parent.data.children = children.filter((n: any) => n.id !== node.data.id);
			if (parent.data.children.length === 0) {
				parent.data.hasChildren = false;
			}
			//this.tree.treeModel.update();

			// Update the available GeoObjectTypes
			this.changeDetectorRef.detectChanges()

		}).catch((err: HttpErrorResponse) => {
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
