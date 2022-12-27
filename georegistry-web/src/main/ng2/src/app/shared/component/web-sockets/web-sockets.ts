
import { GeoRegistryConfiguration } from "@core/model/core";

import { environment } from 'src/environments/environment';

export class WebSockets {

    static buildBaseUrl(): string {
        let protocol = "wss";

        if (window.location.protocol.indexOf("https") !== -1) {
            protocol = "wss"; // Web Socket Secure
        } else {
            protocol = "ws";
        }

        let baseUrl = protocol + "://" + window.location.hostname + (window.location.port ? ":" + window.location.port : "") + environment.apiUrl;

        return baseUrl;
    }

}
