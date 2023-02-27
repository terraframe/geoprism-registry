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

import { select } from "d3";

export function calculateTextWidth(text: string, fontSize: number, svgSelector: string = "#svg"): number {
    let svg = select(svgSelector);

    let textCalcGroup = svg.append("g").classed("g-text-calc", true);

    let textEl = textCalcGroup.append("text")
        .attr("x", -5000)
        .attr("y", -5000)
        .attr("font-size", fontSize)
        .text(text);

    let bbox = textEl.node().getBBox();

    select(".g-text-calc").remove();

    return bbox.width + 2; // +2 is for padding. I caught this truncating just ever so slightly
}

export function svgPoint(x: number, y: number) {
    let svg: any = select("#svg").node();
    let pt = svg.createSVGPoint();

    pt.x = x;
    pt.y = y;

    return pt.matrixTransform(svg.getScreenCTM().inverse());
}

export function isPointWithin(point: { x: number, y: number }, bbox: { x: number, y: number, width: number, height: number }) {
    return point.y > bbox.y && point.y < (bbox.y + bbox.height) && point.x > bbox.x && point.x < (bbox.x + bbox.width);
}

export function isBboxPartiallyWithin(bbox1: { x: number, y: number, width: number, height: number }, bbox2: { x: number, y: number, width: number, height: number }) {
    return isPointWithin({ x: bbox1.x, y: bbox1.y }, bbox2) || isPointWithin({ x: bbox1.x + bbox1.width, y: bbox1.y + bbox1.height }, bbox2) ||
        isPointWithin({ x: bbox1.x + bbox1.width, y: bbox1.y }, bbox2) || isPointWithin({ x: bbox1.x, y: bbox1.y + bbox1.height }, bbox2);
}

export function isBboxTotallyWithin(bbox1: { x: number, y: number, width: number, height: number }, bbox2: { x: number, y: number, width: number, height: number }) {
    return isPointWithin({ x: bbox1.x, y: bbox1.y }, bbox2) && isPointWithin({ x: bbox1.x + bbox1.width, y: bbox1.y + bbox1.height }, bbox2) &&
        isPointWithin({ x: bbox1.x + bbox1.width, y: bbox1.y }, bbox2) && isPointWithin({ x: bbox1.x, y: bbox1.y + bbox1.height }, bbox2);
}

export function getBboxFromSelection(selection: any) {
    return { x: parseInt(selection.attr("x")), y: parseInt(selection.attr("y")), width: parseInt(selection.attr("width")), height: parseInt(selection.attr("height")) };
}

