
export interface CustomAttributeConfig {
  name: string;
  label: string;
  type: string;
  typeLabel: string;
  dhis2Attrs: Dhis2Attr[];
  terms: Term[];
}

export interface Dhis2Attr {
  name: string;
  code: string;
  dhis2Id: string;
  options: Option[];
}

export interface Term {
  label: string;
  code: string;
}

export interface Option {
  code: string;
  name: string;
  id: string;
}
