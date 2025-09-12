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

import { Component, OnDestroy, OnInit } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { v4 as uuid } from "uuid";

import { ErrorHandler } from "@shared/component";
import { Publish, PublishEvents } from "@registry/model/publish";
import { PublishService } from "@registry/service/publish.service";
import { GeoObjectType, GraphType } from "@registry/model/registry";
import { HierarchyType } from "@registry/model/hierarchy";
import { Organization } from "@shared/model/core";
import { BusinessEdgeType, BusinessType } from "@registry/model/business-type";
import { RegistryService } from "@registry/service";
import { BusinessTypeService } from "@registry/service/business-type.service";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Observable, Observer, Subject, Subscription } from "rxjs";

@Component({
    selector: "publish-events-modal",
    templateUrl: "./publish-events-modal.component.html",
    styleUrls: []
})
export class PublishEventsModalComponent implements OnInit, OnDestroy {

    currentDate: Date = new Date();
    message: string = null;

    type: PublishEvents = null;
    isNew: boolean = true;
    readonly: boolean = false;

    types: { label: string, value: string }[] = [];
    hierarchies: { label: string, value: string }[] = [];
    dagTypes: { label: string, value: string }[] = [];
    undirectedTypes: { label: string, value: string }[] = [];
    businessTypes: { label: string, value: string }[] = [];
    edgeTypes: { label: string, value: string }[] = [];

    onChange: Subject<PublishEvents>;


    // eslint-disable-next-line no-useless-constructor
    constructor(
        private businessService: BusinessTypeService,
        private registryService: RegistryService,
        private bsModalRef: BsModalRef,
        private service: PublishService) {
    }

    ngOnInit(): void {

    }

    ngOnDestroy(): void {
        this.onChange.unsubscribe();
    }

    init(observerOrNext?: Partial<Observer<PublishEvents>> | ((value: PublishEvents) => void), type?: PublishEvents): void {
        this.onChange = new Subject();

        if (observerOrNext != null) {
            this.onChange.subscribe(observerOrNext);
        }

        this.businessService.getAll().then(response => {
            this.businessTypes = response.map(b => { return { label: b.displayLabel.localizedValue, value: b.code } });
        })

        this.businessService.getEdges().then(response => {
            this.edgeTypes = response.map(b => { return { label: b.label.localizedValue, value: b.code } });
        })

        this.registryService.init(false, true).then(response => {
            this.hierarchies = response.hierarchies.map(b => { return { label: b.label.localizedValue, value: b.code } });
            this.types = response.types.map(b => { return { label: b.label.localizedValue, value: b.code } });
            this.dagTypes = response.graphTypes
                .filter(b => b.typeCode === 'DirectedAcyclicGraphType')
                .map(b => { return { label: b.label.localizedValue, value: b.code } });
            this.undirectedTypes = response.graphTypes
                .filter(b => b.typeCode === 'UndirectedGraphType')
                .map(b => { return { label: b.label.localizedValue, value: b.code } });
        });

        this.type = {
            uid: uuid(),
            date: null,
            startDate: null,
            endDate: null,
            typeCodes: [],
            businessTypeCodes: [],
            hierarchyCodes: [],
            dagCodes: [],
            undirectedCodes: [],
            businessEdgeCodes: []
        }
    }

    onSubmit(): void {


        this.service.create(this.type).then(dto => {
            // Do something
            this.onChange.next(dto);

            this.bsModalRef.hide();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onCancel(): void {
        this.bsModalRef.hide();
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
