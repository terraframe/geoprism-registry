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

import { Input, Component, OnInit } from "@angular/core";

import { LocalizationService } from "@shared/service/localization.service";

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
