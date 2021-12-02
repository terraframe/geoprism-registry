/* eslint-disable indent */
/* eslint-disable quotes */
import { Component, OnDestroy, OnInit, ViewChild, ChangeDetectorRef } from "@angular/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Observable, Subject } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";

import { IOService, RegistryService } from "@registry/service";
import { DateService } from "@shared/service/date.service";

import { ErrorHandler } from "@shared/component";
import { LocalizationService, AuthService } from "@shared/service";
import { Transition, TransitionEvent } from "@registry/model/transition-event";
import { TransitionEventService } from "@registry/service/transition-event.service";

import { DndDropEvent } from "ngx-drag-drop";
import * as uuid from "uuid";

/* D3 Stuffs */
import * as d3 from "d3";

export const DRAW_SCALE_MULTIPLIER: number = 1.0;

export const VIEWPORT_SCALE_FACTOR_X: number = 1.0;
export const VIEWPORT_SCALE_FACTOR_Y: number = 1.0;

export const GRAPH_ACTIVE_TRANSITION_HIGHLIGHT_COLOR: string = "#6BA542"; // #3E2A5A or "purple"
export const GRAPH_GO_LABEL_COLOR: string = "black";
export const GRAPH_CIRCLE_FILL: string = "#999";
export const GRAPH_LINE_COLOR: string = "#999";

@Component({
    selector: "transition-event-modal",
    templateUrl: "./transition-event-modal.component.html",
    styleUrls: ["./transition-event-modal.component.css"]
})
export class TransitionEventModalComponent implements OnInit, OnDestroy {

    @ViewChild("typeaheadParent") typeaheadParent;

    message: string = null;

    event: TransitionEvent = null;

    activeTransition: Transition = null;

    /*
     * Observable subject for MasterList changes.  Called when an update is successful
     */
    onEventChange: Subject<TransitionEvent>;

    /*
     * All Geo-ObjectTypes in the system
     */
    allTypes: { label: string, code: string, orgCode: string, superTypeCode?: string }[] = [];

    /*
     * Types that we have write permission to
     */
    types: { label: string, code: string, orgCode: string, superTypeCode?: string }[] = [];

    /*
     * List of geo object types from the system
     */
    readonly: boolean = false;

    valid: boolean = false;

    draggable = {
        // note that data is handled with JSON.stringify/JSON.parse
        // only set simple data or POJO's as methods will be lost
        data: "myDragData",
        effectAllowed: "all",
        disable: false,
        handle: true
    };

    // eslint-disable-next-line no-useless-constructor
    constructor(private service: TransitionEventService, private changeDetector: ChangeDetectorRef, public rService: RegistryService, private iService: IOService, private lService: LocalizationService, public bsModalRef: BsModalRef, private authService: AuthService,
        private dateService: DateService) { }

