export class LocaleInfo {
  key: string;
  label: string;
}

export class AllLocaleInfo {
  countries: LocaleInfo[];
  languages: LocaleInfo[];
}

export class Locale {
  language: string;
  country: string
  name: string;
  variant: string;
  displayLabel: string;
}