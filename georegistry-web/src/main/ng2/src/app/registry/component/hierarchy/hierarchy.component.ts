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

@Component({

	selector: 'hierarchies',
	templateUrl: './hierarchy.component.html',
	styleUrls: ['./hierarchy.css']
})

export class HierarchyComponent implements OnInit {

	// isAdmin: boolean;
	// isMaintainer: boolean;
	// isContributor: boolean;
	
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
	  //let data = '{"name":"flare","children":[{"name":"analytics","children":[{"name":"cluster","children":[{"name":"AgglomerativeCluster","value":3938},{"name":"CommunityStructure","value":3812},{"name":"HierarchicalCluster","value":6714},{"name":"MergeEdge","value":743}]},{"name":"graph","children":[{"name":"BetweennessCentrality","value":3534},{"name":"LinkDistance","value":5731},{"name":"MaxFlowMinCut","value":7840},{"name":"ShortestPaths","value":5914},{"name":"SpanningTree","value":3416}]},{"name":"optimization","children":[{"name":"AspectRatioBanker","value":7074}]}]},{"name":"animate","children":[{"name":"Easing","value":17010},{"name":"FunctionSequence","value":5842},{"name":"interpolate","children":[{"name":"ArrayInterpolator","value":1983},{"name":"ColorInterpolator","value":2047},{"name":"DateInterpolator","value":1375},{"name":"Interpolator","value":8746},{"name":"MatrixInterpolator","value":2202},{"name":"NumberInterpolator","value":1382},{"name":"ObjectInterpolator","value":1629},{"name":"PointInterpolator","value":1675},{"name":"RectangleInterpolator","value":2042}]},{"name":"ISchedulable","value":1041},{"name":"Parallel","value":5176},{"name":"Pause","value":449},{"name":"Scheduler","value":5593},{"name":"Sequence","value":5534},{"name":"Transition","value":9201},{"name":"Transitioner","value":19975},{"name":"TransitionEvent","value":1116},{"name":"Tween","value":6006}]},{"name":"data","children":[{"name":"converters","children":[{"name":"Converters","value":721},{"name":"DelimitedTextConverter","value":4294},{"name":"GraphMLConverter","value":9800},{"name":"IDataConverter","value":1314},{"name":"JSONConverter","value":2220}]},{"name":"DataField","value":1759},{"name":"DataSchema","value":2165},{"name":"DataSet","value":586},{"name":"DataSource","value":3331},{"name":"DataTable","value":772},{"name":"DataUtil","value":3322}]},{"name":"display","children":[{"name":"DirtySprite","value":8833},{"name":"LineSprite","value":1732},{"name":"RectSprite","value":3623},{"name":"TextSprite","value":10066}]},{"name":"flex","children":[{"name":"FlareVis","value":4116}]},{"name":"physics","children":[{"name":"DragForce","value":1082},{"name":"GravityForce","value":1336},{"name":"IForce","value":319},{"name":"NBodyForce","value":10498},{"name":"Particle","value":2822},{"name":"Simulation","value":9983},{"name":"Spring","value":2213},{"name":"SpringForce","value":1681}]},{"name":"query","children":[{"name":"AggregateExpression","value":1616},{"name":"And","value":1027},{"name":"Arithmetic","value":3891},{"name":"Average","value":891},{"name":"BinaryExpression","value":2893},{"name":"Comparison","value":5103},{"name":"CompositeExpression","value":3677},{"name":"Count","value":781},{"name":"DateUtil","value":4141},{"name":"Distinct","value":933},{"name":"Expression","value":5130},{"name":"ExpressionIterator","value":3617},{"name":"Fn","value":3240},{"name":"If","value":2732},{"name":"IsA","value":2039},{"name":"Literal","value":1214},{"name":"Match","value":3748},{"name":"Maximum","value":843},{"name":"methods","children":[{"name":"add","value":593},{"name":"and","value":330},{"name":"average","value":287},{"name":"count","value":277},{"name":"distinct","value":292},{"name":"div","value":595},{"name":"eq","value":594},{"name":"fn","value":460},{"name":"gt","value":603},{"name":"gte","value":625},{"name":"iff","value":748},{"name":"isa","value":461},{"name":"lt","value":597},{"name":"lte","value":619},{"name":"max","value":283},{"name":"min","value":283},{"name":"mod","value":591},{"name":"mul","value":603},{"name":"neq","value":599},{"name":"not","value":386},{"name":"or","value":323},{"name":"orderby","value":307},{"name":"range","value":772},{"name":"select","value":296},{"name":"stddev","value":363},{"name":"sub","value":600},{"name":"sum","value":280},{"name":"update","value":307},{"name":"variance","value":335},{"name":"where","value":299},{"name":"xor","value":354},{"name":"_","value":264}]},{"name":"Minimum","value":843},{"name":"Not","value":1554},{"name":"Or","value":970},{"name":"Query","value":13896},{"name":"Range","value":1594},{"name":"StringUtil","value":4130},{"name":"Sum","value":791},{"name":"Variable","value":1124},{"name":"Variance","value":1876},{"name":"Xor","value":1101}]},{"name":"scale","children":[{"name":"IScaleMap","value":2105},{"name":"LinearScale","value":1316},{"name":"LogScale","value":3151},{"name":"OrdinalScale","value":3770},{"name":"QuantileScale","value":2435},{"name":"QuantitativeScale","value":4839},{"name":"RootScale","value":1756},{"name":"Scale","value":4268},{"name":"ScaleType","value":1821},{"name":"TimeScale","value":5833}]},{"name":"util","children":[{"name":"Arrays","value":8258},{"name":"Colors","value":10001},{"name":"Dates","value":8217},{"name":"Displays","value":12555},{"name":"Filter","value":2324},{"name":"Geometry","value":10993},{"name":"heap","children":[{"name":"FibonacciHeap","value":9354},{"name":"HeapNode","value":1233}]},{"name":"IEvaluable","value":335},{"name":"IPredicate","value":383},{"name":"IValueProxy","value":874},{"name":"math","children":[{"name":"DenseMatrix","value":3165},{"name":"IMatrix","value":2815},{"name":"SparseMatrix","value":3366}]},{"name":"Maths","value":17705},{"name":"Orientation","value":1486},{"name":"palette","children":[{"name":"ColorPalette","value":6367},{"name":"Palette","value":1229},{"name":"ShapePalette","value":2059},{"name":"SizePalette","value":2291}]},{"name":"Property","value":5559},{"name":"Shapes","value":19118},{"name":"Sort","value":6887},{"name":"Stats","value":6557},{"name":"Strings","value":22026}]},{"name":"vis","children":[{"name":"axis","children":[{"name":"Axes","value":1302},{"name":"Axis","value":24593},{"name":"AxisGridLine","value":652},{"name":"AxisLabel","value":636},{"name":"CartesianAxes","value":6703}]},{"name":"controls","children":[{"name":"AnchorControl","value":2138},{"name":"ClickControl","value":3824},{"name":"Control","value":1353},{"name":"ControlList","value":4665},{"name":"DragControl","value":2649},{"name":"ExpandControl","value":2832},{"name":"HoverControl","value":4896},{"name":"IControl","value":763},{"name":"PanZoomControl","value":5222},{"name":"SelectionControl","value":7862},{"name":"TooltipControl","value":8435}]},{"name":"data","children":[{"name":"Data","value":20544},{"name":"DataList","value":19788},{"name":"DataSprite","value":10349},{"name":"EdgeSprite","value":3301},{"name":"NodeSprite","value":19382},{"name":"render","children":[{"name":"ArrowType","value":698},{"name":"EdgeRenderer","value":5569},{"name":"IRenderer","value":353},{"name":"ShapeRenderer","value":2247}]},{"name":"ScaleBinding","value":11275},{"name":"Tree","value":7147},{"name":"TreeBuilder","value":9930}]},{"name":"events","children":[{"name":"DataEvent","value":2313},{"name":"SelectionEvent","value":1880},{"name":"TooltipEvent","value":1701},{"name":"VisualizationEvent","value":1117}]},{"name":"legend","children":[{"name":"Legend","value":20859},{"name":"LegendItem","value":4614},{"name":"LegendRange","value":10530}]},{"name":"operator","children":[{"name":"distortion","children":[{"name":"BifocalDistortion","value":4461},{"name":"Distortion","value":6314},{"name":"FisheyeDistortion","value":3444}]},{"name":"encoder","children":[{"name":"ColorEncoder","value":3179},{"name":"Encoder","value":4060},{"name":"PropertyEncoder","value":4138},{"name":"ShapeEncoder","value":1690},{"name":"SizeEncoder","value":1830}]},{"name":"filter","children":[{"name":"FisheyeTreeFilter","value":5219},{"name":"GraphDistanceFilter","value":3165},{"name":"VisibilityFilter","value":3509}]},{"name":"IOperator","value":1286},{"name":"label","children":[{"name":"Labeler","value":9956},{"name":"RadialLabeler","value":3899},{"name":"StackedAreaLabeler","value":3202}]},{"name":"layout","children":[{"name":"AxisLayout","value":6725},{"name":"BundledEdgeRouter","value":3727},{"name":"CircleLayout","value":9317},{"name":"CirclePackingLayout","value":12003},{"name":"DendrogramLayout","value":4853},{"name":"ForceDirectedLayout","value":8411},{"name":"IcicleTreeLayout","value":4864},{"name":"IndentedTreeLayout","value":3174},{"name":"Layout","value":7881},{"name":"NodeLinkTreeLayout","value":12870},{"name":"PieLayout","value":2728},{"name":"RadialTreeLayout","value":12348},{"name":"RandomLayout","value":870},{"name":"StackedAreaLayout","value":9121},{"name":"TreeMapLayout","value":9191}]},{"name":"Operator","value":2490},{"name":"OperatorList","value":5248},{"name":"OperatorSequence","value":4190},{"name":"OperatorSwitch","value":2581},{"name":"SortOperator","value":2023}]},{"name":"Visualization","value":16540}]}]}';
	  //let data = '{"name":"test1","children":[{"name":"child1"}]}';
	  //data = JSON.parse(data);
	  
