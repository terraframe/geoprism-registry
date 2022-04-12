import { Injectable } from "@angular/core";
import { GeoRegistryConfiguration } from "@core/model/registry";
import { LocalizedValue, LocaleView } from "@shared/model/core";

declare let registry: GeoRegistryConfiguration;

@Injectable()
export class LocalizationService {

    locales: LocaleView[] = [];
    locale: string;

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
            // if (!locale.isDefaultLocale)
            // {
            value.localeValues.push({ locale: locale.toString, value: "" });
            // }
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

    public localize(bundle: string, key: string): string {
        if (registry.localization[bundle] != null) {
            const b = registry.localization[bundle];

            if (b[key] != null) {
                return b[key];
            }
        }

        return "??" + key + "??";
    }

    public get(key: string): string {
        if (registry.localization[key] != null) {
            return registry.localization[key];
        }

        return "??" + key + "??";
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
