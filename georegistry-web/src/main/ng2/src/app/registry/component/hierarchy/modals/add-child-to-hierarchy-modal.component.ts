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

    onSelect(value: string): void {
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
