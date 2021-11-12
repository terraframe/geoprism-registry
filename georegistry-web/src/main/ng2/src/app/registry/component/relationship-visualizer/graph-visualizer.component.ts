/* eslint-disable indent */
import { Component, OnInit, Input, SimpleChanges } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import { ErrorHandler } from "@shared/component";
import { LocalizationService, AuthService } from "@shared/service";

import { HierarchyService, IOService } from "@registry/service";
import { GeoObject } from "@registry/model/registry";
import { Subject } from "rxjs";
import { RelationshipVisualizationService } from "@registry/service/relationship-visualization.service";

export const DRAW_SCALE_MULTIPLIER: number = 1.0;

export const GRAPH_GO_LABEL_COLOR: string = "black";
export const GRAPH_CIRCLE_FILL: string = "#999";
export const GRAPH_LINE_COLOR: string = "#999";

@Component({

    selector: "graph-visualizer",
    templateUrl: "./graph-visualizer.component.html",
    styleUrls: ["./graph-visualizer.css"]
})
export class GraphVisualizerComponent implements OnInit {

  /*
   * Reference to the modal current showing
  */
  private bsModalRef: BsModalRef;

  @Input() geoObject: GeoObject = null;

  data: any = null;

  private width: number = 500;
  private height: number = 500;

  panToNode$: Subject<string> = new Subject();

  update$: Subject<boolean> = new Subject();

  // eslint-disable-next-line no-useless-constructor
  constructor(private hierarchyService: HierarchyService, private modalService: BsModalService, private ioService: IOService,
      localizeService: LocalizationService, private vizService: RelationshipVisualizationService, private authService: AuthService) {}

  ngOnInit(): void {
      this.fetchData();
  }

  ngOnChanges(changes: SimpleChanges) {
      if (changes.geoObject && changes.geoObject.previousValue !== changes.geoObject.currentValue) {
          this.update$.next(true);
      }
  }

  private fetchData(): void {
      this.vizService.fetchGraphData().then(data => {
          let graphContainer = document.getElementById("graph-container");
          this.height = graphContainer.clientHeight;
          this.width = graphContainer.clientWidth;

          this.data = data;

          if (this.geoObject != null) {
              window.setTimeout(() => {
                  this.panToNode$.next("g-" + this.geoObject.properties.uid);
              }, 10);
          }
      });
  }

  private stringify(data: any): string {
    return JSON.stringify(data);
  }

  public error(err: HttpErrorResponse): void {
      this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
  }

}
