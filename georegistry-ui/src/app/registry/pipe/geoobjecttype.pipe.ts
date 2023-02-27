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

import { Pipe, PipeTransform } from "@angular/core";
import { HierarchyNode } from "@registry/model/hierarchy";
import { GeoObjectType } from "@registry/model/registry";

@Pipe({
    name: "geoobjecttype",
    pure: false
})
export class GeoObjectTypePipe implements PipeTransform {

    transform(items: GeoObjectType[], filter: HierarchyNode[]): any {
        if (!items || !filter) {
            return items;
        }

        let unassignedGeoObjTypes: string[] = [];
        this.buildUnassignedGeoObjTypes(filter, unassignedGeoObjTypes)

        // filter items array, items which match and return true will be
        // kept, false will be filtered out
        return items.filter(item => unassignedGeoObjTypes.indexOf(item.code) === -1);
    }

    buildUnassignedGeoObjTypes(filter: HierarchyNode[], unassignedGeoObjTypes: string[]): void {
        filter.forEach(f => {
            this.processHierarchyNodes(f, unassignedGeoObjTypes);
        })
    }

    processHierarchyNodes(node: HierarchyNode, unassignedGeoObjTypes: string[]) {
        unassignedGeoObjTypes.push(node.geoObjectType)

        node.children.forEach(child => {
            this.processHierarchyNodes(child, unassignedGeoObjTypes);
        })
    }
}