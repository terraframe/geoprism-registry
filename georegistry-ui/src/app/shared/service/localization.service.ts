import { Injectable } from "@angular/core";
import { LocaleView, LocalizedValue } from "@core/model/core";
import { ConfigurationService } from "@core/service/configuration.service";

@Injectable()
export class LocalizationService {

    constructor(private config : ConfigurationService) {
    }

    addLocale(locale: LocaleView): void {
        const config = this.config.getConfiguration();

        let exists: boolean = false;

        for (let i = 0; i < config.locales.length; ++i) {
            if (config.locales[i].tag === locale.tag) {
                exists = true;
                config.locales[i] = locale;
            }
        }

        if (!exists) {
            config.locales.push(locale);
        }
    }

    setLocales(locales: LocaleView[]): void {
        this.config.getConfiguration().locales = locales;
    }

    getLocale(): string {
        return this.config.getConfiguration().locale;
    }

    getLocales(): LocaleView[] {
        return this.config.getConfiguration().locales;
    }

    create(): LocalizedValue {
        const value = { localizedValue: "", localeValues: [] } as LocalizedValue;

        this.config.getConfiguration().locales.forEach(locale => {
            // if (!locale.isDefaultLocale)
            // {
            value.localeValues.push({ locale: locale.toString, value: "" });
            // }
        });

        return value;
    }

    remove(locale: LocaleView): void {
        const config = this.config.getConfiguration();

        for (let i = 0; i < config.locales.length; ++i) {
            if (config.locales[i].tag === locale.tag) {
                config.locales.splice(i, 1);
                return;
            }
        }

        // eslint-disable-next-line no-console
        console.log("Could not remove locale from array because we could not find it.", locale, config.locales);
    }

    public localize(bundle: string, key: string): string {
        const config = this.config.getConfiguration();

        if (config.localization[bundle] != null) {
            const b = config.localization[bundle];

            if (b[key] != null) {
                return b[key];
            }
        }

        return "??" + key + "??";
    }

    public get(key: string): string {
        const config = this.config.getConfiguration();

        if (config.localization[key] != null) {
            return config.localization[key];
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
