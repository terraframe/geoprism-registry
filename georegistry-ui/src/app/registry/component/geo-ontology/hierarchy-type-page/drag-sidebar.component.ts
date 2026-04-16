///
/// Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Geoprism Registry(tm).
///
/// Geoprism Registry(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Geoprism Registry(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { Component, Input, Output, EventEmitter, SimpleChanges, AfterViewInit, OnChanges } from "@angular/core";

import * as d3 from "d3";

import { GeoObjectType } from "@registry/model/registry";
import { Organization } from "@shared/model/core";

@Component({
    selector: "drag-sidebar",
    templateUrl: "./drag-sidebar.component.html",
    styleUrls: ["./hierarchy-type-page.css"]
})
export class DragSidebarComponent implements AfterViewInit, OnChanges {

    @Input() typesByOrg: { org: Organization, types: GeoObjectType[] }[] = [];

    @Output() onDrag = new EventEmitter<{dragEl: Element, dropEl: Element, event: any}>();

    @Output() onDrop = new EventEmitter<{dragEl: Element, event: any}>();


    constructor() {
    }


    ngAfterViewInit(): void {
        this.registerDragHandlers();        
    }

    ngOnChanges(changes: SimpleChanges): void {

        setTimeout(() => { this.registerDragHandlers(); }, 100);
    }

    registerDragHandlers(): any {
        console.log('Updating Drag Handlers')

        const that = this;

        // GeoObjectTypes and Hierarchies
        let deltaX: number, deltaY: number, width: number;
        let sidebarDragHandler = d3.drag()
            .on("start", function (event: any) {
                let canDrag = d3.select(this).attr("data-candrag");
                if (canDrag === "false") {
                    return;
                }

                let rect = this.getBoundingClientRect();
                deltaX = rect.left - event.sourceEvent.pageX;
                deltaY = rect.top - event.sourceEvent.pageY;
                width = rect.width;
            })
            .on("drag", function (event: any) {
                let canDrag = d3.select(this).attr("data-candrag");
                if (canDrag === "false") {
                    return;
                }

                d3.select(".g-context-menu").remove();

                let selThis = d3.select(this);

                // Kind of a dumb hack, but if we hide our drag element for a sec, then we can check what's underneath it.
                selThis.style("display", "none");

                let target = document.elementFromPoint(event.sourceEvent.pageX, event.sourceEvent.pageY);

                selThis.style("display", null);

                that.onDrag.emit({dragEl: this, dropEl: target, event})

                // for (let i = 0; i < dropTargets.length; ++i) {
                //     dropTargets[i].onDrag(this, target, event);
                // }

                // Move the GeoObjectType with the pointer when they move their mouse
                selThis
                    .classed("dragging", true)
                    .style("left", (event.sourceEvent.pageX + deltaX) + "px")
                    .style("top", (event.sourceEvent.pageY + deltaY) + "px")
                    .style("width", width + "px");

                // If they are moving a GOT group then we have to move the children as well
                if (selThis.classed("got-group-parent")) {
                    let index = 1;
                    d3.selectAll(".got-group-child[data-superTypeCode=\"" + selThis.attr("id") + "\"]").each(function () {
                        let li: any = this;
                        let child = d3.select(li);

                        child
                            .classed("dragging", true)
                            .style("left", (event.sourceEvent.pageX + deltaX) + "px")
                            .style("top", (event.sourceEvent.pageY + deltaY + (li.getBoundingClientRect().height + 2) * index) + "px")
                            .style("width", width + "px");

                        index++;
                    });
                }
            }).on("end", function (event: any) {
                let selThis = d3.select(this)
                    .classed("dragging", false)
                    .style("left", null)
                    .style("top", null)
                    .style("width", null);

                // If they are moving a GOT group then we have to reset the children as well
                if (selThis.classed("got-group-parent")) {
                    d3.selectAll(".got-group-child[data-superTypeCode=\"" + selThis.attr("id") + "\"]").each(function () {
                        let child = d3.select(this);

                        child
                            .classed("dragging", false)
                            .style("left", null)
                            .style("top", null)
                            .style("width", null);
                    });
                }

                that.onDrop.emit({dragEl: this, event});

                // for (let i = 0; i < dropTargets.length; ++i) {
                //     dropTargets[i].onDrop(this, event);
                // }
            });

        sidebarDragHandler(d3.selectAll(".sidebar-section-content ul.list-group li.got-li-item"));
    }
}
