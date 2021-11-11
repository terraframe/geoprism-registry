import { Injectable } from "@angular/core";
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { finalize } from "rxjs/operators";

import { EventService } from "@shared/service";
import { ListType, ListTypeByType } from "@registry/model/list-type";
import { GeoObjectType } from "@registry/model/registry";


declare let acp: any;

@Injectable()
export class ListTypeService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    listForType(type: GeoObjectType): Promise<ListTypeByType> {
        let params: HttpParams = new HttpParams();
        params = params.set("typeCode", type.code);

        this.eventService.start();

        return this.http.get<ListTypeByType>(acp + "/list-type/list-for-type", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    apply(list: ListType): Promise<ListType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<ListType>(acp + "/list-type/apply", JSON.stringify({ list: list }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    delete(list: ListType): Promise<ListType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<ListType>(acp + "/list-type/delete", JSON.stringify({ oid: list.oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }
}
