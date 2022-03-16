import { Injectable } from "@angular/core";
import { GeoObjectTypeCache } from "@registry/model/registry";
import { WebSockets } from "@shared/component/web-sockets/web-sockets";
import { Subscription } from "rxjs";
import { webSocket, WebSocketSubject } from "rxjs/webSocket";
import { RegistryService } from ".";

@Injectable()
export class RegistryCacheService {

    typeCache: GeoObjectTypeCache;

    notifier: WebSocketSubject<{ type: string, message: string }>;

    subscription: Subscription = null;

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
