
import { GeoRegistryConfiguration } from "@core/model/registry";

declare let registry: GeoRegistryConfiguration;

export class WebSockets {

    static buildBaseUrl(): string {
        let protocol = "wss";

        if (window.location.protocol.indexOf("https") !== -1) {
            protocol = "wss"; // Web Socket Secure
        } else {
            protocol = "ws";
        }

        let baseUrl = protocol + "://" + window.location.hostname + (window.location.port ? ":" + window.location.port : "") + registry.contextPath;

        return baseUrl;
    }

}
