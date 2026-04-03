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

import { Component, OnInit } from "@angular/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Subject } from "rxjs";
import { TreeNode } from "@circlon/angular-tree-component";
import { HttpErrorResponse } from "@angular/common/http";
import { ErrorHandler } from "@shared/component";
import { HierarchyType, HierarchyNode } from "@registry/model/hierarchy";
import { GeoObjectType } from "@registry/model/registry";
import { HierarchyService } from "@registry/service";


@Component({
    selector: "add-child-to-hierarchy-modal",
    templateUrl: "./add-child-to-hierarchy-modal.component.html",
    styleUrls: []
})
export class AddChildToHierarchyModalComponent implements OnInit {

    /*
     * parent id of the node being created
     */
    public parent: TreeNode;
    public hierarchyType: HierarchyType;
    public nodes: HierarchyNode[];
    public allGeoObjectTypes: GeoObjectType[];
    public selectedGeoObjectType: GeoObjectType;
    private toRoot: boolean = false;
    selectUndefinedOptionValue: any;
    message: string = null;

    /*
     * Observable subject for TreeNode changes.  Called when create is successful
     */
    public onNodeChange: Subject<HierarchyType>;

    // eslint-disable-next-line no-useless-constructor
    constructor(private hierarchyService: HierarchyService, public bsModalRef: BsModalRef) { }

    ngOnInit(): void {
        this.onNodeChange = new Subject();
    }

    onSelect(event: Event): void {
        const value = (event.target as HTMLInputElement).value;    
            
        this.allGeoObjectTypes.forEach(gObj => {
            if (gObj.code === value) {
                this.selectedGeoObjectType = gObj;
            }
        });
    }

    handleOnSubmit(): void {
        this.message = null;

        let parent = (this.toRoot) ? "ROOT" : this.parent.data.geoObjectType;
        this.hierarchyService.addChildToHierarchy(this.hierarchyType.code, parent, this.selectedGeoObjectType.code).then(data => {
            this.onNodeChange.next(data);
            this.bsModalRef.hide();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
