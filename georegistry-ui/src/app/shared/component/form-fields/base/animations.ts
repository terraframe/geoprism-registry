import {
    AnimationTriggerMetadata,
    animate,
    state,
    style,
    transition,
    trigger
} from "@angular/animations";

export const animations: Array<AnimationTriggerMetadata> = [
    trigger("flyInOut", [
        state("in", style({ transform: "translateX(0)" })),
        transition("void => *", [
            style({ transform: "translateX(100%)" }),
            animate(1000)
        ]),
        state("out", style({ transform: "translateX(100%)" })),
        transition("* => void", [
            animate(1000, style({ transform: "translateX(100%)" }))
        ])
    ]),

    trigger("fadeInOut", [
        state("out",
            style({ opacity: 0 })
        ),
        state("in",
            style({ opacity: 1 })
        ),
        transition("out => in", animate("2000ms"))
    ])
];
