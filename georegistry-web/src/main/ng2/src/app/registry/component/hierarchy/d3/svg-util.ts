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

