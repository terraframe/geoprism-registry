import { Component, OnInit } from "@angular/core";

import { ActivatedRoute } from "@angular/router";
import { GenericTableColumn, GenericTableConfig, TableEvent } from "@shared/model/generic-table";
import { Subject } from "rxjs";
import { BusinessTypeService } from "@registry/service/business-type.service";
import { BusinessType } from "@registry/model/business-type";

@Component({
    selector: "business-table",
    templateUrl: "./business-table.component.html",
    styles: []
})
export class BusinessTableComponent implements OnInit {

    message: string = null;

    businessType: BusinessType;

    config: GenericTableConfig = null;
    cols: GenericTableColumn[] = [];

    refresh: Subject<void>;

    constructor(private service: BusinessTypeService, private route: ActivatedRoute) { }

    ngOnInit(): void {
        const oid = this.route.snapshot.paramMap.get("oid");

        this.service.get(oid).then(businessType => {
            this.businessType = businessType;

            this.cols = [];

            this.businessType.attributes.forEach(attribute => {
                let type = "TEXT";
                let sortable = true;

                if (attribute.type === "integer" || attribute.type === "decimal") {
                    type = "NUMBER";
                } else if (attribute.type === "boolean") {
                    type = "BOOLEAN";
                } else if (attribute.type === "term") {
                    sortable = false;
                }

                this.cols.push({ header: attribute.label.localizedValue, field: attribute.code, type: type, sortable: sortable, filter: sortable });
            });
            this.cols.push({ header: "Geo Object", field: "geoObject", type: "TEXT", sortable: true });

            // this.cols.push({ header: "", type: "ACTIONS", sortable: false });

            this.config = {
                service: this.service,
                remove: false,
                view: false,
                create: false,
                label: this.businessType.displayLabel.localizedValue,
                sort: { field: "code", order: 1 }
            };
        });

        this.refresh = new Subject<void>();
    }

    onClick(event: TableEvent): void {
        // if (event.type === "view") {
        //     this.onView(event.row as BusinessType);
        // } else if (event.type === "remove") {
        //     this.onRemove(event.row as BusinessType);
        // } else if (event.type === "create") {
        //     this.newInstance();
        // }
    }

    // remove(sensor: BusinessType): void {
    //     this.service.remove(sensor.oid).then(response => {
    //         this.refresh.next();
    //     });
    // }

    // onRemove(sensor: BusinessType): void {
    //     this.bsModalRef = this.modalService.show(BasicConfirmModalComponent, {
    //         animated: true,
    //         backdrop: true,
    //         ignoreBackdropClick: true
    //     });
    //     this.bsModalRef.content.message = "Are you sure you want to remove the sensor [" + sensor.name + "]";
    //     this.bsModalRef.content.type = "DANGER";
    //     this.bsModalRef.content.submitText = "Delete";

    //     this.bsModalRef.content.onConfirm.subscribe(data => {
    //         this.remove(sensor);
    //     });
    // }

    // onView(sensor: BusinessType): void {
    //     this.router.navigate(["/site/sensor", sensor.oid]);
    // }

    // newInstance(): void {
    //     this.router.navigate(["/site/sensor", "__NEW__"]);
    // }

}
