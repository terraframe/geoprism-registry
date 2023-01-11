import { Component } from "@angular/core";
import { AuthService } from "@shared/service";

@Component({

    selector: "historical-event-module",
    templateUrl: "./historical-event-module.component.html",
    styleUrls: []
})
export class HistoricalEventModuleComponent {

    tab: string = "HISTORICAL-EVENT";

    readOnly: boolean = false;

    // eslint-disable-next-line no-useless-constructor
    constructor(private authService: AuthService) { }

    ngOnInit(): void {
        this.readOnly = this.authService.isRC(true);
        this.tab = this.readOnly ? "HISTORICAL-REPORT" : "HISTORICAL-EVENT";
    }

    handleTab(tab: string): void {
        this.tab = tab;
    }

}
