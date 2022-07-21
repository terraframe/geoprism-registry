import { Component, OnDestroy, OnInit } from "@angular/core";
import { ListTypeVersion } from "@registry/model/list-type";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Subject } from "rxjs";

@Component({
    selector: "export-format-modal",
    templateUrl: "./export-format-modal.component.html",
    styleUrls: []
})
export class ExportFormatModalComponent implements OnInit, OnDestroy {

    format: string;

    actualGeometryType: string;

    list: ListTypeVersion = null;

    /*
     * Called on confirm
     */
    public onFormat: Subject<{ format: string, actualGeometryType: string }>;

    // eslint-disable-next-line no-useless-constructor
    constructor(public bsModalRef: BsModalRef) { }

    ngOnInit(): void {
        this.onFormat = new Subject();
    }

    ngOnDestroy(): void {
        this.onFormat.unsubscribe();
    }

    init(list: ListTypeVersion): void {
        this.list = list;
    }

    confirm(): void {
        this.bsModalRef.hide();
        this.onFormat.next({
            format: this.format,
            actualGeometryType: this.actualGeometryType
        });
    }

}
