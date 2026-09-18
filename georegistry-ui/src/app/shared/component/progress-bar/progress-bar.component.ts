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

import { Component, HostBinding, OnInit } from "@angular/core";

import { Progress } from "@shared/model/progress";
import { ProgressService, IProgressListener } from "@shared/service";
import { ProgressbarModule } from "ngx-bootstrap/progressbar";
import { NgIf } from "@angular/common";

@Component({
    selector: "progress-bar",
    templateUrl: "./progress-bar.component.html",
    styles: [
        /*
         * :host establishes the containing block (and, only while showing,
         * the height) that .progress-overlay sizes itself against via
         * inset:0 below. Without this, .progress-overlay's old
         * width/height:100% had no positioned ancestor to resolve against,
         * so it fell back to the viewport - misaligning it from the actual
         * content area (a gap on the left where the sidebar's width wasn't
         * accounted for, and overflow on the right).
         */
        ":host { display: block; }",
        ":host(.is-showing) { position: relative; height: 100vh; }",
        ".progress-overlay { background-color: #CCCCCC; position: absolute; inset: 0; display: block;opacity: 0.8;z-index: 99999 !important;}",
        ".progress-div { width: 100%; margin-left: 0; padding-left: 25%; padding-right: 25%; margin-top: 30% }"
    ],
    standalone: true,
    imports: [NgIf, ProgressbarModule]
})
export class ProgressBarComponent implements OnInit, IProgressListener {

    @HostBinding("class.is-showing")
    public showIndicator: boolean = true;

    public prog: Progress = {
        current: 0,
        total: 1,
        description: "Initializing"
    };

    // eslint-disable-next-line no-useless-constructor
    constructor(private service: ProgressService) { }

    ngOnInit(): void {
        this.service.registerListener(this);
    }

    ngOnDestroy(): void {
        this.service.deregisterListener(this);
    }

    start(): void {
        this.prog = {
            current: 0,
            total: 1,
            description: "Initializing"
        };

        this.showIndicator = true;
    }

    progress(progress: Progress): void {
        this.prog = progress;
    }

    complete(): void {
        this.showIndicator = false;
    }

}