	  let data = this.nodes[0];
	  
	  const root = this.myTree(data);

    //const svg = d3.select("");
    const svg = d3.create("svg");
  
    // Edge
    svg.append("g")
      .attr("fill", "none")
      .attr("stroke", "#555")
      .attr("stroke-opacity", 0.4)
      .attr("stroke-width", 1.5)
    .selectAll("path")
      .data(root.links())
      .join("path")
        .attr("d", (d: any) => `
          M${d.target.x},${d.target.y}
          C${d.source.x},${d.target.y}
          ${d.source.x},${d.source.y}
          ${d.source.x},${d.source.y}
        `);
  
     // Use this if you want more curly lines
     //C${d.source.x + root.dx / 2},${d.target.y}
     // ${d.source.x + root.dx / 2},${d.source.y}
  
  
    // node dot
    //svg.append("g")
    //  .selectAll("circle")
    //  .data(root.descendants())
    //  .join("circle")
    //    .attr("cx", (d: any) => d.x)
    //    .attr("cy", (d: any) => d.y)
    //    .attr("fill", (d: any) => d.children ? "#555" : "#999")
    //    .attr("r", 2.5);
    
    let rectW = 150;
    let rectH = 25;
    
    // Header on square which denotes which hierarchy it's a part of
    svg.append("g")
        .selectAll("rect")
        .data(root.descendants())
        .join("rect")
          .attr("x", (d: any) => d.x - (rectW / 2))
          .attr("y", (d: any) => d.y - rectH + 8)
          .attr("fill", (d: any) => "#b3ad00")
          .attr("width", rectW)
          .attr("height", rectH/2)
          .attr("rx", 5)
    
