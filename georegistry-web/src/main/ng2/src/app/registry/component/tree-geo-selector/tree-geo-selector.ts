import { Component, Input, EventEmitter, Output, SimpleChanges } from "@angular/core";
import { IActionMapping, ITreeOptions } from "@circlon/angular-tree-component";

import { HierarchyOverTime } from "@registry/model/registry";

@Component({

    selector: "tree-geo-selector",
    templateUrl: "./tree-geo-selector.html",
    styleUrls: ["./tree-geo-selector.css"]
})
export class TreeGeoSelector {

    @Input() hierarchy: HierarchyOverTime;

    @Input() readOnly: boolean = false;

    @Input() forDate: Date = null;

    @Output() onManageVersion = new EventEmitter<HierarchyOverTime>();

    nodes: any[] = [];

    actionMapping: IActionMapping = {
        mouse: {
            click: null
        }
    }

    options: ITreeOptions = {
        displayField: "name",
        isExpandedField: "expanded",
        idField: "code",
        actionMapping: this.actionMapping
    };

    // eslint-disable-next-line no-useless-constructor
    constructor() {

    }

    ngOnInit(): void {
        this.calculate();
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes["forDate"]) {
            this.calculate();
        }
    }

    calculate(): any {
        let time = null;

        if (this.forDate != null) {
            time = this.forDate.getTime();
        }

        let nodes = [];
        let current = null;

        this.hierarchy.entries.forEach(pot => {
            const startDate = Date.parse(pot.startDate);
            const endDate = Date.parse(pot.endDate);

            // eslint-disable-next-line no-mixed-operators
            if (time == null || time >= startDate && time <= endDate) {
                this.hierarchy.types.forEach(type => {
                    let node: any = {
                        code: type.code,
                        label: type.label,
                        children: [],
                        expanded: false
                    };

                    if (pot.parents[type.code] != null) {
                        node.name = pot.parents[type.code].text;

                        if (time == null) {
                            node.name = node.name + " (" + pot.startDate + " -> " + pot.endDate + ")";
                        }

                        node.geoObject = pot.parents[type.code].geoObject;
                    }

                    if (current == null) {
                        nodes.push(node);
                    } else {
                        current.children.push(node);
                        current.expanded = true;
                    }

                    current = node;
                });
            }

            current = null;
        });

        this.nodes = nodes;
    }

    onEdit(): void {
        this.onManageVersion.emit(this.hierarchy);
    }

}
