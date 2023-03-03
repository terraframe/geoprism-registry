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

import { Component, OnInit } from "@angular/core";

import { EventService, IEventListener } from "@shared/service";

@Component({

    selector: "message",
    templateUrl: "./message.component.html",
    styles: [
        ".error-container { text-align: center; border-width: 1px; border-color: #8c0000; border-style: solid; color: #8c0000; font-size: 17px; background: white;}",
        ".message-container { text-align: center; border-width: 1px; border-color: #106828; border-style: solid; margin-left: 10%; margin-right: 10%; color: #106828; }"
    ]
})
export class MessageComponent implements OnInit, IEventListener {

    error: any = null;

    message: string = "";

    // eslint-disable-next-line no-useless-constructor
    constructor(private service: EventService) { }

    ngOnInit(): void {
        this.service.registerListener(this);
    }

    ngOnDestroy(): void {
        this.service.deregisterListener(this);
    }

    start(): void {
        this.error = null;
    }

    complete(): void {
    }

    onError(error: any): void {
        this.error = error;
    }

    onMessage(msg: string): void {
        this.message = msg;
    }

}
