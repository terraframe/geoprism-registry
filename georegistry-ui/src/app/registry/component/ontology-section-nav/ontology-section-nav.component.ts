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

import { Component, EventEmitter, Input, Output } from "@angular/core";
import { NgFor, NgClass } from "@angular/common";
import { LocalizeComponent } from "@shared/component/localize/localize.component";

export interface OntologySectionNavItem {
    id: string;
    labelKey: string;
    icon: string;
}

@Component({
    selector: "ontology-section-nav",
    templateUrl: "./ontology-section-nav.component.html",
    styleUrls: ["./ontology-section-nav.css"],
    standalone: true,
    imports: [NgFor, NgClass, LocalizeComponent]
})
export class OntologySectionNavComponent {

    @Input() items: OntologySectionNavItem[] = [];

    @Input() active: string;

    @Output() activeChange = new EventEmitter<string>();

    select(item: OntologySectionNavItem): void {
        if (item.id !== this.active) {
            this.active = item.id;
            this.activeChange.emit(item.id);
        }
    }

}
