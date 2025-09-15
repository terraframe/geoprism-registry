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

import { HttpErrorResponse } from "@angular/common/http";
import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";
import { Commit, PublishEvents } from "@registry/model/publish";
import { PublishService } from "@registry/service/publish.service";

@Component({
    selector: "publish-events",
    templateUrl: "./publish-events.component.html",
    styleUrls: []
})
export class PublishEventsComponent implements OnInit {

    @Input() type: PublishEvents = null;
    @Input() types: { label: string, value: string }[] = [];
    @Input() hierarchies: { label: string, value: string }[] = [];
    @Input() dagTypes: { label: string, value: string }[] = [];
    @Input() undirectedTypes: { label: string, value: string }[] = [];
    @Input() businessTypes: { label: string, value: string }[] = [];
    @Input() edgeTypes: { label: string, value: string }[] = [];

    @Output() error = new EventEmitter<HttpErrorResponse>();

    commits: Commit[] = [];

    constructor(private service: PublishService) {
    }

    ngOnInit(): void {
        if (this.type != null) {
            this.service.getCommits(this.type.uid)
                .then(commits => this.commits = commits)
                .catch(err => this.error.emit(err))
        }
    }

    onCreateNewCommit(): void {

        if (this.type != null) {
            this.error.emit(null);

            this.service.createNewVersion(this.type.uid).then(dto => {
                this.service.getCommits(this.type.uid)
                    .then(commits => this.commits = commits)
                    .catch(err => this.error.emit(err))
            }).catch((err: HttpErrorResponse) => {
                this.error.emit(err);
            });
        }
    }
}
