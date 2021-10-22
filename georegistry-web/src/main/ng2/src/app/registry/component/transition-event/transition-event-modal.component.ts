import { Component, OnDestroy, OnInit } from "@angular/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Observable, Subject } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";

import { IOService, RegistryService } from "@registry/service";
import { DateService } from "@shared/service/date.service";

import { ErrorHandler } from "@shared/component";
import { LocalizationService, AuthService } from "@shared/service";
import { Transition, TransitionEvent } from "@registry/model/transition-event";
import { TransitionEventService } from "@registry/service/transition-event.service";

/* D3 Stuffs */
import * as d3 from "d3";
export const TREE_SCALE_FACTOR_X: number = 1.8;
export const TREE_SCALE_FACTOR_Y: number = 1.8;

@Component({
    selector: "transition-event-modal",
    templateUrl: "./transition-event-modal.component.html",
    styleUrls: []
})
export class TransitionEventModalComponent implements OnInit, OnDestroy {

    message: string = null;

    event: TransitionEvent = null;

    /*
     * Observable subject for MasterList changes.  Called when an update is successful
     */
    onEventChange: Subject<TransitionEvent>;

    /*
     * List of geo object types from the system
     */
    types: { label: string, code: string }[] = [];

    /*
     * List of geo object types from the system
     */
    readonly: boolean = false;

    // eslint-disable-next-line no-useless-constructor
    constructor(private service: TransitionEventService, public rService: RegistryService, private iService: IOService, private lService: LocalizationService, public bsModalRef: BsModalRef, private authService: AuthService,
        private dateService: DateService) { }

    ngOnInit(): void {
        this.onEventChange = new Subject();

        this.iService.listGeoObjectTypes(true).then(types => {
            let myOrgTypes = [];
            for (let i = 0; i < types.length; ++i) {
                const orgCode = types[i].orgCode;
                const typeCode = types[i].superTypeCode != null ? types[i].superTypeCode : types[i].code;

                if (this.authService.isGeoObjectTypeRM(orgCode, typeCode)) {
                    myOrgTypes.push(types[i]);
                }
            }
            this.types = myOrgTypes;
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
                description: this.lService.create(),
                transitions: []
            };
        }

        this.onChange();
    }

    onCreate(): void {
        this.event.transitions.push({
            sourceCode: "",
            sourceType: "",
            targetCode: "",
            targetType: "",
            transitionType: "",
            impact: ""
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
            transition.targetText = selection.item.name;
        } else {
            transition.sourceCode = selection.item.code;
            transition.sourceType = selection.item.typeCode;
            transition.sourceText = selection.item.name;
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
        return this.lService.decode("transition.event.type." + type.toLowerCase());
    }

    remove(index: number): void {
        this.event.transitions.splice(index, 1);
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

        /*
        <option value="MERGE"><localize key="transition.event.type.merge"></localize></option>
        <option value="SPLIT"><localize key="transition.event.type.split"></localize></option>
        <option value="UPGRADE"><localize key="transition.event.type.upgrade"></localize></option>
        <option value="DOWNGRADE"><localize key="transition.event.type.downgrade"></localize></option>
        <option value="REASSIGN"><localize key="transition.event.type.reassign"></localize></option>
        */

        this.event.transitions.forEach(trans => {
            if (trans.sourceCode != null && trans.sourceCode !== "" && trans.targetCode != null && trans.targetCode !== "") {
                let sourceStats = stats[trans.sourceCode];
                let targetStats = stats[trans.targetCode];

                if (sourceStats.source > 1) {
                    trans.impact = "PARTIAL";
                    trans.transitionType = "SPLIT";
                } else {
                    if (targetStats.target > 1) {
                        trans.transitionType = "MERGE";
                    } else if (trans.sourceType !== trans.targetType) {
                        trans.impact = "FULL";
                        trans.transitionType = "UPGRADE";
                    } else {
                        trans.transitionType = "REASSIGN";
                    }

                    trans.impact = "FULL";
                }
            }
        });
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

        let data = this.generateD3Data();

        let width = 100;

        /*
        let autoBox = function autoBox() {
            document.body.appendChild(this);
            const { x, y, width, height } = svg.node().getBBox();
            document.body.removeChild(this);
            return [x, y, width, height];
        };
        */

        let tree = data => {
            const root: any = d3.hierarchy(data).sort((a, b) => d3.descending(a.height, b.height) || d3.ascending(a.data.name, b.data.name));
            root.dx = 10;
            root.dy = width / (root.height + 1);
            return d3.cluster().nodeSize([root.dx, root.dy])(root);
        };

        let chart = () => {
            const root = tree(data);

            // const svg = d3.create("svg");

            svg.append("g")
              .attr("fill", "none")
              .attr("stroke", "#555")
              .attr("stroke-opacity", 0.4)
              .attr("stroke-width", 1.5)
            .selectAll("path")
              .data(root.links())
              .join("path")
                .style("display", function(d: any) {
                    return d.source.depth === 0 ? "none" : null;
                })
                .attr("d", d => `
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
                    .attr("cx", d => d.y)
                    .attr("cy", d => d.x)
                    .attr("fill", d => d.children ? "#555" : "#999")
                    .attr("r", 2.5);

            svg.append("g")
                .attr("font-family", "sans-serif")
                .attr("font-size", 5)
                .attr("stroke-linejoin", "round")
                .attr("stroke-width", 3)
              .selectAll("text")
              .data(root.descendants())
              .join("text")
                .style("display", function(d: any) {
                    return d.depth === 0 ? "none" : null;
                })
                .attr("x", d => d.y)
                .attr("y", d => d.x)
                .attr("dy", "0.31em")
                .attr("dx", d => d.children ? -6 : 6)
                .text(d => d.data.name)
              .filter(d => d.children)
                .attr("text-anchor", "end")
              .clone(true).lower()
                .attr("stroke", "white");

            // return svg.attr("viewBox", autoBox).node();
        }

        chart();

        this.calculateSvgViewBox();
    }

    generateD3Data(): any {
        let children = [];

        this.event.transitions.forEach(trans => {
            if (trans.sourceCode != null && trans.sourceCode !== "" && trans.targetCode != null && trans.targetCode !== "") {
                let index = children.findIndex(child => child.code === trans.sourceCode);

                if (index !== -1) {
                    let child = children[index];

                    let index2 = child.children.findIndex(child => child.code === trans.targetCode);

                    if (index2 != null) {
                        child.children.push({
                            name: trans.targetText,
                            code: trans.targetCode,
                            children: []
                        });
                    }
                } else {
                    let child = {
                        name: trans.sourceText,
                        code: trans.sourceCode,
                        children: [{
                            name: trans.targetText,
                            code: trans.targetCode,
                            children: []
                        }]
                    };

                    children.push(child);
                }
            }
        });

        return {
            name: "root",
            children: children
        };
    }

    calculateSvgViewBox(): void {
        let svg: any = d3.select("#svg");
        let svgNode: any = svg.node();

        let { x, y, width, height } = svgNode.getBBox();

        const xPadding = 0;
        const yPadding = 0;
        svg.attr("viewBox", (x - xPadding) + " " + (y - yPadding) + " " + (width + xPadding * 2) + " " + (height + yPadding * 2));

        width = (width + xPadding * 2) * TREE_SCALE_FACTOR_X;
        height = (height + yPadding * 2) * TREE_SCALE_FACTOR_Y;

        //d3.select("#svgHolder").style("width", width + "px");
        // d3.select("#svgHolder").style("height", height + "px");
    }

}
