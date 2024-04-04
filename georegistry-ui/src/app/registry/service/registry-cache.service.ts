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

import { Injectable, OnDestroy } from "@angular/core";
import { GeoObjectTypeCache } from "@registry/model/registry";
import { WebSockets } from "@shared/component/web-sockets/web-sockets";
import { Subscription } from "rxjs";
import { webSocket, WebSocketSubject } from "rxjs/webSocket";
import { RegistryService } from ".";
import { EventService, ISessionListener } from "@shared/service";

/**
 * The WebSocket functionality was found to be working for primary usecases (creating types, logging in, logging out) on 12/15/2022.
 * There does appear to be a corner-case where it stops working but I'm having trouble tracking it down.
 */
@Injectable()
export class RegistryCacheService implements ISessionListener, OnDestroy {

    typeCache: GeoObjectTypeCache;

    notifier: WebSocketSubject<{ type: string, message: string }>;

    subscription: Subscription = null;

    // This constructor is invoked when its injected into a component (not before). The component is also destroyed on logout.
    constructor(private eventService: EventService, private service: RegistryService) {
        this.typeCache = new GeoObjectTypeCache(this.service);

        let baseUrl = WebSockets.buildBaseUrl();

        this.notifier = webSocket(baseUrl + "/websocket/notify");
        this.subscription = this.notifier.subscribe(message => {
            if (message.type === "TYPE_CACHE_CHANGE") {
                this.typeCache.refresh();
            }
        });

        this.eventService.registerSessionListener(this);
    }

    ngOnDestroy(): void {
        this.eventService.deregisterSessionListener(this);

        // Logging out causes the WebSocket to be cleaned up.
        /*
        if (this.subscription != null) {
            this.subscription.unsubscribe();
        }

        this.notifier.complete();
        */

    }

    onLogin(): void {
        this.typeCache.refresh();
    }

    onLogout(): void {
    }

    public getTypeCache(): GeoObjectTypeCache {
        return this.typeCache;
    }

    public refresh(): void {
        if (this.typeCache != null) {
            this.typeCache.refresh();
        }
    }


}
