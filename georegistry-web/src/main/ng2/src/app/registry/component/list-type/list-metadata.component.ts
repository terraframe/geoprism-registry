import { Component, Input } from "@angular/core";
import { ListMetadata } from "@registry/model/list-type";

@Component({
    selector: "list-metadata",
    templateUrl: "./list-metadata.component.html",
    styleUrls: []
})
export class ListMetadataComponent {
    @Input() metadata: ListMetadata;

    @Input() readonly: boolean;
}