    ngOnInit(): void {
        this.onEventChange = new Subject();

        this.iService.listGeoObjectTypes(false).then(types => {
            let myOrgTypes = [];
            for (let i = 0; i < types.length; ++i) {
                const orgCode = types[i].orgCode;
                const typeCode = types[i].superTypeCode != null ? types[i].superTypeCode : types[i].code;

                if (this.authService.isGeoObjectTypeRM(orgCode, typeCode)) {
                    myOrgTypes.push(types[i]);
                }
            }
            this.types = myOrgTypes;
            this.allTypes = types;

            this.readonly = this.readonly || this.event.permissions.indexOf("WRITE") === -1;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    ngOnDestroy(): void {
        this.onEventChange.unsubscribe();
    }

    init(readonly: boolean, event?: TransitionEvent): void {
        this.readonly = readonly;

        if (event != null) {
            this.event = event;
        } else {
            this.event = {
                beforeTypeCode: "",
                afterTypeCode: "",
                eventDate: "",
                permissions: ["WRITE", "READ", "DELETE", "CREATE"],
                description: this.lService.create(),
                transitions: []
            };
        }

        setTimeout(() => {
            this.onChange();
        }, 0);
    }

    setActiveTransition(transition: Transition) {
        let highlight = (active: boolean, trans: Transition) => {
            let colorable = d3.selectAll('#svgHolder p[data-goCode="' + trans.sourceCode + '"][data-depth="1"],p[data-goCode="' + trans.targetCode + '"][data-depth="2"]');
            colorable.style("color", active ? GRAPH_ACTIVE_TRANSITION_HIGHLIGHT_COLOR : GRAPH_GO_LABEL_COLOR);

            let fillable = d3.selectAll('#svgHolder circle[data-goCode="' + trans.sourceCode + '"][data-depth="1"],circle[data-goCode="' + trans.targetCode + '"][data-depth="2"]');
            fillable.attr("fill", active ? GRAPH_ACTIVE_TRANSITION_HIGHLIGHT_COLOR : GRAPH_CIRCLE_FILL);

            let strokeable = d3.selectAll('#svgHolder path[data-transOid="' + trans.oid + '"]');
            strokeable.attr("stroke", active ? GRAPH_ACTIVE_TRANSITION_HIGHLIGHT_COLOR : GRAPH_LINE_COLOR);
        };

        if (this.activeTransition != null) {
            highlight(false, this.activeTransition);
        }

        this.activeTransition = transition;

        if (transition != null) {
            highlight(true, transition);
        }
    }

    onCreate(): void {
        this.event.transitions.push({
            oid: uuid.v4(),
            isNew: true,
            sourceCode: "",
            sourceType: "",
            targetCode: "",
            targetType: "",
            transitionType: "",
            impact: "",
            order: this.event.transitions.length
        });
    }

    onChange(): void {
        this.calculateDerivedAttributes();
        this.renderVisual();

        // Register highlight event listeners
        let that = this;

        setTimeout(() => {
            d3.selectAll(".transition").on("mouseover", function(mouseEvent) {
                let d3This: any = this;
                let transitionOid = d3This.getAttribute("data-transOid");

                let index = that.event.transitions.findIndex(trans => trans.oid === transitionOid);

                that.setActiveTransition(that.event.transitions[index]);
            });
            d3.select("#transition-container").on("mouseleave", function(mouseEvent) {
                that.setActiveTransition(null);
            });
        }, 0);

        this.validChange();
    }

    getTypeAheadObservable(isSource: boolean, transition: Transition, typeCode: string, property: string): Observable<any> {
        let date = isSource ? this.dateService.addDay(-1, this.event.eventDate) : this.event.eventDate;
        return new Observable((observer: any) => {
            this.rService.getGeoObjectSuggestions(transition[property], typeCode, null, null, null, date, date).then(results => {
                let filtered = results.filter(result => {
                  let pair = {
                    sourceCode: isSource ? result.code : transition.sourceCode,
                    targetCode: isSource ? transition.targetCode : result.code
                  };

                  for (let i = 0; i < this.event.transitions.length; ++i) {
                      let transition = this.event.transitions[i];

                      if (transition.sourceCode === pair.sourceCode && transition.targetCode === pair.targetCode) {
                          return false;
                      }
                  }

                  return true;
                });

                observer.next(filtered);
            });
        });
    }

    typeaheadOnSelect(selection: any, transition: Transition, property: string): void {
        if (property === "targetText") {
            transition.targetCode = selection.item.code;
            transition.targetType = selection.item.typeCode;
            transition.targetText = selection.item.name + " (" + selection.item.code + ")";
        } else {
            transition.sourceCode = selection.item.code;
            transition.sourceType = selection.item.typeCode;
            transition.sourceText = selection.item.name + " (" + selection.item.code + ")";
        }

        this.onChange();
    }

    clear(transition: Transition, property: string): void {
        if (property === "targetText") {
            transition.targetCode = "";
            transition.targetType = "";
            transition.targetText = "";
        } else {
            transition.sourceCode = "";
            transition.sourceType = "";
            transition.sourceText = "";
        }

        this.onChange();
    }

    localizeTransitionImpact(impact: string): string {
        return this.lService.decode("transition.event." + impact.toLowerCase());
    }

    localizeTransitionType(type: string): string {
        return type == null ? null : this.lService.decode("transition.event.type." + type.toLowerCase());
    }

    validChange() {
        setTimeout(() => {
            this.valid = (this.event.eventDate != null && this.event.eventDate.length > 0) &&
                this.event.transitions.length > 0 &&
                this.event.afterTypeCode != null &&
                this.event.beforeTypeCode != null;
        }, 0);
    }

    remove(index: number): void {
        this.event.transitions.splice(index, 1);
        this.onChange();
    }

    onSubmit(): void {
        this.service.apply(this.event).then(response => {
            this.onEventChange.next(response);
            this.bsModalRef.hide();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onCancel(): void {
        this.bsModalRef.hide();
    }

    formatDate(date: string): string {
        return this.dateService.formatDateForDisplay(date);
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

    calculateDerivedAttributes(): void {
        let stats = {};
        this.event.transitions.forEach(trans => {
            if (trans.sourceCode != null && trans.sourceCode !== "" && trans.targetCode != null && trans.targetCode !== "") {
                if (stats[trans.sourceCode] == null) {
                    stats[trans.sourceCode] = {
                        source: 1,
                        target: 0
                    };
                } else {
                    stats[trans.sourceCode].source++;
                }

                if (stats[trans.targetCode] == null) {
                    stats[trans.targetCode] = {
                        source: 0,
                        target: 1
                    };
                } else {
                    stats[trans.targetCode].target++;
                }
            }
        });

        this.event.transitions.forEach(trans => {
            if (trans.sourceCode != null && trans.sourceCode !== "" && trans.targetCode != null && trans.targetCode !== "") {
                let sourceStats = stats[trans.sourceCode];
                let targetStats = stats[trans.targetCode];

                let updown = "UPGRADE";
                if (trans.transitionType != null && trans.transitionType.indexOf("DOWNGRADE") !== -1) {
                    updown = "DOWNGRADE";
                }

                if (sourceStats.source === 1 && targetStats.target === 1) {
                    trans.transitionType = "REASSIGN";
                    trans.impact = "FULL";
                } else if (sourceStats.source > 1) {
                    trans.impact = "PARTIAL";

                    if (targetStats.target > 1) {
                        trans.transitionType = "MERGE";
                    } else {
                        trans.transitionType = "SPLIT";
                    }
                } else if (targetStats.target > 1) {
                    trans.impact = "FULL";
                    trans.transitionType = "MERGE";
                }

                if (trans.sourceType !== trans.targetType) {
                    if (trans.transitionType === "REASSIGN") {
                        trans.typeUpdown = updown;
                        delete trans.typePart;
                        trans.transitionType = trans.typeUpdown;
                    } else {
                        trans.typeUpdown = updown;
                        trans.typePart = trans.transitionType;
                        trans.transitionType = trans.typeUpdown + "_" + trans.typePart;
                    }
                } else {
                    delete trans.typePart;
                }
            }
        });
    }

    onChangeTypeUpdown(transition: any): void {
        if (transition.typePart) {
            transition.transitionType = transition.typeUpdown + "_" + transition.typePart;
        } else {
            transition.transitionType = transition.typeUpdown;
        }
    }

    /* Drag Drop Transitions */
    onDragStart(event:DragEvent) {
        // console.log("drag started", JSON.stringify(event, null, 2));
    }

    onDragEnd(event:DragEvent) {
        // console.log("drag ended", JSON.stringify(event, null, 2));
    }

    onDragged(item: any, type: string) {
        // console.log("onDragged", item, type);
    }

    onDraggableCopied(event:DragEvent) {
        // console.log("draggable copied", JSON.stringify(event, null, 2));
    }

    onDraggableLinked(event:DragEvent) {
        // console.log("draggable linked", JSON.stringify(event, null, 2));
    }

    onDraggableMoved(event:DragEvent) {
        // console.log("draggable moved", JSON.stringify(event, null, 2));
    }

    onDragCanceled(event:DragEvent) {
        // console.log("drag cancelled", JSON.stringify(event, null, 2));
    }

    onDragover(event:DragEvent) {
        // console.log("dragover", JSON.stringify(event, null, 2));
    }

    onDrop(event:DndDropEvent) {
        let transition: Transition = event.data;
        let index: number = event.index;

        // Remove from array
        this.event.transitions.splice(transition.order, 1);

        // Calculate new index, which may have shifted due to us removing the transition.
        let newIndex = (index > transition.order) ? index - 1 : index;

        // Insert us back into the array at newIndex
        this.event.transitions.splice(newIndex, 0, transition);

        // Update order for all transitions as elements have shifted
        for (let i = 0; i < this.event.transitions.length; ++i) {
            this.event.transitions[i].order = i;
        }

        window.setTimeout(() => { this.onChange(); }, 0);
    }

    /* D3 Stuff */
    private renderVisual(): void {
        if (this.event.transitions == null || this.event.transitions.length === 0) {
            d3.select("#svg").remove();
            return;
        }
        d3.select("#svg").remove();

        let svg = d3.select("#svg");

        if (svg.node() == null) {
            svg = d3.select("#svgHolder").append("svg");
            svg.attr("id", "svg");
        }

        let appData = this.generateAppData();
        let renderingData = this.generateRenderingData(appData);

        /*
        let autoBox = function autoBox() {
            document.body.appendChild(this);
            const { x, y, width, height } = svg.node().getBBox();
            document.body.removeChild(this);
            return [x, y, width, height];
        };
        */

        let chart = () => {
            const root = renderingData.d3;

            let links = svg.append("g")
                .attr("fill", "none")
                .attr("stroke", GRAPH_LINE_COLOR)
                .attr("stroke-opacity", 0.4)
                .attr("stroke-width", 0.4 * DRAW_SCALE_MULTIPLIER);
            links.selectAll("path")
                .data(root.links())
                .join("path")
                    .style("display", function(d: any) {
                        return d.source.depth === 0 ? "none" : null;
                    })
                    .attr("d", (d: any) => `
                      M${d.target.y},${d.target.x}
                       ${d.source.y},${d.source.x}
                    `)
                    .attr("data-transOid", (d: any) => d.source.data.name === "root" ? null : appData.linkDataMappings[d.source.data.code + ":" + d.target.data.code]);

            svg.append("g")
                .selectAll("circle")
                .data(root.descendants())
                .join("circle")
                    .style("display", function(d: any) {
                        return d.depth === 0 ? "none" : null;
                    })
                    .attr("cx", (d: any) => d.y)
                    .attr("cy", (d: any) => d.x)
                    .attr("fill", (d: any) => GRAPH_CIRCLE_FILL)
                    .attr("r", 0.9 * DRAW_SCALE_MULTIPLIER)
                    .attr("data-goCode", (d: any) => d.data.code)
                    .attr("data-depth", (d: any) => d.depth);

            svg.append("g")
                .attr("font-family", "sans-serif")
                .attr("font-size", 2 * DRAW_SCALE_MULTIPLIER)
                .attr("stroke-linejoin", "round")
                .attr("stroke-width", 3)
              .selectAll("foreignObject")
              .data(root.descendants())
              .join("foreignObject")
                .style("display", function(d: any) {
                    return d.depth === 0 ? "none" : null;
                })
                .attr("x", (d: any) => (d.y + ((d.depth === 1) ? -13 : 1)))
                .attr("y", (d: any) => (d.x) + ((d.depth === 1) ? -2 : -3))
                .attr("font-size", "0.7em")
                .attr("font-family", "sans-serif")
                .attr("font-weight", "bold")
                .attr("width", 12)
                .attr("height", 8)
              .append("xhtml:p")
                .attr("xmlns", "http://www.w3.org/1999/xhtml")
                .attr("data-goCode", (d: any) => d.data.code)
                .attr("data-depth", (d: any) => d.depth)
                .style("margin", "0.5px")
                .style("vertical-align", "middle")
                .style("line-height", 1.5)
                .style("color", GRAPH_GO_LABEL_COLOR)
                .style("padding-left", "0.4px")
                .style("padding-top", "0.2px")
                .html((d: any) => d.data.name)
              .filter((d: any) => d.depth === 1)
                .style("text-align", "right");

            renderingData.multipleParentLinks.forEach(function(link) {
                links.append("path")
                    .attr("d", () => `
                      M${link.parent.y},${link.parent.x}
                       ${link.child.y},${link.child.x}
                    `)
                    .attr("data-transOid", () => link.oid);
            });

            // return svg.attr("viewBox", autoBox).node();
        };

        chart();

        this.calculateSvgViewBox();
    }

    generateRenderingData(appData: any): any {
        let width = 100;

        const root: any = d3.hierarchy(appData.d3Data).sort((a, b) => d3.ascending(a.data.order, b.data.order));
        root.dx = 5 * DRAW_SCALE_MULTIPLIER;
        root.dy = width / (root.height + 1);
        let d3RenderingData = d3.tree().nodeSize([root.dx, root.dy]).separation(() => 1.5)(root);

        let multipleParentLinks = [];
        appData.multipleParentLinks.forEach(function(link) {
            let parentNode = root.find(node => node.data.isSource && node.data.code === link.parent.code);
            let childNode = root.find(node => !node.data.isSource && node.data.code === link.child.code);

            if (parentNode != null && childNode != null) {
                multipleParentLinks.push({
                    parent: parentNode,
                    child: childNode,
                    oid: link.oid
                });
            }
        });

        return {
            d3: d3RenderingData,
            multipleParentLinks: multipleParentLinks
        };
    }

    generateAppData(): any {
        let children = [];
        let multipleParentLinks = []; // D3 can't handle multiple parents so we have to draw them ourselves.
        let linkDataMappings = {}; // D3 doesn't allow us to put data on the link itself. Our link needs an oid. So this is a hack to store data on a link.

        let isChildOfOtherNode = (code: string) => {
            for (let i = 0; i < children.length; ++i) {
                let child = children[i];

                if (child.children) {
                    for (let j = 0; j < child.children.length; ++j) {
                        let grandChild = child.children[j];

                        if (grandChild.code === code) {
                            return true;
                        }
                    }
                }
            }

            return false;
        };

        this.event.transitions.forEach(trans => {
            if (trans.sourceCode != null && trans.sourceCode !== "" && trans.targetCode != null && trans.targetCode !== "") {
                let index = children.findIndex(child => child.code === trans.sourceCode);
                linkDataMappings[trans.sourceCode + ":" + trans.targetCode] = trans.oid;

                let childExists = isChildOfOtherNode(trans.targetCode);
                let grandChild = null;
                if (!childExists) {
                    grandChild = {
                        name: trans.targetText,
                        code: trans.targetCode,
                        isSource: false,
                        children: []
                    };
                } else {
                    multipleParentLinks.push({
                        oid: trans.oid,
                        child: {
                            code: trans.targetCode,
                            text: trans.targetText,
                            type: trans.targetType
                        },
                        parent: {
                            code: trans.sourceCode,
                            text: trans.sourceText,
                            type: trans.sourceType
                        }
                    });
                }

                if (index !== -1) {
                    let child = children[index];

                    let index2 = child.children.findIndex(child => child.code === trans.targetCode);

                    if (index2 != null && grandChild != null) {
                        child.children.push(grandChild);
                    }
                } else {
                    let child: any = {
                        name: trans.sourceText,
                        code: trans.sourceCode,
                        children: [],
                        isSource: true
                    };

                    if (grandChild != null) {
                        child.children.push(grandChild);
                    }

                    children.push(child);
                }
            }
        });

        return {
            d3Data: {
                name: "root",
                children: children
            },
            multipleParentLinks: multipleParentLinks,
            linkDataMappings: linkDataMappings
        };
    }

    calculateSvgViewBox(): void {
        let svg: any = d3.select("#svg");
        let svgNode: any = svg.node();

        let { x, y, width, height } = svgNode.getBBox();

        const xPadding = 0;
        const yPadding = 2;
        svg.attr("viewBox", (x - xPadding) + " " + (y - yPadding) + " " + (width + xPadding * 2) * VIEWPORT_SCALE_FACTOR_X + " " + (height + yPadding * 2) * VIEWPORT_SCALE_FACTOR_Y);

        // width = (width + xPadding * 2) * VIEWPORT_SCALE_FACTOR_X;
        // height = (height + yPadding * 2) * VIEWPORT_SCALE_FACTOR_Y;

        // d3.select("#svgHolder").style("width", width + "px");
        // d3.select("#svgHolder").style("height", height + "px");
    }

}
