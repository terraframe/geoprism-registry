export enum ModalTypes {
    "warning" = "WARNING",
    "danger" = "DANGER"
}

export class StepConfig {
    steps: Step[];
}

export class Step {
    label: string;
    active: boolean;
    enabled: boolean;
}