import { Injectable } from "@angular/core";
import { LocalizedValue, LocaleView } from "@shared/model/core";

declare let Globalize: any;
declare let com: any;
declare let registry: any;

@Injectable()
export class LocalizationService {

    locales: LocaleView[] = [];
    locale: string;

    private parser: any = Globalize.numberParser();
    private formatter: any = Globalize.numberFormatter();

    constructor() {
        this.locales = registry.locales;
        this.locale = registry.locale;
    }

    addLocale(locale: LocaleView): void {
        let exists: boolean = false;

        for (let i = 0; i < this.locales.length; ++i) {
            if (this.locales[i].tag === locale.tag) {
                exists = true;
                this.locales[i] = locale;
            }
        }

        if (!exists) {
            this.locales.push(locale);
        }
    }

    setLocales(locales: LocaleView[]): void {
        this.locales = locales;
    }

    getLocale(): string {
        return this.locale;
    }

    getLocales(): LocaleView[] {
        return this.locales;
    }

    create(): LocalizedValue {
        const value = { localizedValue: "", localeValues: [] } as LocalizedValue;

        this.locales.forEach(locale => {
            //if (!locale.isDefaultLocale)
            //{
            value.localeValues.push({ locale: locale.toString, value: "" });
            //}
        });

        return value;
    }

    remove(locale: LocaleView): void {
        for (let i = 0; i < this.locales.length; ++i) {
            if (this.locales[i].tag === locale.tag) {
                this.locales.splice(i, 1);
                return;
            }
        }

        // eslint-disable-next-line no-console
        console.log("Could not remove locale from array because we could not find it.", locale, this.locales);
    }

    public parseNumber(value: string): number {
        if (value != null && value.length > 0) {
            // convert data from view format to model format
            let number = this.parser(value);

            return number;
        }

        return null;
    }

    public formatNumber(value: any): string {
        if (value != null) {
            let number = value;

            if (typeof number === "string") {
                if (number.length > 0 && Number(number)) {
                    number = Number(value);
                } else {
                    return "";
                }
            }

            // convert data from model format to view format
            return this.formatter(number);
        }

        return null;
    }

    public localize(bundle: string, key: string): string {
        return com.runwaysdk.Localize.localize(bundle, key);
    }

    public get(key: string): string {
        return com.runwaysdk.Localize.get(key);
    }

    public decode(key: string): string {
        let index = key.lastIndexOf(".");

        if (index !== -1) {
            let temp = [key.slice(0, index), key.slice(index + 1)];

            return this.localize(temp[0], temp[1]);
        } else {
            return this.get(key);
        }
    }

}
