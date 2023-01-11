import { Injectable } from "@angular/core";
import { GeoObjectTypeCache } from "@registry/model/registry";
import { WebSockets } from "@shared/component/web-sockets/web-sockets";
import { Subscription } from "rxjs";
import { webSocket, WebSocketSubject } from "rxjs/webSocket";
import { RegistryService } from ".";

/**
 * The WebSocket functionality was found to be working for primary usecases (creating types, logging in, logging out) on 12/15/2022.
 * There does appear to be a corner-case where it stops working but I'm having trouble tracking it down.
 */
@Injectable()
export class RegistryCacheService {

    typeCache: GeoObjectTypeCache;

    notifier: WebSocketSubject<{ type: string, message: string }>;

    subscription: Subscription = null;

// This constructor is invoked when its injected into a component (not before). The component is also destroyed on logout.
    constructor(private service: RegistryService) {
        this.typeCache = new GeoObjectTypeCache(this.service);

        let baseUrl = WebSockets.buildBaseUrl();

        this.notifier = webSocket(baseUrl + "/websocket/notify");
        this.subscription = this.notifier.subscribe(message => {
            if (message.type === "TYPE_CACHE_CHANGE") {
                this.typeCache.refresh();
            }
        });
    }

// Logging out causes the WebSocket to be cleaned up.
/*
    ngOnDestroy() {
        if (this.subscription != null) {
            this.subscription.unsubscribe();
        }

        this.notifier.complete();
    }
    */

    public getTypeCache(): GeoObjectTypeCache {
        return this.typeCache;
    }

}
