/* eslint-disable indent */
import { Component, OnInit } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";
import * as d3 from "d3";
import * as d3Dag from "d3-dag";

import { ErrorHandler } from "@shared/component";
import { LocalizationService, AuthService } from "@shared/service";

import { RegistryService, HierarchyService } from "@registry/service";

export const DRAW_SCALE_MULTIPLIER: number = 1.0;

export const GRAPH_GO_LABEL_COLOR: string = "black";
export const GRAPH_CIRCLE_FILL: string = "#999";
export const GRAPH_LINE_COLOR: string = "#999";

@Component({

    selector: "relationship-visualizer",
    templateUrl: "./relationship-visualizer.component.html",
    styleUrls: ["./relationship-visualizer.css"]
})
export class RelationshipVisualizerComponent implements OnInit {

  /*
   * Reference to the modal current showing
  */
  private bsModalRef: BsModalRef;

  private data: any = {
      links: [["1", "2"], ["1", "22"], ["2", "3"]],
      nodes: [{id:"1", level:1}, {id:"2", level:2}, {id:"22", level:2}, {id:"3", level:3}]
  };

  // eslint-disable-next-line no-useless-constructor
  constructor(hierarchyService: HierarchyService, private modalService: BsModalService,
      localizeService: LocalizationService, private registryService: RegistryService, private authService: AuthService) {}

  ngOnInit(): void {
      this.draw();
  }

  private draw(): void {
      const connect = d3Dag.dagConnect();
      const dag = connect(this.data.links);

      /*
      // Topological layout
      let layout = d3Dag.zherebko();
      layout = layout.size([8,8]);
      layout(dag);
      */

      let layout = d3Dag.sugiyama();
      layout = layout.nodeSize(node => node === undefined ? [0, 0] : [200, 200]);
      layout(dag);

      d3.select("#svg").remove();

      let svg = d3.select("#svg");

      if (svg.node() == null) {
          svg = d3.select("#svgHolder").append("svg");
          svg.attr("id", "svg");
      }

      let links = svg.append("g")
          .attr("fill", "none")
          .attr("stroke", GRAPH_LINE_COLOR)
          .attr("stroke-opacity", 0.4)
          .attr("stroke-width", 10 * DRAW_SCALE_MULTIPLIER);
      links.selectAll("path")
          .data(dag.links())
          .join("path")
              .attr("d", (d: any) => `
                M${d.target.y},${d.target.x}
                 ${d.source.y},${d.source.x}
              `);

      const circleRadius: number = 20 * DRAW_SCALE_MULTIPLIER;
      svg.append("g")
          .selectAll("circle")
          .data(dag.descendants())
          .join("circle")
              .attr("cx", (d: any) => d.y)
              .attr("cy", (d: any) => d.x)
              .attr("fill", (d: any) => GRAPH_CIRCLE_FILL)
              .attr("r", circleRadius);

      svg.append("g")
                .attr("font-family", "sans-serif")
                .attr("font-size", circleRadius)
                .attr("stroke-linejoin", "round")
                .attr("stroke-width", 3)
              .selectAll("foreignObject")
              .data(dag.descendants())
              .join("foreignObject")
                .attr("x", (d: any) => (d.y - circleRadius))
                .attr("y", (d: any) => (d.x + circleRadius))
                .attr("width", circleRadius * 2)
                .attr("height", 25)
              .append("xhtml:p")
                .attr("xmlns", "http://www.w3.org/1999/xhtml")
                .style("margin", "0px")
                .style("vertical-align", "middle")
                .style("text-align", "center")
                .style("color", GRAPH_GO_LABEL_COLOR)
                .html((d: any) => d.data.id);

      this.calculateSvgViewBox();
  }

  calculateSvgViewBox(): void {
      let svg: any = d3.select("#svg");
      let svgNode: any = svg.node();

      let { x, y, width, height } = svgNode.getBBox();

      const xPadding = 0;
      const yPadding = 1.0;
      svg.attr("viewBox", (x - xPadding) + " " + (y - yPadding) + " " + (width + xPadding * 2) + " " + (height + yPadding * 2));

      // width = (width + xPadding * 2) * VIEWPORT_SCALE_FACTOR_X;
      // height = (height + yPadding * 2) * VIEWPORT_SCALE_FACTOR_Y;

      // d3.select("#svgHolder").style("width", width + "px");
      // d3.select("#svgHolder").style("height", height + "px");
  }

  public error(err: HttpErrorResponse): void {
      this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
  }

}
