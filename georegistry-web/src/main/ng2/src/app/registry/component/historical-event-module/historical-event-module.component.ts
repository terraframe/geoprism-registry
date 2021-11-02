import { Component } from "@angular/core";

@Component({

    selector: "historical-event-module",
    templateUrl: "./historical-event-module.component.html",
    styleUrls: [],
})
export class HistoricalEventModuleComponent {

    tab: string = 'HISTORICAL-EVENT';

    handleTab(tab: string): void {
        this.tab = tab;
    }

}
