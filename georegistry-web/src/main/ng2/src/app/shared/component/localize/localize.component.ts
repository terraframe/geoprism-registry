import { Input, Component, OnInit } from "@angular/core";

import { LocalizationService } from "@shared/service";

@Component({

    selector: "localize",
    templateUrl: "./localize.component.html",
    styleUrls: []
})
export class LocalizeComponent implements OnInit {

    @Input() key: string;
    @Input() params: { [key: string]: string } = null;

    text: string;

    // eslint-disable-next-line no-useless-constructor
    constructor(private service: LocalizationService) { }

    ngOnInit(): void {
        this.text = this.service.decode(this.key);

        if (this.params != null) {
            const keys = Object.keys(this.params);

            keys.forEach((key) => {
                if (this.params[key] != null) {
                    this.text = this.text.replace(key, this.params[key]);
                }
            });
        }
    }

}
