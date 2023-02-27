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

import { Layer } from "./layer-data-source";

export class LayerDiff {

    type: "LAYER_REORDER" | "REMOVE_LAYER" | "NEW_LAYER" | "COLOR_CHANGE" | "RENDERED_CHANGE"
    newLayer: Layer;
    newLayerIndex: number;
    oldLayer: Layer;
    oldLayerIndex: number;

    constructor(type: "LAYER_REORDER" | "REMOVE_LAYER" | "NEW_LAYER" | "COLOR_CHANGE" | "RENDERED_CHANGE", newLayer: Layer, newLayerIndex: number, oldLayer: Layer, oldLayerIndex: number) {
        this.type = type;
        this.newLayer = newLayer;
        this.newLayerIndex = newLayerIndex;
        this.oldLayer = oldLayer;
        this.oldLayerIndex = oldLayerIndex;
    }

    equals(obj: any) {
        if (!(obj instanceof LayerDiff)) {
            return false;
        }

        return this.type === obj.type &&
               this.newLayerIndex === obj.newLayerIndex && this.oldLayerIndex === obj.oldLayerIndex &&
               ((this.newLayer == null && obj.newLayer == null) || (this.newLayer != null && obj.newLayer != null && this.newLayer.getId() === obj.newLayer.getId())) &&
               ((this.oldLayer == null && obj.oldLayer == null) || (this.oldLayer != null && obj.oldLayer != null && this.oldLayer.getId() === obj.oldLayer.getId()));
    }

}

export class LayerDiffingStrategy {

    private newLayers: Layer[];

    private oldLayers: Layer[];

    private diffs: LayerDiff[] = [];

    constructor(newLayers: Layer[], oldLayers: Layer[]) {
        this.newLayers = newLayers;
        this.oldLayers = oldLayers;
        this.calculateDiffs();
    }

    public getDiffs(): LayerDiff[] {
        return this.diffs;
    }

    private calculateDiffs(): void {
        this.diffs = [];

        let iterations = Math.max(this.newLayers.length, this.oldLayers.length);
        for (let i = 0; i < iterations; ++i) {
            let newLayer: Layer = null;
            let oldLayer: Layer = null;
            let newLayerIndex: number = -1;
            let oldLayerIndex: number = -1;

            if (i < this.newLayers.length) {
                newLayer = this.newLayers[i];
                newLayerIndex = i;

                oldLayerIndex = this.oldLayers.findIndex(findLayer => findLayer.getId() === newLayer.getId());
                if (oldLayerIndex !== -1) {
                    oldLayer = this.oldLayers[oldLayerIndex];
                }

                this.diffLayers(newLayer, newLayerIndex, oldLayer, oldLayerIndex);
            }

            newLayer = null;
            oldLayer = null;
            newLayerIndex = -1;
            oldLayerIndex = -1;

            if (i < this.oldLayers.length) {
                oldLayer = this.oldLayers[i];
                oldLayerIndex = i;

                newLayerIndex = this.newLayers.findIndex(findLayer => findLayer.getId() === oldLayer.getId());
                if (newLayerIndex !== -1) {
                    newLayer = this.newLayers[newLayerIndex];
                }

                this.diffLayers(newLayer, newLayerIndex, oldLayer, oldLayerIndex);
            }
        }
    }

    private addDiff(diff: LayerDiff) {
        if (this.diffs.findIndex(search => search.equals(diff)) === -1) {
            this.diffs.push(diff);
        }
    }

    private diffLayers(newLayer: Layer, newLayerIndex: number, oldLayer: Layer, oldLayerIndex: number): void {
        if (newLayer != null && oldLayer != null) {
            if (newLayerIndex !== oldLayerIndex) {
                this.addDiff(new LayerDiff("LAYER_REORDER", newLayer, newLayerIndex, oldLayer, oldLayerIndex));
            }

            if (newLayer.rendered !== oldLayer.rendered) {
                this.addDiff(new LayerDiff("RENDERED_CHANGE", newLayer, newLayerIndex, oldLayer, oldLayerIndex));
            }
            if (newLayer.color !== oldLayer.color) {
                this.addDiff(new LayerDiff("COLOR_CHANGE", newLayer, newLayerIndex, oldLayer, oldLayerIndex));
            }
        } else if (newLayer != null && oldLayer == null) {
            this.addDiff(new LayerDiff("NEW_LAYER", newLayer, newLayerIndex, oldLayer, oldLayerIndex));
        } else if (newLayer == null && oldLayer != null) {
            this.addDiff(new LayerDiff("REMOVE_LAYER", newLayer, newLayerIndex, oldLayer, oldLayerIndex));
        }
    }

}
