import { Injectable } from "@angular/core";
import { GeoObjectTypeCache } from "@registry/model/registry";
import { RegistryService } from ".";

@Injectable()
export class RegistryCacheService {

    typeCache: GeoObjectTypeCache;

    private initialized: boolean = false;

    constructor(private service: RegistryService) {
        this.typeCache = new GeoObjectTypeCache(this.service);
        this.typeCache.refresh().then(types => {
            this.initialized = true;
        });
    }

    public getTypeCache(): GeoObjectTypeCache {
        return this.typeCache;
    }

}
