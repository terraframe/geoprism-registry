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

import { Component, Input, OnDestroy, OnInit } from "@angular/core";
import { HubService } from "@core/service/hub.service";
import { Subscription } from "rxjs";

@Component({
    selector: "page-container",
    templateUrl: "./page-container.component.html",
    styleUrls: ['./page-container.css']
})
export class PageContainerComponent implements OnInit, OnDestroy {
    @Input() loadingBar: boolean = true;

    expanded: boolean = false;

    subscription: Subscription = null;

    constructor(private service: HubService) {
    }

    ngOnInit(): void {
        this.subscription = this.service.getExpanded().subscribe(expanded => {
            console.log('Setting expanded: ', expanded);

            this.expanded = expanded;
        });
    }

    ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }

    onToggleExpanded(): void {
        this.expanded = !this.expanded;

        console.log('Expanded', this.expanded);
    }
}
