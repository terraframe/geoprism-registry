export class GeoRegistryConfiguration {

    contextPath: string;
    locale: string;
    locales: [
        {
            "isDefaultLocale": boolean,
            "toString": string,
            "tag": string,
            "language": { "label": string, "code": string },
            "country": { "label": string, "code": string },
            "variant": { "label": string, "code": string },
            "label": any
        }
    ];
    searchEnabled: boolean;
    enableBusinessData: boolean;
    defaultMapBounds: [[number]];

}