    // Square around the label
    svg.append("g")
        .selectAll("rect")
        .data(root.descendants())
        .join("rect")
          .attr("x", (d: any) => d.x - (rectW / 2))
          .attr("y", (d: any) => d.y - (rectH / 2))
          .attr("fill", (d: any) => "#e0e0e0")
          .attr("width", rectW)
          .attr("height", rectH)
          .attr("rx", 5)
          
          
    // label
    svg.append("g")
        .attr("font-family", "sans-serif")
        .attr("font-size", 10)
        .attr("stroke-linejoin", "round")
        .attr("stroke-width", 3)
      .selectAll("text")
      .data(root.descendants())
      .join("text")
        .attr("x", (d:any) => d.x - 70)
        .attr("y", (d:any) => d.y - 4)
        .attr("dx", "0.31em")
        .attr("dy", (d:any) => 6)
        .text((d:any) => d.data.label)
      //.filter((d:any) => d.children)
      //  .attr("text-anchor", "end")
      //.clone(true).lower()
      //  .attr("stroke", "white");
        
  
    let autoBox = function() {
      document.body.appendChild(this);
      const {x, y, width, height} = this.getBBox();
      document.body.removeChild(this);
      return x + " " + y + " " + width + " " + height;
    }
  
    svg.attr("id", "svg");
    
    d3.select("#svg").remove();
    document.getElementById("svgHolder").appendChild(svg.attr("viewBox", autoBox).node());
	}
  
  private myTree(data): any {
    const root: any = d3.hierarchy(data).sort((a, b) => d3.descending(a.height, b.height) || d3.ascending(a.data.name, b.data.name));
    root.dx = 10;
    root.dy = this.svgWidth / (root.height + 1);
    return d3.cluster().nodeSize([root.dx, root.dy])(root);
  }
  
  private registerDragHandlers(): any {
    var dragHandler = d3.drag()
    .on("drag", function (event) {
        console.log("left", event.sourceEvent.pageX);
    
        d3.select(this)
            .style("position", "absolute")
            .style("left", event.sourceEvent.pageX)
            .style("top", event.sourceEvent.pageY);
    }).on("end", function(event) {
        d3.select(this)
            .style("position", "relative")
            .style("left", null)
            .style("top", null);
    });

    dragHandler(d3.selectAll(".sidebar-section-content ul.list-group li.list-group-item"));
  }

	ngAfterViewInit() {

	}

	isRA(): boolean {
		return this.authService.isRA();
	}

	isOrganizationRA(orgCode: string): boolean {
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

	public addChildAndRootToHierarchy(): void {
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
	}

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

	public onDrop($event: any) {
		// Dropped $event.element
		this.removeTreeNode($event.element)
	}

	public allowDrop(element: Element) {
		// Return true/false based on element
		return true;
	}

	public error(err: HttpErrorResponse): void {
		this.bsModalRef = this.modalService.show(ErrorModalComponent, { backdrop: true });
		this.bsModalRef.content.message = ErrorHandler.getMessageFromError(err);
	}

}
