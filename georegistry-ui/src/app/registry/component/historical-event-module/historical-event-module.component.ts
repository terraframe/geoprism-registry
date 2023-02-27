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
