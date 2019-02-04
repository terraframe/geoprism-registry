export enum ModalTypes {
    "warning" = "WARNING",
    "danger" = "DANGER"
}

export class StepConfig {
    steps: Step[];
}

export class Step {
    label: string;
    order: number;
    active: boolean;
    enabled: boolean;
}