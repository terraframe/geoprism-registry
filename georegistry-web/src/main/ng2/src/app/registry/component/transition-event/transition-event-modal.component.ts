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

/* D3 Stuffs */
import * as d3 from "d3";

export const DRAW_SCALE_MULTIPLIER: number = 1.0;

export const VIEWPORT_SCALE_FACTOR_X: number = 1.0;
export const VIEWPORT_SCALE_FACTOR_Y: number = 1.0;

@Component({
    selector: "transition-event-modal",
    templateUrl: "./transition-event-modal.component.html",
    styleUrls: ["./transition-event-modal.component.css"]
})
export class TransitionEventModalComponent implements OnInit, OnDestroy {

    @ViewChild("typeaheadParent") typeaheadParent;

    message: string = null;

    event: TransitionEvent = null;

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

    render: boolean = true;

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

        setTimeout(() => { this.onChange(); }, 0);
    }

    onCreate(): void {
        this.event.transitions.push({
            sourceCode: "",
            sourceType: "",
            targetCode: "",
            targetType: "",
            transitionType: "",
            impact: "",
            order: this.event.transitions.length - 1
        });
    }

    onChange(): void {
        this.calculateDerivedAttributes();
        this.renderVisual();
    }

    getTypeAheadObservable(transition: Transition, typeCode: string, property: string): Observable<any> {
        return new Observable((observer: any) => {
            this.rService.getGeoObjectSuggestions(transition[property], typeCode, null, null, null, this.event.eventDate, this.event.eventDate).then(results => {
                observer.next(results);
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

                if (sourceStats.source > 1) {
                    trans.impact = "PARTIAL";
                    trans.transitionType = "SPLIT";
                } else {
                    if (targetStats.target > 1) {
                        trans.transitionType = "MERGE";
                    } else {
                        trans.transitionType = "REASSIGN";
                    }

                    trans.impact = "FULL";
                }

                if (trans.sourceType !== trans.targetType) {
                    trans.typeUpdown = updown;
                    trans.typePart = trans.transitionType;
                    trans.transitionType = trans.typeUpdown + "_" + trans.typePart;
                }
            }
        });
    }

    onChangeTypeUpdown(transition: any): void {
        transition.transitionType = transition.typeUpdown + "_" + transition.typePart;
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

        // Insert us back into the array at ne1wIndex
        this.event.transitions.splice(newIndex, 0, transition);

        // Update order for all transitions as elements have shifted
        for (let i = 0; i < this.event.transitions.length; ++i) {
            this.event.transitions[i].order = i;
        }

        this.onChange();

        // this.changeDetector.detectChanges(); // Doesn't work
        // Angular front-end is having some sort of glitch where it doesn't render the table elements properly. This is the only
        // hack I've found which actually forces it to properly redraw the table.
        this.render = false;
        window.setTimeout(() => { this.render = true; }, 0);
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
                .attr("stroke", "#555")
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
                    `);

            svg.append("g")
                .selectAll("circle")
                .data(root.descendants())
                .join("circle")
                    .style("display", function(d: any) {
                        return d.depth === 0 ? "none" : null;
                    })
                    .attr("cx", (d: any) => d.y)
                    .attr("cy", (d: any) => d.x)
                    .attr("fill", (d: any) => d.children ? "#555" : "#999")
                    .attr("r", 0.9 * DRAW_SCALE_MULTIPLIER);

            svg.append("g")
                .attr("font-family", "sans-serif")
                .attr("font-size", 2 * DRAW_SCALE_MULTIPLIER)
                .attr("stroke-linejoin", "round")
                .attr("stroke-width", 3)
              .selectAll("text")
              .data(root.descendants())
              .join("text")
                .style("display", function(d: any) {
                    return d.depth === 0 ? "none" : null;
                })
                .attr("x", (d: any) => d.y)
                .attr("y", (d: any) => d.x)
                .attr("dy", "0.31em")
                .attr("dx", (d: any) => (d.depth === 1) ? -6 : 6)
                .text((d: any) => d.data.name)
              .filter((d: any) => d.depth === 1)
                .attr("text-anchor", "end")
              .clone(true).lower()
                .attr("stroke", "white");

            renderingData.multipleParentLinks.forEach(function(link) {
                links.append("path")
                    .attr("d", () => `
                      M${link.parent.y},${link.parent.x}
                       ${link.child.y},${link.child.x}
                    `);
            });

            // return svg.attr("viewBox", autoBox).node();
        }

        chart();

        this.calculateSvgViewBox();
    }

    generateRenderingData(appData: any): any {
        let width = 100;

        const root: any = d3.hierarchy(appData.d3Data).sort((a, b) => d3.ascending(a.data.order, b.data.order));
        root.dx = 5 * DRAW_SCALE_MULTIPLIER;
        root.dy = width / (root.height + 1);
        let d3RenderingData = d3.tree().nodeSize([root.dx, root.dy])(root);

        let multipleParentLinks = [];
        appData.multipleParentLinks.forEach(function(link) {
            let parentNode = root.find(node => node.data.isSource && node.data.code === link.parent.code);
            let childNode = root.find(node => !node.data.isSource && node.data.code === link.child.code);

            if (parentNode != null && childNode != null) {
                multipleParentLinks.push({
                    parent: parentNode,
                    child: childNode
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
            multipleParentLinks: multipleParentLinks
        };
    }

    calculateSvgViewBox(): void {
        let svg: any = d3.select("#svg");
        let svgNode: any = svg.node();

        let { x, y, width, height } = svgNode.getBBox();

        const xPadding = 0;
        const yPadding = 0;
        svg.attr("viewBox", (x - xPadding) + " " + (y - yPadding) + " " + (width + xPadding * 2) * VIEWPORT_SCALE_FACTOR_X + " " + (height + yPadding * 2) * VIEWPORT_SCALE_FACTOR_Y);

        // width = (width + xPadding * 2) * VIEWPORT_SCALE_FACTOR_X;
        // height = (height + yPadding * 2) * VIEWPORT_SCALE_FACTOR_Y;

        // d3.select("#svgHolder").style("width", width + "px");
        // d3.select("#svgHolder").style("height", height + "px");
    }

}
