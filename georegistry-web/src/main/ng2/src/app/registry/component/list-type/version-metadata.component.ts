import { Component, Input } from "@angular/core";
import { VersionMetadata } from "@registry/model/list-type";

@Component({
    selector: "version-metadata",
    templateUrl: "./version-metadata.component.html",
    styleUrls: []
})
export class VersionMetadataComponent {
    @Input() metadata: VersionMetadata;

    @Input() readonly: boolean;
}
